package com.sumanonline.hisabkhata.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sumanonline.hisabkhata.core.common.Constants

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val balance: Double = 0.0,
    val type: String = Constants.PARTY_TYPE_CUSTOMER,
    val photoURL: String? = null,
    val gst: String? = null,
    val dueDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = Constants.SYNC_STATUS_SYNCED
)
