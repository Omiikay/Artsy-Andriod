package com.csci571.artsyapp.utils

/** Constants.kt
 * This file contains constants used throughout the application, including the base URL for API requests
 * and route names for navigation.
 */
object Constants {
    const val BASE_URL = "https://csci571-hw5-nrocikay.wl.r.appspot.com/"

    object Route {
        const val HOME = "home"
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val ARTIST_DETAIL = "artist_detail"
        const val SEARCH_RESULTS = "search_results"
    }
}
