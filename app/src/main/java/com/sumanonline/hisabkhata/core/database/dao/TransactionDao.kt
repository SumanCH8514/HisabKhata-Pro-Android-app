package com.sumanonline.hisabkhata.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC, id DESC")
    fun observeTransactionsByCustomer(customerId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC, id DESC")
    suspend fun getTransactionsByCustomer(customerId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC, id DESC")
    fun observeAllTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC, id DESC")
    suspend fun getAllTransactionsByUser(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :txId")
    suspend fun getTransactionById(txId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE clientTxId = :clientTxId")
    suspend fun getTransactionByClientTxId(clientTxId: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :txId")
    suspend fun deleteById(txId: String)

    @Query("DELETE FROM transactions WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: String)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
