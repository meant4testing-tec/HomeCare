package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

data class AppSettingsState(
    val currencySymbol: String = "₹",
    val defaultReminderDays: Int = 3,
    val notificationsEnabled: Boolean = true,
    val isGoogleSyncEnabled: Boolean = false,
    val syncStatusText: String = "Local-Only Mode",
    val lastSyncTimestamp: Long? = null,
    val signedInEmail: String? = null
)

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("home_maintenance_prefs", Context.MODE_PRIVATE)

    fun getSettings(): AppSettingsState {
        val currency = prefs.getString("currency", "₹") ?: "₹"
        val reminderDays = prefs.getInt("reminder_days", 3)
        val notif = prefs.getBoolean("notif_enabled", true)
        val sync = prefs.getBoolean("sync_enabled", false)
        val lastSync = prefs.getLong("last_sync", 0L).takeIf { it > 0 }
        val email = prefs.getString("user_email", null)
        val statusText = if (sync) {
            if (lastSync != null) "Synced ${DateUtils.formatShortDate(lastSync)}" else "Syncing..."
        } else {
            "Local-Only Mode"
        }

        return AppSettingsState(
            currencySymbol = currency,
            defaultReminderDays = reminderDays,
            notificationsEnabled = notif,
            isGoogleSyncEnabled = sync,
            syncStatusText = statusText,
            lastSyncTimestamp = lastSync,
            signedInEmail = email
        )
    }

    fun setCurrency(symbol: String) {
        prefs.edit().putString("currency", symbol).apply()
    }

    fun setDefaultReminderDays(days: Int) {
        prefs.edit().putInt("reminder_days", days).apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
    }

    fun setGoogleSync(enabled: Boolean, email: String? = null) {
        prefs.edit()
            .putBoolean("sync_enabled", enabled)
            .putLong("last_sync", if (enabled) System.currentTimeMillis() else 0L)
            .putString("user_email", if (enabled) (email ?: "meant4testing@gmail.com") else null)
            .apply()
    }

    fun markSyncedNow() {
        prefs.edit().putLong("last_sync", System.currentTimeMillis()).apply()
    }
}
