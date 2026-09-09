package com.example.data.repository

import android.content.Context
import com.example.data.db.HomeMaintenanceDao
import com.example.data.model.Asset
import com.example.data.model.AssetWithDetails
import com.example.data.model.DateUtils
import com.example.data.model.LogWithAsset
import com.example.data.model.MaintenanceLog
import com.example.data.model.MaintenanceSchedule
import com.example.data.model.ScheduleWithAsset
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

class HomeMaintenanceRepository(private val dao: HomeMaintenanceDao) {

    val allAssets: Flow<List<Asset>> = dao.getAllAssets()
    val allSchedulesWithAsset: Flow<List<ScheduleWithAsset>> = dao.getAllSchedulesWithAsset()
    val allLogsWithAsset: Flow<List<LogWithAsset>> = dao.getAllLogsWithAsset()

    fun getAssetWithDetails(id: String): Flow<AssetWithDetails?> = dao.getAssetWithDetails(id)

    suspend fun insertAsset(asset: Asset) = dao.insertAsset(asset)

    suspend fun updateAsset(asset: Asset) = dao.updateAsset(asset)

    suspend fun deleteAsset(asset: Asset) = dao.deleteAsset(asset)

    suspend fun deleteAssetById(id: String) = dao.deleteAssetById(id)

    fun getSchedulesForAsset(assetId: String): Flow<List<MaintenanceSchedule>> =
        dao.getSchedulesForAsset(assetId)

