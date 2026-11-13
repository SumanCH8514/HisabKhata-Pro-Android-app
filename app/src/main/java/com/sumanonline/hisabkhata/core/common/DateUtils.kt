package com.sumanonline.hisabkhata.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val timeOnlyFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    @Volatile
    var activeDateFormat: String = "dd MMM yyyy"

    fun todayIso(): String {
        return isoDateFormat.format(Date())
    }

    fun formatIsoDate(date: Date): String {
        return isoDateFormat.format(date)
    }

    fun formatDisplayDate(timestamp: Long): String {
        return try {
            SimpleDateFormat(activeDateFormat, Locale.getDefault()).format(Date(timestamp))
        } catch (_: Exception) {
            displayDateFormat.format(Date(timestamp))
        }
    }

    fun formatDisplayDateTime(timestamp: Long): String {
        return displayDateTimeFormat.format(Date(timestamp))
    }

    fun formatEntryDetailsDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDatePill(dateIso: String, timestamp: Long): String {
        val date = parseIsoDate(dateIso) ?: Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())
        val formattedDate = sdf.format(date).uppercase()
        val relTime = formatRelativeTime(date.time).uppercase()
        return "$formattedDate • $relTime"
    }

    fun formatTime(timestamp: Long): String {
        return timeOnlyFormat.format(Date(timestamp))
    }

    fun parseIsoDate(isoString: String): Date? {
        return try {
            isoDateFormat.parse(isoString)
        } catch (_: Exception) {
            null
        }
    }

    fun addDaysToToday(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return isoDateFormat.format(calendar.time)
    }

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Recently"
        val diff = System.currentTimeMillis() - timestamp
        if (diff < 0L) return "Recently"
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)
        val months = days / 30
        val years = days / 365

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 30 -> "$days ${if (days == 1L) "day" else "days"} ago"
            months < 12 -> "$months ${if (months == 1L) "month" else "months"} ago"
            else -> "$years ${if (years == 1L) "year" else "years"} ago"
        }
    }

    fun getDueDateStatus(dueDateIso: String?): DueStatus {
        if (dueDateIso.isNullOrBlank()) return DueStatus.None
        val due = parseIsoDate(dueDateIso) ?: return DueStatus.None
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dueCalendar = Calendar.getInstance().apply {
            time = due
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = dueCalendar.timeInMillis - todayCalendar.timeInMillis
        val diffDays = (diffMs / (24 * 60 * 60 * 1000)).toInt()

        return when {
            diffDays < 0 -> DueStatus.Overdue(Math.abs(diffDays))
            diffDays == 0 -> DueStatus.DueToday
            diffDays <= 7 -> DueStatus.DueSoon(diffDays)
            else -> DueStatus.Upcoming(diffDays)
        }
    }
}

sealed interface DueStatus {
    data object None : DueStatus
    data object DueToday : DueStatus
    data class Overdue(val days: Int) : DueStatus
    data class DueSoon(val days: Int) : DueStatus
    data class Upcoming(val days: Int) : DueStatus
}
