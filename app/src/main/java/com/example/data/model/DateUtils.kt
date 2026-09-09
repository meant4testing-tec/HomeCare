package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TaskUrgency {
    OVERDUE,
    DUE_TODAY,
    DUE_SOON, // next 7-14 days
    UPCOMING  // further out
}

data class WarrantyInfo(
    val status: String,
    val isExpired: Boolean,
    val isExpiringSoon: Boolean,
    val daysRemaining: Long?
)

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatDate(millis: Long?): String {
        if (millis == null || millis <= 0) return "Not recorded"
        return standardDateFormat.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        return monthYearFormat.format(Date(millis))
    }

    fun getStartOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getEndOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getUrgency(dueDateMillis: Long): TaskUrgency {
        val todayStart = getStartOfDay()
        val todayEnd = getEndOfDay()

        return when {
            dueDateMillis < todayStart -> TaskUrgency.OVERDUE
            dueDateMillis in todayStart..todayEnd -> TaskUrgency.DUE_TODAY
            dueDateMillis <= todayStart + TimeUnit.DAYS.toMillis(14) -> TaskUrgency.DUE_SOON
            else -> TaskUrgency.UPCOMING
        }
    }

    fun formatRelativeDue(dueDateMillis: Long): String {
        val todayStart = getStartOfDay()
        val dueStart = getStartOfDay(dueDateMillis)
        val diffDays = (dueStart - todayStart) / (1000 * 60 * 60 * 24)

        return when {
            diffDays < -1 -> "Overdue by ${-diffDays} days"
            diffDays == -1L -> "Overdue by 1 day"
            diffDays == 0L -> "Due Today"
            diffDays == 1L -> "Due Tomorrow"
            diffDays in 2..14 -> "Due in $diffDays days"
            diffDays > 14 -> "Due in ${diffDays / 30 + 1} months (${formatDate(dueDateMillis)})"
            else -> formatDate(dueDateMillis)
        }
    }

    fun calculateNextDueDate(
        completedDateMillis: Long,
        frequencyType: String,
        frequencyValue: Int
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = completedDateMillis
        }
        val safeValue = if (frequencyValue <= 0) 1 else frequencyValue
        when (frequencyType.uppercase(Locale.ROOT)) {
            "DAYS" -> cal.add(Calendar.DAY_OF_YEAR, safeValue)
            "WEEKS" -> cal.add(Calendar.WEEK_OF_YEAR, safeValue)
            "MONTHS" -> cal.add(Calendar.MONTH, safeValue)
            "YEARS" -> cal.add(Calendar.YEAR, safeValue)
            "ONE_TIME" -> return completedDateMillis
            else -> cal.add(Calendar.MONTH, safeValue)
        }
        return cal.timeInMillis
    }

    fun frequencyDescription(frequencyType: String, frequencyValue: Int, condition: String?): String {
        val base = when (frequencyType.uppercase(Locale.ROOT)) {
            "DAYS" -> if (frequencyValue == 1) "Daily" else "Every $frequencyValue days"
            "WEEKS" -> if (frequencyValue == 1) "Weekly" else "Every $frequencyValue weeks"
            "MONTHS" -> when (frequencyValue) {
                1 -> "Monthly"
                3 -> "Quarterly (Every 3 months)"
                6 -> "Half-Yearly (Every 6 months)"
                12 -> "Yearly"
                else -> "Every $frequencyValue months"
            }
            "YEARS" -> if (frequencyValue == 1) "Yearly" else "Every $frequencyValue years"
            "ONE_TIME" -> "One-time"
            else -> "Every $frequencyValue $frequencyType"
        }
        return if (!condition.isNullOrBlank()) "$base $condition" else base
    }

    fun getWarrantyInfo(expiryDateMillis: Long?): WarrantyInfo {
        if (expiryDateMillis == null || expiryDateMillis <= 0) {
            return WarrantyInfo(status = "No warranty recorded", isExpired = false, isExpiringSoon = false, daysRemaining = null)
        }
        val today = getStartOfDay()
        val expiryDay = getStartOfDay(expiryDateMillis)
        val diffDays = (expiryDay - today) / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 0 -> WarrantyInfo(
                status = "Warranty expired ${-diffDays} days ago",
                isExpired = true,
                isExpiringSoon = false,
                daysRemaining = 0
            )
            diffDays == 0L -> WarrantyInfo(
                status = "Warranty expires today!",
                isExpired = false,
                isExpiringSoon = true,
                daysRemaining = 0
            )
            diffDays in 1..30 -> WarrantyInfo(
                status = "Warranty expires in $diffDays days",
                isExpired = false,
                isExpiringSoon = true,
                daysRemaining = diffDays
            )
            else -> WarrantyInfo(
                status = "Warranty active ($diffDays days left)",
                isExpired = false,
                isExpiringSoon = false,
                daysRemaining = diffDays
            )
        }
    }
}
