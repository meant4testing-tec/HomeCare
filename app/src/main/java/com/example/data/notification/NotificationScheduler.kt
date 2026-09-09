package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.DateUtils
import com.example.data.model.MaintenanceSchedule
import com.example.data.model.ScheduleWithAsset

object NotificationScheduler {

    const val ACTION_TRIGGER_REMINDER = "com.example.ACTION_TRIGGER_REMINDER"
    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    const val EXTRA_TASK_NAME = "extra_task_name"
    const val EXTRA_ASSET_NAME = "extra_asset_name"
    const val EXTRA_DUE_DATE = "extra_due_date"

    fun getNotificationId(scheduleId: String): Int {
        return (scheduleId.hashCode() and 0x7FFFFFFF)
    }

    fun scheduleReminder(
        context: Context,
        schedule: MaintenanceSchedule,
        assetName: String
    ) {
        if (!schedule.remindersEnabled) {
            cancelReminder(context, schedule.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val notificationId = getNotificationId(schedule.id)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(EXTRA_TASK_NAME, schedule.taskName)
            putExtra(EXTRA_ASSET_NAME, assetName)
            putExtra(EXTRA_DUE_DATE, schedule.nextDueDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val reminderOffsetMillis = schedule.reminderPeriodDays * 24L * 60 * 60 * 1000
        val targetTriggerTime = schedule.nextDueDate - reminderOffsetMillis

        if (targetTriggerTime <= now) {
            // Target reminder time has arrived or passed
            if (schedule.nextDueDate < now) {
                // Overdue
                val overdueDays = ((now - schedule.nextDueDate) / (24L * 60 * 60 * 1000)).toInt()
                val overdueText = if (overdueDays <= 1) "due yesterday" else "due $overdueDays days ago"
                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notificationId,
                    title = "${schedule.taskName} is overdue",
                    message = "$assetName: Maintenance was $overdueText (${DateUtils.formatDate(schedule.nextDueDate)}).",
                    taskId = schedule.id
                )
            } else {
                // Due today or in reminder window
                val daysLeft = ((schedule.nextDueDate - now) / (24L * 60 * 60 * 1000)).toInt()
                val dueText = if (daysLeft <= 0) "is due today" else "is due in $daysLeft days"
                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notificationId,
                    title = "${schedule.taskName} $dueText",
                    message = "$assetName: Scheduled service date is ${DateUtils.formatDate(schedule.nextDueDate)}.",
                    taskId = schedule.id
                )
            }
            return
        }

        // Future alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetTriggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // AlarmManager permission restrictions
        }
    }

    fun cancelReminder(context: Context, scheduleId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val notificationId = getNotificationId(scheduleId)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        NotificationHelper.cancelNotification(context, notificationId)
    }

    fun reconcileAllReminders(
        context: Context,
        schedules: List<ScheduleWithAsset>
    ) {
        for (item in schedules) {
            if (item.schedule.remindersEnabled) {
                scheduleReminder(context, item.schedule, item.asset.name)
            } else {
                cancelReminder(context, item.schedule.id)
            }
        }
    }
}
