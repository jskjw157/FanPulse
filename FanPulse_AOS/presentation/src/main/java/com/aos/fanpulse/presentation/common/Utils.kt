package com.aos.fanpulse.presentation.common

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Date
import java.util.TimeZone

fun Long.toRelativeTime(): String {
    val currentTime = System.currentTimeMillis()
    val diffTime = (currentTime - this) / 1000

    return when {
        diffTime < 0 -> "방금 전"
        diffTime < 60 -> "방금 전"
        diffTime < 60 * 60 -> "${diffTime / 60}분 전"
        diffTime < 60 * 60 * 24 -> "${diffTime / (60 * 60)}시간 전"
        diffTime < 60 * 60 * 24 * 7 -> "${diffTime / (60 * 60 * 24)}일 전"
        diffTime < 60 * 60 * 24 * 30 -> "${diffTime / (60 * 60 * 24 * 7)}주 전"
        else -> {
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

fun String.formatIsoTimeToEnglish(): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val zonedDateTime = Instant.parse(this).atZone(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            zonedDateTime.format(formatter)
        } else {
            val truncatedString = if (this.length > 24) this.substring(0, 23) + "Z" else this

            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(truncatedString) ?: return ""

            val formatter = SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm a", Locale.ENGLISH)
            formatter.format(date)
        }
    } catch (e: Exception) {
        ""
    }
}