package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "maintenance_logs",
    foreignKeys = [
        ForeignKey(
            entity = Asset::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"]), Index(value = ["scheduleId"])]
)
data class MaintenanceLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val assetId: String,
    val scheduleId: String? = null,
    val taskName: String,
    val completedDate: Long = System.currentTimeMillis(),
    val cost: Double? = null,
    val serviceProvider: String? = null,
    val servicePhone: String? = null,
    val meterReading: String? = null,
    val notes: String? = null,
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
