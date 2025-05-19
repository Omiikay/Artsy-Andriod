package com.csci571.artsyapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import androidx.core.content.edit

class PersistentCookieJar(context: Context) : CookieJar {
    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("OkHttpCookies", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        // Load cookies from SharedPreferences
        val allCookies = prefs.all
        for ((key, value) in allCookies) {
            if (value is String) {
                val cookiesType = object : TypeToken<List<SerializableCookie>>() {}.type
                val cookies = gson.fromJson<List<SerializableCookie>>(value, cookiesType)
                val validCookies = cookies
                    .mapNotNull { it.cookie }
                    .filter { !it.expiresAt.isExpired() }

                if (validCookies.isNotEmpty()) {
                    cookieStore[key] = validCookies
                } else {
                    prefs.edit { remove(key) }
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existingCookies = cookieStore[host].orEmpty().toMutableList()

        for (cookie in cookies) {
            // Remove existing cookies with the same name
            existingCookies.removeAll { it.name == cookie.name }

            // Add the new cookie if it's not expired
            if (!cookie.expiresAt.isExpired()) {
                existingCookies.add(cookie)
            }
        }

        if (existingCookies.isNotEmpty()) {
            cookieStore[host] = existingCookies
            saveCookiesToPrefs(host, existingCookies)
        } else {
            cookieStore.remove(host)
            prefs.edit { remove(host) }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host].orEmpty()

        // Filter out expired cookies
        val validCookies = cookies.filter { !it.expiresAt.isExpired() }

        // Update the store if expired cookies were removed
        if (validCookies.size < cookies.size) {
            cookieStore[url.host] = validCookies
            saveCookiesToPrefs(url.host, validCookies)
        }

        return validCookies
    }

    private fun saveCookiesToPrefs(host: String, cookies: List<Cookie>) {
        val serializableCookies = cookies.map { SerializableCookie(it) }
        val json = gson.toJson(serializableCookies)
        prefs.edit { putString(host, json) }
    }

    private fun Long.isExpired(): Boolean = this < System.currentTimeMillis()

    // Helper class for serializing cookies
    private data class SerializableCookie(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean
    ) {
        constructor(cookie: Cookie) : this(
            cookie.name,
            cookie.value,
            cookie.expiresAt,
            cookie.domain,
            cookie.path,
            cookie.secure,
            cookie.httpOnly,
            cookie.hostOnly
        )

        val cookie: Cookie?
            get() {
                // Don't create expired cookies
                if (expiresAt < System.currentTimeMillis()) return null

                return Cookie.Builder()
                    .name(name)
                    .value(value)
                    .expiresAt(expiresAt)
                    .apply {
                        if (hostOnly) {
                            hostOnlyDomain(domain)
                        } else {
                            domain(domain)
                        }
                        path(path)
                        if (secure) secure()
                        if (httpOnly) httpOnly()
                    }
                    .build()
            }
    }
}