package com.sumanonline.hisabkhata.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeCustomersByUser(userId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE userId = :userId")
    suspend fun getCustomersByUser(userId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun observeCustomerById(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(customers: List<CustomerEntity>)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("UPDATE customers SET balance = :newBalance, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun updateBalance(customerId: String, newBalance: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET dueDate = :dueDate, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun updateDueDate(customerId: String, dueDate: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteById(customerId: String)

    @Query("DELETE FROM customers WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
