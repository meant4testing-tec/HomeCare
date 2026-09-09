package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class AssetWithDetails(
    @Embedded val asset: Asset,
    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val schedules: List<MaintenanceSchedule>,
    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val logs: List<MaintenanceLog>
)
