package com.csci571.artsyapp.utils

import android.util.Patterns
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import java.util.concurrent.TimeUnit

fun isValidEmail(email: String): Boolean {
    return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Returns a human-readable relative time string for the given date,
 * e.g. "5 seconds ago", "3 minutes ago", "2 hours ago", "4 days ago", "3 weeks ago", "5 months ago", "1 year ago".
 */
fun formatDateTime(date: Date): String {
    val now = Date()
    val diffMs = now.time - date.time
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> {
            val sec = if (seconds < 1) 1 else seconds
            "$sec ${if (sec == 1L) "second" else "seconds"} ago"
        }
        minutes < 60 -> {
            val min = if (minutes < 1) 1 else minutes
            "$min ${if (min == 1L) "minute" else "minutes"} ago"
        }
        hours < 24 -> {
            val hr = if (hours < 1) 1 else hours
            "$hr ${if (hr == 1L) "hour" else "hours"} ago"
        }
        days < 7 -> {
            val d = if (days < 1) 1 else days
            "$d ${if (d == 1L) "day" else "days"} ago"
        }
        weeks < 4 -> {
            val w = if (weeks < 1) 1 else weeks
            "$w ${if (w == 1L) "week" else "weeks"} ago"
        }
        months < 12 -> {
            val m = if (months < 1) 1 else months
            "$m ${if (m == 1L) "month" else "months"} ago"
        }
        else -> {
            val y = if (years < 1) 1 else years
            "$y ${if (y == 1L) "year" else "years"} ago"
        }
    }
}

/**
 * A Flow that emits a formatted date string every second.
 * The format is updated based on the time elapsed since the date.
 * This is useful for displaying relative time in a UI.
 */
fun relativeTimeFlow(date: Date): Flow<String> = flow {
    while (true) {
        emit(formatDateTime(date))
        // compute next delay based on how old the timestamp is
        val diffInMillis = Date().time - date.time
        val nextDelay = when {
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> TimeUnit.SECONDS.toMillis(1)
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> TimeUnit.MINUTES.toMillis(1)
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> TimeUnit.HOURS.toMillis(1)
            else -> TimeUnit.DAYS.toMillis(1)
        }
        delay(nextDelay)
    }
}