    suspend fun insertSchedule(schedule: MaintenanceSchedule) = dao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: MaintenanceSchedule) = dao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: MaintenanceSchedule) = dao.deleteSchedule(schedule)

    suspend fun deleteScheduleById(id: String) = dao.deleteScheduleById(id)

    suspend fun insertLog(log: MaintenanceLog) = dao.insertLog(log)

    suspend fun deleteLogById(id: String) = dao.deleteLogById(id)

    fun searchAssets(query: String): Flow<List<Asset>> = dao.searchAssets(query)

    fun searchSchedules(query: String): Flow<List<ScheduleWithAsset>> = dao.searchSchedules(query)

    fun searchLogs(query: String): Flow<List<LogWithAsset>> = dao.searchLogs(query)

    /**
     * Mark a scheduled task as completed.
     * Records an entry in MaintenanceLog, automatically calculates the next recurring date
     * (or uses custom override if provided by user), and updates the schedule.
     */
    suspend fun completeTask(
        schedule: MaintenanceSchedule,
        completedDate: Long,
        cost: Double?,
        serviceProvider: String?,
        servicePhone: String?,
        meterReading: String?,
        notes: String?,
        nextDueDateOverride: Long? = null
    ) {
        val log = MaintenanceLog(
            id = UUID.randomUUID().toString(),
            assetId = schedule.assetId,
            scheduleId = schedule.id,
            taskName = schedule.taskName,
            completedDate = completedDate,
            cost = cost,
            serviceProvider = serviceProvider ?: schedule.defaultServiceProvider,
            servicePhone = servicePhone ?: schedule.defaultServicePhone,
            meterReading = meterReading,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )
        dao.insertLog(log)

        val nextDue = nextDueDateOverride ?: DateUtils.calculateNextDueDate(
            completedDateMillis = completedDate,
            frequencyType = schedule.frequencyType,
            frequencyValue = schedule.frequencyValue
        )

        val updatedSchedule = schedule.copy(
            lastCompletedDate = completedDate,
            nextDueDate = nextDue,
            defaultServiceProvider = serviceProvider ?: schedule.defaultServiceProvider,
            defaultServicePhone = servicePhone ?: schedule.defaultServicePhone,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateSchedule(updatedSchedule)
    }

    /**
     * Populates standard realistic household starter items if empty,
     * so user can immediately experience the "What needs attention?",
     * "When was it last done?", and "When do I need to do it again?" flow.
     */
    suspend fun populateStarterDataIfNeeded() {
        val cal = Calendar.getInstance()
        val today = DateUtils.getStartOfDay(cal.timeInMillis)

        // 1. Bedroom AC
        val acId = UUID.randomUUID().toString()
        val ac = Asset(
            id = acId,
            name = "Bedroom AC",
            category = "HVAC & Cooling",
            brandModel = "Daikin 1.5T Inverter 5-Star",
            serialNumber = "DK-2024-8841X",
            location = "Master Bedroom",
            purchaseDate = today - (180L * 24 * 60 * 60 * 1000),
            purchasePrice = 42000.0,
            warrantyDurationMonths = 24,
            warrantyExpiryDate = today + (185L * 24 * 60 * 60 * 1000),
            notes = "Installed on West wall. Remote kept on side-table."
        )

        val acSchedule1 = MaintenanceSchedule(
            id = UUID.randomUUID().toString(),
            assetId = acId,
            taskName = "General Servicing",
            frequencyType = "MONTHS",
            frequencyValue = 6,
            lastCompletedDate = today - (175L * 24 * 60 * 60 * 1000),
            nextDueDate = today + (5L * 24 * 60 * 60 * 1000), // Due in 5 days
            estimatedCost = 600.0,
            defaultServiceProvider = "CoolCare Technicians",
            defaultServicePhone = "+91 98765 43210",
            notes = "Clean cooling coils and check gas pressure"
        )

        val acSchedule2 = MaintenanceSchedule(
            id = UUID.randomUUID().toString(),
            assetId = acId,
            taskName = "Filter Mesh Rinse",
            frequencyType = "DAYS",
            frequencyValue = 30,
            lastCompletedDate = today - (28L * 24 * 60 * 60 * 1000),
            nextDueDate = today + (2L * 24 * 60 * 60 * 1000), // Due in 2 days
            estimatedCost = 0.0,
            notes = "Quick wash under running water"
        )

        val acLog = MaintenanceLog(
            id = UUID.randomUUID().toString(),
            assetId = acId,
            scheduleId = acSchedule1.id,
            taskName = "General Servicing",
            completedDate = today - (175L * 24 * 60 * 60 * 1000),
            cost = 550.0,
            serviceProvider = "CoolCare Technicians",
            servicePhone = "+91 98765 43210",
            notes = "Jet cleaning of outdoor unit done. Gas level normal."
        )

        // 2. Water Purifier
        val roId = UUID.randomUUID().toString()
        val ro = Asset(
            id = roId,
            name = "Water Purifier (RO)",
            category = "Kitchen Appliances",
            brandModel = "Kent Grand Plus",
            serialNumber = "KT-90124",
            location = "Kitchen",
            purchaseDate = today - (365L * 24 * 60 * 60 * 1000),
            purchasePrice = 16500.0,
            warrantyDurationMonths = 12,
            warrantyExpiryDate = today - (10L * 24 * 60 * 60 * 1000), // Expired 10 days ago
            notes = "TDS input ~450 ppm, purified ~35 ppm"
        )

        val roSchedule1 = MaintenanceSchedule(
            id = UUID.randomUUID().toString(),
            assetId = roId,
            taskName = "Filter Replacement",
            frequencyType = "MONTHS",
            frequencyValue = 6,
            lastCompletedDate = today - (180L * 24 * 60 * 60 * 1000),
            nextDueDate = today, // Due Today!
            estimatedCost = 750.0,
            defaultServiceProvider = "AquaPure Services",
            defaultServicePhone = "+91 98234 11223",
            notes = "Replace sediment candle & activated carbon"
        )

        val roLog = MaintenanceLog(
            id = UUID.randomUUID().toString(),
            assetId = roId,
            scheduleId = roSchedule1.id,
            taskName = "Sediment Filter Change",
            completedDate = today - (180L * 24 * 60 * 60 * 1000),
            cost = 700.0,
            serviceProvider = "AquaPure Services",
            servicePhone = "+91 98234 11223",
            notes = "Sediment filter and post carbon replaced"
        )

        // 3. Overhead Water Tank
        val tankId = UUID.randomUUID().toString()
        val tank = Asset(
            id = tankId,
            name = "Main Water Tank",
            category = "Plumbing & Water",
            brandModel = "Sintex 1500L Triple Layer",
            location = "Rooftop Terrace",
            purchaseDate = today - (730L * 24 * 60 * 60 * 1000),
            purchasePrice = 12000.0,
            notes = "Provides water to kitchen and both bathrooms"
        )

        val tankSchedule = MaintenanceSchedule(
            id = UUID.randomUUID().toString(),
            assetId = tankId,
            taskName = "Deep Cleaning & Disinfection",
            frequencyType = "MONTHS",
            frequencyValue = 6,
            lastCompletedDate = today - (192L * 24 * 60 * 60 * 1000),
            nextDueDate = today - (12L * 24 * 60 * 60 * 1000), // Overdue by 12 days!
            estimatedCost = 1200.0,
            defaultServiceProvider = "Shine Tank Cleaners",
            defaultServicePhone = "+91 98450 67890",
            notes = "Drain sludge, high-pressure wash and potassium permanganate treatment"
        )

        // 4. Family Car
        val carId = UUID.randomUUID().toString()
        val car = Asset(
            id = carId,
            name = "Hyundai Creta",
            category = "Vehicles",
            brandModel = "SX(O) 1.5 Turbo Petrol",
            serialNumber = "VIN-HY99281729",
            location = "Garage",
            purchaseDate = today - (400L * 24 * 60 * 60 * 1000),
            purchasePrice = 1850000.0,
            warrantyDurationMonths = 36,
            warrantyExpiryDate = today + (695L * 24 * 60 * 60 * 1000),
            notes = "Registration DL-08-CA-4421"
        )

        val carSchedule1 = MaintenanceSchedule(
            id = UUID.randomUUID().toString(),
            assetId = carId,
            taskName = "Engine Oil & Filter Service",
            frequencyType = "MONTHS",
            frequencyValue = 6,
            conditionValue = "or 5,000 km",
            lastCompletedDate = today - (90L * 24 * 60 * 60 * 1000),
            nextDueDate = today + (90L * 24 * 60 * 60 * 1000), // Upcoming in 3 months
            estimatedCost = 4500.0,
            defaultServiceProvider = "Authorized Hyundai Center",
            defaultServicePhone = "+91 11 4455 6677",
            notes = "0W-20 Full synthetic oil"
        )

        val carLog = MaintenanceLog(
            id = UUID.randomUUID().toString(),
            assetId = carId,
            scheduleId = carSchedule1.id,
            taskName = "Periodic Maintenance Service (10,000 km)",
            completedDate = today - (90L * 24 * 60 * 60 * 1000),
            cost = 4200.0,
            serviceProvider = "Authorized Hyundai Center",
            servicePhone = "+91 11 4455 6677",
            meterReading = "10,240 km",
            notes = "Engine oil, oil filter, wheel alignment done"
        )

        dao.insertAssets(listOf(ac, ro, tank, car))
        dao.insertSchedules(listOf(acSchedule1, acSchedule2, roSchedule1, tankSchedule, carSchedule1))
        dao.insertLogs(listOf(acLog, roLog, carLog))
    }

    /**
     * Complete local JSON export format.
     */
    suspend fun exportDataJson(allAssetsList: List<Asset>, schedulesList: List<ScheduleWithAsset>, logsList: List<LogWithAsset>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Home Maintenance")
        root.put("exportedAt", System.currentTimeMillis())

        val assetsArray = JSONArray()
        for (a in allAssetsList) {
            val obj = JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("category", a.category)
                put("brandModel", a.brandModel ?: "")
                put("serialNumber", a.serialNumber ?: "")
                put("purchaseDate", a.purchaseDate ?: 0L)
                put("purchasePrice", a.purchasePrice ?: 0.0)
                put("location", a.location ?: "")
                put("warrantyDurationMonths", a.warrantyDurationMonths ?: 0)
                put("warrantyExpiryDate", a.warrantyExpiryDate ?: 0L)
                put("invoiceNumber", a.invoiceNumber ?: "")
                put("warrantyNotes", a.warrantyNotes ?: "")
                put("photoUri", a.photoUri ?: "")
                put("notes", a.notes ?: "")
                put("createdAt", a.createdAt)
                put("updatedAt", a.updatedAt)
            }
            assetsArray.put(obj)
        }
        root.put("assets", assetsArray)

        val schedulesArray = JSONArray()
        for (item in schedulesList) {
            val s = item.schedule
            val obj = JSONObject().apply {
                put("id", s.id)
                put("assetId", s.assetId)
                put("taskName", s.taskName)
                put("frequencyType", s.frequencyType)
                put("frequencyValue", s.frequencyValue)
                put("conditionValue", s.conditionValue ?: "")
                put("lastCompletedDate", s.lastCompletedDate ?: 0L)
                put("nextDueDate", s.nextDueDate)
                put("reminderPeriodDays", s.reminderPeriodDays)
                put("remindersEnabled", s.remindersEnabled)
                put("estimatedCost", s.estimatedCost ?: 0.0)
                put("defaultServiceProvider", s.defaultServiceProvider ?: "")
                put("defaultServicePhone", s.defaultServicePhone ?: "")
                put("notes", s.notes ?: "")
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
            }
            schedulesArray.put(obj)
        }
        root.put("schedules", schedulesArray)

        val logsArray = JSONArray()
        for (item in logsList) {
            val l = item.log
            val obj = JSONObject().apply {
                put("id", l.id)
                put("assetId", l.assetId)
                put("scheduleId", l.scheduleId ?: "")
                put("taskName", l.taskName)
                put("completedDate", l.completedDate)
                put("cost", l.cost ?: 0.0)
                put("serviceProvider", l.serviceProvider ?: "")
                put("servicePhone", l.servicePhone ?: "")
                put("meterReading", l.meterReading ?: "")
                put("notes", l.notes ?: "")
                put("attachmentUri", l.attachmentUri ?: "")
                put("createdAt", l.createdAt)
            }
            logsArray.put(obj)
        }
        root.put("logs", logsArray)

        return root.toString(2)
    }

    /**
     * Restore from local JSON export format.
     */
    suspend fun restoreDataJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val assetsArray = root.optJSONArray("assets") ?: JSONArray()
            val schedulesArray = root.optJSONArray("schedules") ?: JSONArray()
            val logsArray = root.optJSONArray("logs") ?: JSONArray()

            val newAssets = mutableListOf<Asset>()
            for (i in 0 until assetsArray.length()) {
                val obj = assetsArray.getJSONObject(i)
                newAssets.add(
                    Asset(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.optString("category", "General"),
                        brandModel = obj.optString("brandModel").takeIf { it.isNotBlank() },
                        serialNumber = obj.optString("serialNumber").takeIf { it.isNotBlank() },
                        purchaseDate = obj.optLong("purchaseDate").takeIf { it > 0 },
                        purchasePrice = obj.optDouble("purchasePrice").takeIf { !it.isNaN() && it > 0 },
                        location = obj.optString("location").takeIf { it.isNotBlank() },
                        warrantyDurationMonths = obj.optInt("warrantyDurationMonths").takeIf { it > 0 },
                        warrantyExpiryDate = obj.optLong("warrantyExpiryDate").takeIf { it > 0 },
                        invoiceNumber = obj.optString("invoiceNumber").takeIf { it.isNotBlank() },
                        warrantyNotes = obj.optString("warrantyNotes").takeIf { it.isNotBlank() },
                        photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val newSchedules = mutableListOf<MaintenanceSchedule>()
            for (i in 0 until schedulesArray.length()) {
                val obj = schedulesArray.getJSONObject(i)
                newSchedules.add(
                    MaintenanceSchedule(
                        id = obj.getString("id"),
                        assetId = obj.getString("assetId"),
                        taskName = obj.getString("taskName"),
                        frequencyType = obj.optString("frequencyType", "MONTHS"),
                        frequencyValue = obj.optInt("frequencyValue", 6),
                        conditionValue = obj.optString("conditionValue").takeIf { it.isNotBlank() },
                        lastCompletedDate = obj.optLong("lastCompletedDate").takeIf { it > 0 },
                        nextDueDate = obj.optLong("nextDueDate", System.currentTimeMillis()),
                        reminderPeriodDays = obj.optInt("reminderPeriodDays", 3),
                        remindersEnabled = obj.optBoolean("remindersEnabled", true),
                        estimatedCost = obj.optDouble("estimatedCost").takeIf { !it.isNaN() && it > 0 },
                        defaultServiceProvider = obj.optString("defaultServiceProvider").takeIf { it.isNotBlank() },
                        defaultServicePhone = obj.optString("defaultServicePhone").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val newLogs = mutableListOf<MaintenanceLog>()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                newLogs.add(
                    MaintenanceLog(
                        id = obj.getString("id"),
                        assetId = obj.getString("assetId"),
                        scheduleId = obj.optString("scheduleId").takeIf { it.isNotBlank() },
                        taskName = obj.getString("taskName"),
                        completedDate = obj.optLong("completedDate", System.currentTimeMillis()),
                        cost = obj.optDouble("cost").takeIf { !it.isNaN() && it > 0 },
                        serviceProvider = obj.optString("serviceProvider").takeIf { it.isNotBlank() },
                        servicePhone = obj.optString("servicePhone").takeIf { it.isNotBlank() },
                        meterReading = obj.optString("meterReading").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        attachmentUri = obj.optString("attachmentUri").takeIf { it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            dao.clearAllData()
            dao.insertAssets(newAssets)
            dao.insertSchedules(newSchedules)
            dao.insertLogs(newLogs)
            true
        } catch (_: Exception) {
            false
        }
    }
}
