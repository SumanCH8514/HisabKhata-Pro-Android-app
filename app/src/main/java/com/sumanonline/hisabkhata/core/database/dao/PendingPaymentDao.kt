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

    @Update
    suspend fun update(payment: PendingPaymentEntity)

    @Query("UPDATE pending_payments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM pending_payments WHERE id = :id")
    suspend fun deleteById(id: String)
}
