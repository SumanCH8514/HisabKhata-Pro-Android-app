package com.sumanonline.hisabkhata.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_payments",
    indices = [
        Index(value = ["merchantId"]),
        Index(value = ["status"])
    ]
)
data class PendingPaymentEntity(
    @PrimaryKey
    val id: String,
    val merchantId: String,
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val transactionId: String? = null,
    val screenshot: String? = null,
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)
