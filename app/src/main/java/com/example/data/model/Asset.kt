package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "General",
    val brandModel: String? = null,
    val serialNumber: String? = null,
    val purchaseDate: Long? = null,
    val purchasePrice: Double? = null,
    val location: String? = null,
    val warrantyDurationMonths: Int? = null,
    val warrantyExpiryDate: Long? = null,
    val invoiceNumber: String? = null,
    val warrantyNotes: String? = null,
    val photoUri: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
