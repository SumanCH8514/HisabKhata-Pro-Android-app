package com.sumanonline.hisabkhata.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sumanonline.hisabkhata.core.common.Constants

@Entity(
    tableName = "outbox_operations",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class OutboxOperationEntity(
    @PrimaryKey
    val operationId: String,
    val operationType: String,
    val entityId: String,
    val userId: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val status: String = Constants.SYNC_STATUS_PENDING
)
