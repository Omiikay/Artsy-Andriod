package com.csci571.artsyapp;

import android.app.Application;
import com.csci571.artsyapp.data.api.ApiClient
import com.csci571.artsyapp.utils.PersistentCookieJar
import okhttp3.CookieJar

/**
 * ArtistSearchApplication.kt
 * This is the main application class for the Artsy app.
 * It initializes the cookie jar and API client on application startup.
 */
class ArtistSearchApplication : Application() {
    lateinit var cookieJar: CookieJar

    override fun onCreate() {
        super.onCreate()

        // Initialize cookie jar
        cookieJar = PersistentCookieJar(this)

        // Initialize API client
        ApiClient.initialize(this)
    }
}
