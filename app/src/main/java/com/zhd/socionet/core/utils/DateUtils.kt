package com.zhd.socionet.core.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatTimestamp(timestamp: Timestamp, pattern: String = "dd MMM yyyy, hh:mm a"): String {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    fun getCurrentMonthName(): String {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getRelativeTime(timestamp: Timestamp): String {
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> formatTimestamp(timestamp, "dd MMM")
        }
    }
}