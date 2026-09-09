package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class LogWithAsset(
    @Embedded val log: MaintenanceLog,
    @Relation(
        parentColumn = "assetId",
        entityColumn = "id"
    )
    val asset: Asset
)
