package com.csci571.artsyapp.data.api

import android.content.Context
import com.csci571.artsyapp.ArtistSearchApplication
import com.csci571.artsyapp.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private lateinit var apiService: ArtsyApi

    fun initialize(context: Context) {
        val app = context.applicationContext as ArtistSearchApplication

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(app.cookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ArtsyApi::class.java)
    }

    fun getApiService(): ArtsyApi {
        if (!::apiService.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized before use")
        }
        return apiService
    }
}