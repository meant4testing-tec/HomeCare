package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ScheduleWithAsset(
    @Embedded val schedule: MaintenanceSchedule,
    @Relation(
        parentColumn = "assetId",
        entityColumn = "id"
    )
    val asset: Asset
)
