package com.sumanonline.hisabkhata.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.database.entity.OutboxOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Query("SELECT * FROM outbox_operations WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getOperationsByStatus(status: String = Constants.SYNC_STATUS_PENDING): List<OutboxOperationEntity>

    @Query("SELECT COUNT(*) FROM outbox_operations WHERE status != :syncedStatus")
    fun observePendingCount(syncedStatus: String = Constants.SYNC_STATUS_SYNCED): Flow<Int>

    @Query("SELECT * FROM outbox_operations ORDER BY createdAt DESC")
    fun observeAllOperations(): Flow<List<OutboxOperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OutboxOperationEntity)

    @Update
    suspend fun updateOperation(operation: OutboxOperationEntity)

    @Query("UPDATE outbox_operations SET status = :status, attemptCount = attemptCount + 1, lastError = :error WHERE operationId = :operationId")
    suspend fun updateStatus(operationId: String, status: String, error: String? = null)

    @Query("DELETE FROM outbox_operations WHERE operationId = :operationId")
    suspend fun deleteOperation(operationId: String)

    @Query("DELETE FROM outbox_operations WHERE status = :status")
    suspend fun deleteCompleted(status: String = Constants.SYNC_STATUS_SYNCED)
}
