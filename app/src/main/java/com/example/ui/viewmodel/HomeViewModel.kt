package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.HomeMaintenanceDatabase
import com.example.data.model.AppSettingsManager
import com.example.data.model.AppSettingsState
import com.example.data.model.Asset
import com.example.data.model.AssetWithDetails
import com.example.data.model.DateUtils
import com.example.data.model.LogWithAsset
import com.example.data.model.MaintenanceLog
import com.example.data.model.MaintenanceSchedule
import com.example.data.model.ScheduleTemplate
import com.example.data.model.ScheduleWithAsset
import com.example.data.model.TaskUrgency
import com.example.data.notification.NotificationHelper
import com.example.data.notification.NotificationScheduler
import com.example.data.repository.HomeMaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class AppNavDestination {
    DASHBOARD,
    ASSETS,
    CALENDAR,
    HISTORY,
    SETTINGS
}

data class DashboardState(
    val overdue: List<ScheduleWithAsset> = emptyList(),
    val dueToday: List<ScheduleWithAsset> = emptyList(),
    val dueSoon: List<ScheduleWithAsset> = emptyList(),
    val upcoming: List<ScheduleWithAsset> = emptyList(),
    val recentlyCompleted: List<LogWithAsset> = emptyList(),
    val expiringWarranties: List<Asset> = emptyList()
)

