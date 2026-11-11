package com.sumanonline.hisabkhata.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.OutboxDao
import com.sumanonline.hisabkhata.core.database.dao.PendingPaymentDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.OutboxOperationEntity
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity

@Database(
    entities = [
        CustomerEntity::class,
        TransactionEntity::class,
        OutboxOperationEntity::class,
        PendingPaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HisabKhataDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun outboxDao(): OutboxDao
    abstract fun pendingPaymentDao(): PendingPaymentDao
}
