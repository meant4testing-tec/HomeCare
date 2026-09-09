package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "maintenance_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Asset::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"])]
)
data class MaintenanceSchedule(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val assetId: String,
    val taskName: String,
    val frequencyType: String = "MONTHS", // DAYS, WEEKS, MONTHS, YEARS, ONE_TIME
    val frequencyValue: Int = 6,
    val conditionValue: String? = null, // e.g. "or 5,000 km"
    val lastCompletedDate: Long? = null,
    val nextDueDate: Long = System.currentTimeMillis(),
    val reminderPeriodDays: Int = 3, // 0 = due date, 1 = 1 day before, 3 = 3 days before, 7 = 1 week before
    val remindersEnabled: Boolean = true,
    val estimatedCost: Double? = null,
    val defaultServiceProvider: String? = null,
    val defaultServicePhone: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