data class ExpensesSummary(
    val totalAllTime: Double = 0.0,
    val totalThisYear: Double = 0.0,
    val totalThisMonth: Double = 0.0,
    val completedCount: Int = 0,
    val categoryBreakdown: Map<String, Double> = emptyMap(),
    val assetBreakdown: Map<String, Double> = emptyMap()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeMaintenanceRepository
    private val settingsManager: AppSettingsManager = AppSettingsManager(application)

    private val _currentDestination = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    private val _selectedAssetId = MutableStateFlow<String?>(null)
    val selectedAssetId: StateFlow<String?> = _selectedAssetId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _assetCategoryFilter = MutableStateFlow("All")
    val assetCategoryFilter: StateFlow<String> = _assetCategoryFilter.asStateFlow()

    private val _calendarSelectedDate = MutableStateFlow(DateUtils.getStartOfDay())
    val calendarSelectedDate: StateFlow<Long> = _calendarSelectedDate.asStateFlow()

    private val _settingsState = MutableStateFlow(settingsManager.getSettings())
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    // Sheet / Modal states
    private val _showQuickAddModal = MutableStateFlow(false)
    val showQuickAddModal: StateFlow<Boolean> = _showQuickAddModal.asStateFlow()

    private val _showAddAssetSheet = MutableStateFlow(false)
    val showAddAssetSheet: StateFlow<Boolean> = _showAddAssetSheet.asStateFlow()

    private val _editingAsset = MutableStateFlow<Asset?>(null)
    val editingAsset: StateFlow<Asset?> = _editingAsset.asStateFlow()

    private val _showAddScheduleSheet = MutableStateFlow(false)
    val showAddScheduleSheet: StateFlow<Boolean> = _showAddScheduleSheet.asStateFlow()

    private val _scheduleTargetAssetId = MutableStateFlow<String?>(null)
    val scheduleTargetAssetId: StateFlow<String?> = _scheduleTargetAssetId.asStateFlow()

    private val _editingSchedule = MutableStateFlow<MaintenanceSchedule?>(null)
    val editingSchedule: StateFlow<MaintenanceSchedule?> = _editingSchedule.asStateFlow()

    private val _completingTask = MutableStateFlow<ScheduleWithAsset?>(null)
    val completingTask: StateFlow<ScheduleWithAsset?> = _completingTask.asStateFlow()

    private val _showQuickLogSheet = MutableStateFlow(false)
    val showQuickLogSheet: StateFlow<Boolean> = _showQuickLogSheet.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val allAssets: StateFlow<List<Asset>>
    val allSchedulesWithAsset: StateFlow<List<ScheduleWithAsset>>
    val allLogsWithAsset: StateFlow<List<LogWithAsset>>
    val dashboardState: StateFlow<DashboardState>
    val expensesSummary: StateFlow<ExpensesSummary>
    val nextScheduledReminder: StateFlow<ScheduleWithAsset?>
    val activeScheduledRemindersCount: StateFlow<Int>

    init {
        val db = HomeMaintenanceDatabase.getInstance(application)
        repository = HomeMaintenanceRepository(db.dao())

        allAssets = repository.allAssets
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allSchedulesWithAsset = repository.allSchedulesWithAsset
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allLogsWithAsset = repository.allLogsWithAsset
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        nextScheduledReminder = allSchedulesWithAsset.map { list ->
            list.filter { it.schedule.remindersEnabled }
                .minByOrNull { it.schedule.nextDueDate }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        activeScheduledRemindersCount = allSchedulesWithAsset.map { list ->
            list.count { it.schedule.remindersEnabled }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        // Combine for Dashboard state
        dashboardState = combine(allSchedulesWithAsset, allLogsWithAsset, allAssets) { schedules, logs, assets ->
            val overdue = mutableListOf<ScheduleWithAsset>()
            val dueToday = mutableListOf<ScheduleWithAsset>()
            val dueSoon = mutableListOf<ScheduleWithAsset>()
            val upcoming = mutableListOf<ScheduleWithAsset>()

            schedules.forEach { item ->
                when (DateUtils.getUrgency(item.schedule.nextDueDate)) {
                    TaskUrgency.OVERDUE -> overdue.add(item)
                    TaskUrgency.DUE_TODAY -> dueToday.add(item)
                    TaskUrgency.DUE_SOON -> dueSoon.add(item)
                    TaskUrgency.UPCOMING -> upcoming.add(item)
                }
            }

            val expiringWarranties = assets.filter {
                val info = DateUtils.getWarrantyInfo(it.warrantyExpiryDate)
                info.isExpiringSoon || (it.warrantyExpiryDate != null && it.warrantyExpiryDate > System.currentTimeMillis() && it.warrantyExpiryDate < System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000)
            }

            DashboardState(
                overdue = overdue,
                dueToday = dueToday,
                dueSoon = dueSoon,
                upcoming = upcoming,
                recentlyCompleted = logs.take(5),
                expiringWarranties = expiringWarranties
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

        // Combine for Expenses and Financial Summary
        expensesSummary = combine(allLogsWithAsset, allAssets) { logs, assets ->
            var totalAll = 0.0
            var totalYear = 0.0
            var totalMonth = 0.0

            val currentCal = Calendar.getInstance()
            val currentYear = currentCal.get(Calendar.YEAR)
            val currentMonth = currentCal.get(Calendar.MONTH)

            val categoryMap = mutableMapOf<String, Double>()
            val assetMap = mutableMapOf<String, Double>()

            val logCal = Calendar.getInstance()
            logs.forEach { item ->
                val cost = item.log.cost ?: 0.0
                totalAll += cost

                logCal.timeInMillis = item.log.completedDate
                if (logCal.get(Calendar.YEAR) == currentYear) {
                    totalYear += cost
                    if (logCal.get(Calendar.MONTH) == currentMonth) {
                        totalMonth += cost
                    }
                }

                val cat = item.asset.category
                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + cost

                val assetName = item.asset.name
                assetMap[assetName] = (assetMap[assetName] ?: 0.0) + cost
            }

            ExpensesSummary(
                totalAllTime = totalAll,
                totalThisYear = totalYear,
                totalThisMonth = totalMonth,
                completedCount = logs.size,
                categoryBreakdown = categoryMap,
                assetBreakdown = assetMap
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpensesSummary())

        // Preload starter data on fresh install if empty and reconcile notifications
        viewModelScope.launch {
            if (allAssets.value.isEmpty()) {
                repository.populateStarterDataIfNeeded()
            }
            allSchedulesWithAsset.collect { schedules ->
                if (schedules.isNotEmpty()) {
                    NotificationScheduler.reconcileAllReminders(application, schedules)
                }
            }
        }
    }

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
        _selectedAssetId.value = null
    }

    fun selectAsset(assetId: String?) {
        _selectedAssetId.value = assetId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun setCategoryFilter(category: String) {
        _assetCategoryFilter.value = category
    }

    fun setCalendarDate(dateMillis: Long) {
        _calendarSelectedDate.value = DateUtils.getStartOfDay(dateMillis)
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    // Modal triggers
    fun openQuickAddModal() {
        _showQuickAddModal.value = true
    }

    fun closeQuickAddModal() {
        _showQuickAddModal.value = false
    }

    fun openAddAsset(assetToEdit: Asset? = null) {
        _editingAsset.value = assetToEdit
        _showAddAssetSheet.value = true
        _showQuickAddModal.value = false
    }

    fun closeAddAsset() {
        _showAddAssetSheet.value = false
        _editingAsset.value = null
    }

    fun openAddSchedule(targetAssetId: String? = null, scheduleToEdit: MaintenanceSchedule? = null) {
        _scheduleTargetAssetId.value = targetAssetId ?: _selectedAssetId.value
        _editingSchedule.value = scheduleToEdit
        _showAddScheduleSheet.value = true
        _showQuickAddModal.value = false
    }

    fun closeAddSchedule() {
        _showAddScheduleSheet.value = false
        _editingSchedule.value = null
        _scheduleTargetAssetId.value = null
    }

    fun openCompleteTask(task: ScheduleWithAsset) {
        _completingTask.value = task
    }

    fun closeCompleteTask() {
        _completingTask.value = null
    }

    fun openQuickLogSheet() {
        _showQuickLogSheet.value = true
        _showQuickAddModal.value = false
    }

    fun closeQuickLogSheet() {
        _showQuickLogSheet.value = false
    }

    // Asset CRUD
    fun saveAsset(
        name: String,
        category: String,
        brandModel: String?,
        serialNumber: String?,
        purchaseDate: Long?,
        purchasePrice: Double?,
        location: String?,
        warrantyMonths: Int?,
        warrantyExpiry: Long?,
        invoiceNumber: String?,
        warrantyNotes: String?,
        notes: String?,
        suggestedSchedules: List<ScheduleTemplate> = emptyList()
    ) {
        viewModelScope.launch {
            val existing = _editingAsset.value
            val assetId = existing?.id ?: UUID.randomUUID().toString()
            val asset = Asset(
                id = assetId,
                name = name.trim(),
                category = category,
                brandModel = brandModel?.trim()?.takeIf { it.isNotBlank() },
                serialNumber = serialNumber?.trim()?.takeIf { it.isNotBlank() },
                purchaseDate = purchaseDate,
                purchasePrice = purchasePrice,
                location = location?.trim()?.takeIf { it.isNotBlank() },
                warrantyDurationMonths = warrantyMonths,
                warrantyExpiryDate = warrantyExpiry,
                invoiceNumber = invoiceNumber?.trim()?.takeIf { it.isNotBlank() },
                warrantyNotes = warrantyNotes?.trim()?.takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (existing == null) {
                repository.insertAsset(asset)
                // If suggested schedules were selected from template, create them
                suggestedSchedules.forEach { template ->
                    val nextDue = DateUtils.calculateNextDueDate(
                        System.currentTimeMillis(),
                        template.frequencyType,
                        template.frequencyValue
                    )
                    val schedule = MaintenanceSchedule(
                        id = UUID.randomUUID().toString(),
                        assetId = assetId,
                        taskName = template.taskName,
                        frequencyType = template.frequencyType,
                        frequencyValue = template.frequencyValue,
                        conditionValue = template.conditionValue,
                        nextDueDate = nextDue,
                        estimatedCost = template.estimatedCost,
                        notes = template.notes
                    )
                    repository.insertSchedule(schedule)
                    if (schedule.remindersEnabled) {
                        NotificationScheduler.scheduleReminder(getApplication(), schedule, asset.name)
                    }
                }
                showSnackbar("Asset \"${asset.name}\" created successfully")
            } else {
                repository.updateAsset(asset)
                showSnackbar("Asset \"${asset.name}\" updated")
            }
            closeAddAsset()
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            allSchedulesWithAsset.value.filter { it.schedule.assetId == asset.id }.forEach { item ->
                NotificationScheduler.cancelReminder(getApplication(), item.schedule.id)
            }
            repository.deleteAsset(asset)
            if (_selectedAssetId.value == asset.id) {
                _selectedAssetId.value = null
            }
            showSnackbar("Asset \"${asset.name}\" deleted")
        }
    }

    // Schedule CRUD
    fun saveSchedule(
        assetId: String,
        taskName: String,
        frequencyType: String,
        frequencyValue: Int,
        conditionValue: String?,
        nextDueDate: Long,
        reminderPeriodDays: Int,
        estimatedCost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val existing = _editingSchedule.value
            val schedule = MaintenanceSchedule(
                id = existing?.id ?: UUID.randomUUID().toString(),
                assetId = assetId,
                taskName = taskName.trim(),
                frequencyType = frequencyType,
                frequencyValue = frequencyValue,
                conditionValue = conditionValue?.trim()?.takeIf { it.isNotBlank() },
                lastCompletedDate = existing?.lastCompletedDate,
                nextDueDate = nextDueDate,
                reminderPeriodDays = reminderPeriodDays,
                remindersEnabled = existing?.remindersEnabled ?: true,
                estimatedCost = estimatedCost,
                defaultServiceProvider = serviceProvider?.trim()?.takeIf { it.isNotBlank() },
                defaultServicePhone = servicePhone?.trim()?.takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val assetName = allAssets.value.find { it.id == assetId }?.name ?: "Household item"
            if (existing == null) {
                repository.insertSchedule(schedule)
                if (schedule.remindersEnabled) {
                    NotificationScheduler.scheduleReminder(getApplication(), schedule, assetName)
                }
                showSnackbar("Maintenance rule \"$taskName\" added")
            } else {
                repository.updateSchedule(schedule)
                if (schedule.remindersEnabled) {
                    NotificationScheduler.scheduleReminder(getApplication(), schedule, assetName)
                } else {
                    NotificationScheduler.cancelReminder(getApplication(), schedule.id)
                }
                showSnackbar("Maintenance rule updated")
            }
            closeAddSchedule()
        }
    }

    fun toggleScheduleReminder(schedule: MaintenanceSchedule) {
        viewModelScope.launch {
            val updated = schedule.copy(
                remindersEnabled = !schedule.remindersEnabled,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateSchedule(updated)
            val assetName = allAssets.value.find { it.id == schedule.assetId }?.name ?: "Household item"
            if (updated.remindersEnabled) {
                NotificationScheduler.scheduleReminder(getApplication(), updated, assetName)
            } else {
                NotificationScheduler.cancelReminder(getApplication(), updated.id)
            }
            val status = if (updated.remindersEnabled) "enabled" else "disabled"
            showSnackbar("Reminders $status for \"${schedule.taskName}\"")
        }
    }

    fun deleteSchedule(schedule: MaintenanceSchedule) {
        viewModelScope.launch {
            NotificationScheduler.cancelReminder(getApplication(), schedule.id)
            repository.deleteSchedule(schedule)
            showSnackbar("Maintenance task deleted")
        }
    }

    // Task Completion
    fun completeScheduledTask(
        task: ScheduleWithAsset,
        completedDate: Long,
        cost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        meterReading: String?,
        notes: String?,
        nextDueDateOverride: Long?
    ) {
        viewModelScope.launch {
            NotificationScheduler.cancelReminder(getApplication(), task.schedule.id)
            repository.completeTask(
                schedule = task.schedule,
                completedDate = completedDate,
                cost = cost,
                serviceProvider = serviceProvider,
                servicePhone = servicePhone,
                meterReading = meterReading,
                notes = notes,
                nextDueDateOverride = nextDueDateOverride
            )

            if (task.schedule.frequencyType != "ONE_TIME" && task.schedule.remindersEnabled) {
                val nextDue = nextDueDateOverride ?: DateUtils.calculateNextDueDate(
                    completedDate,
                    task.schedule.frequencyType,
                    task.schedule.frequencyValue
                )
                val updatedSchedule = task.schedule.copy(
                    lastCompletedDate = completedDate,
                    nextDueDate = nextDue,
                    updatedAt = System.currentTimeMillis()
                )
                NotificationScheduler.scheduleReminder(getApplication(), updatedSchedule, task.asset.name)
            }

            closeCompleteTask()
            showSnackbar("Marked \"${task.schedule.taskName}\" as completed!")
        }
    }

    // Quick direct log
    fun logPastService(
        assetId: String,
        taskName: String,
        completedDate: Long,
        cost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        meterReading: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val log = MaintenanceLog(
                id = UUID.randomUUID().toString(),
                assetId = assetId,
                taskName = taskName.trim(),
                completedDate = completedDate,
                cost = cost,
                serviceProvider = serviceProvider?.trim()?.takeIf { it.isNotBlank() },
                servicePhone = servicePhone?.trim()?.takeIf { it.isNotBlank() },
                meterReading = meterReading?.trim()?.takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() }
            )
            repository.insertLog(log)
            closeQuickLogSheet()
            showSnackbar("Service record saved to history")
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch {
            repository.deleteLogById(logId)
            showSnackbar("Historical log removed")
        }
    }

    // Settings
    fun setCurrency(symbol: String) {
        settingsManager.setCurrency(symbol)
        _settingsState.value = settingsManager.getSettings()
        showSnackbar("Currency set to $symbol")
    }

    fun setDefaultReminderDays(days: Int) {
        settingsManager.setDefaultReminderDays(days)
        _settingsState.value = settingsManager.getSettings()
        showSnackbar("Default reminder updated")
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
        _settingsState.value = settingsManager.getSettings()
        showSnackbar(if (enabled) "Notifications enabled" else "Notifications muted")
    }

    fun toggleGoogleSync(enable: Boolean) {
        settingsManager.setGoogleSync(enable)
        _settingsState.value = settingsManager.getSettings()
        if (enable) {
            showSnackbar("Google sync connected. Synchronizing offline-first...")
        } else {
            showSnackbar("Switched to Local-Only mode. All data remains securely on this device.")
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            settingsManager.markSyncedNow()
            _settingsState.value = settingsManager.getSettings()
            showSnackbar("Synced just now with Google Cloud backup")
        }
    }

    fun loadStarterData() {
        viewModelScope.launch {
            repository.populateStarterDataIfNeeded()
            val schedules = repository.allSchedulesWithAsset.stateIn(viewModelScope).value
            NotificationScheduler.reconcileAllReminders(getApplication(), schedules)
            showSnackbar("Sample household maintenance items loaded!")
        }
    }

    suspend fun exportDataJson(): String {
        return repository.exportDataJson(
            allAssetsList = allAssets.value,
            schedulesList = allSchedulesWithAsset.value,
            logsList = allLogsWithAsset.value
        )
    }

    suspend fun restoreDataJson(json: String): Boolean {
        val success = repository.restoreDataJson(json)
        if (success) {
            val schedules = repository.allSchedulesWithAsset.stateIn(viewModelScope).value
            NotificationScheduler.reconcileAllReminders(getApplication(), schedules)
            showSnackbar("Data backup restored successfully!")
        } else {
            showSnackbar("Failed to restore backup: Invalid JSON format")
        }
        return success
    }

    fun reconcileReminders() {
        NotificationScheduler.reconcileAllReminders(getApplication(), allSchedulesWithAsset.value)
    }

    fun sendTestNotification(title: String, message: String) {
        NotificationHelper.showReminderNotification(
            context = getApplication(),
            notificationId = (System.currentTimeMillis() % 10000).toInt(),
            title = title,
            message = message
        )
    }
}
