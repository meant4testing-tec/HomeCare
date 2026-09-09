package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ScheduleWithAsset
import com.example.data.notification.NotificationHelper
import com.example.ui.components.AppTopBar
import com.example.ui.dialogs.ExportBackupDialog
import com.example.ui.dialogs.GoogleSyncDialog
import com.example.ui.dialogs.ImportBackupDialog
import com.example.ui.dialogs.SearchDialog
import com.example.ui.sheets.AddAssetSheet
import com.example.ui.sheets.AddScheduleSheet
import com.example.ui.sheets.MarkCompleteSheet
import com.example.ui.sheets.QuickAddModal
import com.example.ui.sheets.QuickLogSheet
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1100)
        showSplash = false
    }

    val currentDest by viewModel.currentDestination.collectAsState()
    val selectedAssetId by viewModel.selectedAssetId.collectAsState()
    val allAssets by viewModel.allAssets.collectAsState()
    val allSchedules by viewModel.allSchedulesWithAsset.collectAsState()
    val allLogs by viewModel.allLogsWithAsset.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val expensesSummary by viewModel.expensesSummary.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val categoryFilter by viewModel.assetCategoryFilter.collectAsState()
    val calendarDate by viewModel.calendarSelectedDate.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val activeRemindersCount by viewModel.activeScheduledRemindersCount.collectAsState()
    val nextReminder by viewModel.nextScheduledReminder.collectAsState()
    val isNotificationPermissionGranted = remember(currentDest, showSplash) {
        NotificationHelper.areNotificationsEnabled(context)
    }

    // Modals
    val showQuickAdd by viewModel.showQuickAddModal.collectAsState()
    val showAddAsset by viewModel.showAddAssetSheet.collectAsState()
    val editingAsset by viewModel.editingAsset.collectAsState()
    val showAddSchedule by viewModel.showAddScheduleSheet.collectAsState()
    val scheduleTargetAssetId by viewModel.scheduleTargetAssetId.collectAsState()
    val editingSchedule by viewModel.editingSchedule.collectAsState()
    val completingTask by viewModel.completingTask.collectAsState()
    val showQuickLog by viewModel.showQuickLogSheet.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonContent by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showGoogleSyncDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (selectedAssetId == null) {
                    val (title, subtitle) = when (currentDest) {
                        AppNavDestination.DASHBOARD -> "HomeCare" to "Personal household maintenance diary"
                        AppNavDestination.ASSETS -> "Household Assets" to "${allAssets.size} tracked items"
                        AppNavDestination.CALENDAR -> "Maintenance Calendar" to "Recurring timeline"
                        AppNavDestination.HISTORY -> "Service & Expenses" to "Maintenance logbook"
                        AppNavDestination.SETTINGS -> "Settings & Sync" to "Device status & local-first preferences"
                    }
                    AppTopBar(
                        title = title,
                        subtitle = subtitle,
                        settings = settingsState,
                        onSearchClick = { showSearchDialog = true },
                        onSyncStatusClick = { showGoogleSyncDialog = true },
                        onSettingsClick = { viewModel.navigateTo(AppNavDestination.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentDest == AppNavDestination.DASHBOARD && selectedAssetId == null,
                        onClick = { viewModel.navigateTo(AppNavDestination.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = (currentDest == AppNavDestination.ASSETS) || selectedAssetId != null,
                        onClick = { viewModel.navigateTo(AppNavDestination.ASSETS) },
                        icon = { Icon(Icons.Default.HomeRepairService, contentDescription = "Assets") },
                        label = { Text("Assets") },
                        modifier = Modifier.testTag("nav_assets")
                    )
                    NavigationBarItem(
                        selected = currentDest == AppNavDestination.CALENDAR && selectedAssetId == null,
                        onClick = { viewModel.navigateTo(AppNavDestination.CALENDAR) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                        label = { Text("Calendar") },
                        modifier = Modifier.testTag("nav_calendar")
                    )
                    NavigationBarItem(
                        selected = currentDest == AppNavDestination.HISTORY && selectedAssetId == null,
                        onClick = { viewModel.navigateTo(AppNavDestination.HISTORY) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "History") },
                        label = { Text("History") },
                        modifier = Modifier.testTag("nav_history")
                    )
                    NavigationBarItem(
                        selected = currentDest == AppNavDestination.SETTINGS && selectedAssetId == null,
                        onClick = { viewModel.navigateTo(AppNavDestination.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            },
        floatingActionButton = {
            if (selectedAssetId == null) {
                FloatingActionButton(
                    onClick = { viewModel.openQuickAddModal() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("quick_add_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Add")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedAssetId != null) {
                val asset = allAssets.find { it.id == selectedAssetId }
                if (asset != null) {
                    val assetSchedules = allSchedules.filter { it.schedule.assetId == asset.id }.map { it.schedule }
                    val assetLogs = allLogs.filter { it.asset.id == asset.id }.map { it.log }

                    AssetDetailScreen(
                        asset = asset,
                        schedules = assetSchedules,
                        logs = assetLogs,
                        currencySymbol = settingsState.currencySymbol,
                        onBackClick = { viewModel.selectAsset(null) },
                        onEditAsset = { viewModel.openAddAsset(asset) },
                        onDeleteAsset = { viewModel.deleteAsset(asset) },
                        onAddSchedule = { viewModel.openAddSchedule(targetAssetId = asset.id) },
                        onEditSchedule = { s -> viewModel.openAddSchedule(targetAssetId = asset.id, scheduleToEdit = s) },
                        onDeleteSchedule = { s -> viewModel.deleteSchedule(s) },
                        onCompleteTask = { sWithA -> viewModel.openCompleteTask(sWithA) },
                        onToggleReminder = { s -> viewModel.toggleScheduleReminder(s) },
                        onDeleteLog = { logId -> viewModel.deleteLog(logId) }
                    )
                } else {
                    viewModel.selectAsset(null)
                }
            } else {
                when (currentDest) {
                    AppNavDestination.DASHBOARD -> {
                        DashboardScreen(
                            dashboardState = dashboardState,
                            expensesSummary = expensesSummary,
                            totalAssetsCount = allAssets.size,
                            currencySymbol = settingsState.currencySymbol,
                            onTaskClick = { task -> viewModel.selectAsset(task.asset.id) },
                            onCompleteTask = { task -> viewModel.openCompleteTask(task) },
                            onToggleReminder = { task -> viewModel.toggleScheduleReminder(task.schedule) },
                            onViewAsset = { assetId -> viewModel.selectAsset(assetId) },
                            onAddNewTask = { viewModel.openQuickAddModal() },
                            onViewAllAssets = { viewModel.navigateTo(AppNavDestination.ASSETS) }
                        )
                    }
                    AppNavDestination.ASSETS -> {
                        AssetsScreen(
                            assets = allAssets,
                            allSchedules = allSchedules,
                            selectedCategory = categoryFilter,
                            searchQuery = "",
                            onCategorySelect = { viewModel.setCategoryFilter(it) },
                            onAssetClick = { assetId -> viewModel.selectAsset(assetId) },
                            onAddAssetClick = { viewModel.openAddAsset() }
                        )
                    }
                    AppNavDestination.CALENDAR -> {
                        CalendarScreen(
                            schedules = allSchedules,
                            selectedDateMillis = calendarDate,
                            currencySymbol = settingsState.currencySymbol,
                            onSelectDate = { viewModel.setCalendarDate(it) },
                            onTaskClick = { task -> viewModel.selectAsset(task.asset.id) },
                            onCompleteTask = { task -> viewModel.openCompleteTask(task) },
                            onToggleReminder = { task -> viewModel.toggleScheduleReminder(task.schedule) }
                        )
                    }
                    AppNavDestination.HISTORY -> {
                        HistoryExpensesScreen(
                            logs = allLogs,
                            summary = expensesSummary,
                            currencySymbol = settingsState.currencySymbol,
                            onAssetClick = { assetId -> viewModel.selectAsset(assetId) },
                            onDeleteLog = { logId -> viewModel.deleteLog(logId) },
                            onAddLogClick = { viewModel.openQuickLogSheet() }
                        )
                    }
                    AppNavDestination.SETTINGS -> {
                        SettingsScreen(
                            settings = settingsState,
                            isNotificationPermissionGranted = isNotificationPermissionGranted,
                            activeRemindersCount = activeRemindersCount,
                            nextReminder = nextReminder,
                            onOpenNotificationSettings = {
                                val intent = Intent().apply {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        action = AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS
                                        putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                                    } else {
                                        action = AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            onToggleGoogleSync = { viewModel.toggleGoogleSync(it) },
                            onManualSync = { viewModel.triggerManualSync() },
                            onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
                            onReminderDaysChange = { viewModel.setDefaultReminderDays(it) },
                            onCurrencyChange = { viewModel.setCurrency(it) },
                            onExportBackup = {
                                coroutineScope.launch {
                                    exportJsonContent = viewModel.exportDataJson()
                                    showExportDialog = true
                                }
                            },
                            onImportBackup = { showImportDialog = true },
                            onLoadStarterData = { viewModel.loadStarterData() },
                            onSendTestNotification = {
                                viewModel.sendTestNotification(
                                    title = "Water Purifier Service Due",
                                    message = "Sediment & pre-carbon filter replacement is scheduled for today."
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        SplashScreen()
    }
}

    // Modal Sheets
    if (showQuickAdd) {
        QuickAddModal(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismiss = { viewModel.closeQuickAddModal() },
            onAddAsset = { viewModel.openAddAsset() },
            onAddSchedule = { viewModel.openAddSchedule() },
            onQuickLog = { viewModel.openQuickLogSheet() }
        )
    }

    if (showAddAsset) {
        AddAssetSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            assetToEdit = editingAsset,
            currencySymbol = settingsState.currencySymbol,
            onDismiss = { viewModel.closeAddAsset() },
            onSave = { name, cat, model, serial, pDate, price, loc, months, expiry, inv, wNotes, notes, suggested ->
                viewModel.saveAsset(name, cat, model, serial, pDate, price, loc, months, expiry, inv, wNotes, notes, suggested)
            }
        )
    }

    if (showAddSchedule) {
        AddScheduleSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            scheduleToEdit = editingSchedule,
            targetAssetId = scheduleTargetAssetId,
            assets = allAssets,
            currencySymbol = settingsState.currencySymbol,
            onDismiss = { viewModel.closeAddSchedule() },
            onSave = { assetId, task, fType, fVal, cond, due, rem, est, prov, phone, notes ->
                viewModel.saveSchedule(assetId, task, fType, fVal, cond, due, rem, est, prov, phone, notes)
            }
        )
    }

    completingTask?.let { task ->
        MarkCompleteSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            task = task,
            currencySymbol = settingsState.currencySymbol,
            onDismiss = { viewModel.closeCompleteTask() },
            onConfirm = { date, cost, prov, phone, meter, notes, nextDueOverride ->
                viewModel.completeScheduledTask(task, date, cost, prov, phone, meter, notes, nextDueOverride)
            }
        )
    }

    if (showQuickLog) {
        QuickLogSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            assets = allAssets,
            currencySymbol = settingsState.currencySymbol,
            onDismiss = { viewModel.closeQuickLogSheet() },
            onSave = { assetId, task, date, cost, prov, phone, meter, notes ->
                viewModel.logPastService(assetId, task, date, cost, prov, phone, meter, notes)
            }
        )
    }

    // Dialogs
    if (showSearchDialog) {
        SearchDialog(
            allAssets = allAssets,
            allSchedules = allSchedules,
            allLogs = allLogs,
            onDismiss = { showSearchDialog = false },
            onSelectAsset = { assetId ->
                viewModel.selectAsset(assetId)
            }
        )
    }

    if (showExportDialog) {
        ExportBackupDialog(
            jsonContent = exportJsonContent,
            onDismiss = { showExportDialog = false },
            onCopied = { viewModel.showSnackbar("Backup JSON copied to clipboard!") }
        )
    }

    if (showImportDialog) {
        ImportBackupDialog(
            onDismiss = { showImportDialog = false },
            onRestore = { json ->
                coroutineScope.launch {
                    viewModel.restoreDataJson(json)
                }
            }
        )
    }

    if (showGoogleSyncDialog) {
        GoogleSyncDialog(
            settings = settingsState,
            onDismiss = { showGoogleSyncDialog = false },
            onToggleSync = { viewModel.toggleGoogleSync(it) },
            onSyncNow = { viewModel.triggerManualSync() }
        )
    }
}
