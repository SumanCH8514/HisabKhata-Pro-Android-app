package com.sumanonline.hisabkhata.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sumanonline.hisabkhata.core.common.Constants

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["userId"]),
        Index(value = ["clientTxId"], unique = true),
        Index(value = ["syncStatus"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val clientTxId: String,
    val userId: String,
    val customerId: String,
    val amount: Double,
    val type: String,
    val description: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val balance: Double = 0.0,
    val attachment: String? = null,
    val syncStatus: String = Constants.SYNC_STATUS_SYNCED
)
