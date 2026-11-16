package com.sumanonline.hisabkhata.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val isoFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
    private val displayDateFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    private val displayDateTimeFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
    private val timeOnlyFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    @Volatile
    var activeDateFormat: String = "dd MMM yyyy"

    fun todayIso(): String {
        return isoFormatThreadLocal.get()?.format(Date()) ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun formatIsoDate(date: Date): String {
        return isoFormatThreadLocal.get()?.format(date) ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    fun formatDisplayDate(timestamp: Long): String {
        return try {
            SimpleDateFormat(activeDateFormat, Locale.getDefault()).format(Date(timestamp))
        } catch (_: Exception) {
            displayDateFormatThreadLocal.get()?.format(Date(timestamp)) ?: ""
        }
    }

    fun formatDisplayDateTime(timestamp: Long): String {
        return displayDateTimeFormatThreadLocal.get()?.format(Date(timestamp)) ?: ""
    }

    fun formatDisplayDateTime(dateIso: String?, timestamp: Long): String {
        val resolved = getResolvedTimestamp(dateIso, timestamp)
        return displayDateTimeFormatThreadLocal.get()?.format(Date(resolved)) ?: ""
    }

    fun formatEntryDetailsDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatEntryDetailsDateTime(dateIso: String?, timestamp: Long): String {
        val resolved = getResolvedTimestamp(dateIso, timestamp)
        val sdf = SimpleDateFormat("dd MMM yy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(resolved))
    }

    fun formatDatePill(dateIso: String, timestamp: Long): String {
        val date = parseIsoDate(dateIso) ?: Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())
        val formattedDate = sdf.format(date).uppercase()
        val relTime = formatRelativeTime(date.time).uppercase()
        return "$formattedDate • $relTime"
    }

    fun formatTime(timestamp: Long): String {
        return timeOnlyFormatThreadLocal.get()?.format(Date(timestamp)) ?: ""
    }

    fun getResolvedTimestamp(dateIso: String?, timestamp: Long): Long {
        if (dateIso.isNullOrBlank()) {
            return if (timestamp > 0L) timestamp else System.currentTimeMillis()
        }
        val dateFromIso = parseIsoDate(dateIso.trim()) ?: return if (timestamp > 0L) timestamp else System.currentTimeMillis()
        val targetCal = Calendar.getInstance().apply { time = dateFromIso }
        val baseCal = Calendar.getInstance().apply {
            timeInMillis = if (timestamp > 0L) timestamp else System.currentTimeMillis()
        }

        val sameDay = targetCal.get(Calendar.YEAR) == baseCal.get(Calendar.YEAR) &&
                targetCal.get(Calendar.DAY_OF_YEAR) == baseCal.get(Calendar.DAY_OF_YEAR)

        if (sameDay && timestamp > 0L) {
            return baseCal.timeInMillis
        }

        targetCal.set(Calendar.HOUR_OF_DAY, baseCal.get(Calendar.HOUR_OF_DAY))
        targetCal.set(Calendar.MINUTE, baseCal.get(Calendar.MINUTE))
        targetCal.set(Calendar.SECOND, baseCal.get(Calendar.SECOND))
        targetCal.set(Calendar.MILLISECOND, baseCal.get(Calendar.MILLISECOND))

        return targetCal.timeInMillis
    }

    fun parseIsoDate(isoString: String): Date? {
        val clean = isoString.trim().take(10)
        return try {
            isoFormatThreadLocal.get()?.parse(clean)
        } catch (_: Exception) {
            null
        }
    }

    fun addDaysToToday(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return isoFormatThreadLocal.get()?.format(calendar.time) ?: ""
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
        val now = System.currentTimeMillis()
        val timeZone = TimeZone.getDefault()
        val offset = timeZone.getOffset(now)
        val todayDayNumber = (now + offset) / (24 * 60 * 60 * 1000L)
        val dueDayNumber = (due.time + offset) / (24 * 60 * 60 * 1000L)
        val diffDays = (dueDayNumber - todayDayNumber).toInt()

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
