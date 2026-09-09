package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.HomeMaintenanceDatabase
import com.example.data.model.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannel(context)

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = HomeMaintenanceDatabase.getInstance(context)
                        val dao = db.dao()
                        val schedules = dao.getAllSchedulesWithAssetDirect()
                        NotificationScheduler.reconcileAllReminders(context, schedules)
                    } catch (_: Exception) {
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            NotificationScheduler.ACTION_TRIGGER_REMINDER -> {
                val scheduleId = intent.getStringExtra(NotificationScheduler.EXTRA_SCHEDULE_ID) ?: return
                val taskName = intent.getStringExtra(NotificationScheduler.EXTRA_TASK_NAME) ?: "Maintenance Task"
                val assetName = intent.getStringExtra(NotificationScheduler.EXTRA_ASSET_NAME) ?: "Household Asset"
                val dueDate = intent.getLongExtra(NotificationScheduler.EXTRA_DUE_DATE, System.currentTimeMillis())

                val now = System.currentTimeMillis()
                val isOverdue = now > dueDate
                val notificationId = NotificationScheduler.getNotificationId(scheduleId)

                val (title, message) = if (isOverdue) {
                    val overdueDays = ((now - dueDate) / (24L * 60 * 60 * 1000)).toInt()
                    val overdueText = if (overdueDays <= 1) "due yesterday" else "due $overdueDays days ago"
                    "$taskName is overdue" to "$assetName: Scheduled maintenance was $overdueText (${DateUtils.formatDate(dueDate)}). Tap to complete."
                } else {
                    val daysLeft = ((dueDate - now) / (24L * 60 * 60 * 1000)).toInt()
                    val dueText = if (daysLeft <= 0) "is due today" else "is due in $daysLeft days"
                    "$taskName $dueText" to "$assetName: Due on ${DateUtils.formatDate(dueDate)}. Tap to view or complete."
                }

                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    message = message,
                    taskId = scheduleId
                )
            }
        }
    }
}
