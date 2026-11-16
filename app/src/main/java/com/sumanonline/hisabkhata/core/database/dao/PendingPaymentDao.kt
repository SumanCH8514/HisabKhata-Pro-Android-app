package com.sumanonline.hisabkhata.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPaymentDao {

    @Query("SELECT * FROM pending_payments WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun observePendingPayments(merchantId: String): Flow<List<PendingPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(payments: List<PendingPaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PendingPaymentEntity)

    @Update
    suspend fun update(payment: PendingPaymentEntity)

    @Query("SELECT * FROM pending_payments WHERE id = :id LIMIT 1")
    suspend fun getPaymentById(id: String): PendingPaymentEntity?

    @Query("SELECT * FROM pending_payments WHERE merchantId = :merchantId AND status = 'pending' ORDER BY timestamp DESC")
    suspend fun getPendingPayments(merchantId: String): List<PendingPaymentEntity>

    @Query("UPDATE pending_payments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE pending_payments SET status = :status, transactionId = :transactionId WHERE id = :id")
    suspend fun updateStatusAndTxId(id: String, status: String, transactionId: String?)

    @Query("DELETE FROM pending_payments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_payments WHERE (status = 'pending' OR status = 'cancelled') AND timestamp < :cutoffTimestamp")
    suspend fun deleteExpiredPendingPayments(cutoffTimestamp: Long)

    @Query("DELETE FROM pending_payments WHERE status = 'pending' OR status = 'cancelled'")
    suspend fun deleteAllPendingAndCancelled()
}
