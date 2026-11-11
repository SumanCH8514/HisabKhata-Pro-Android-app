package com.sumanonline.hisabkhata.core.sync

import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.OutboxDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.OutboxOperationEntity
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.network.FirebaseApiService
import com.sumanonline.hisabkhata.core.network.model.FirebaseCustomerDto
import com.sumanonline.hisabkhata.core.network.model.FirebaseTransactionDto
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val outboxDao: OutboxDao,
    private val firebaseApi: FirebaseApiService,
    private val sessionManager: SecureSessionManager,
    private val json: Json
) {
    val pendingCount: Flow<Int> = outboxDao.observePendingCount()

    suspend fun createCustomerOffline(
        name: String,
        phone: String,
        email: String,
        openingBalance: Double,
        type: String,
        photoURL: String? = null,
        gst: String? = null,
        dueDate: String? = null
    ): String = withContext(Dispatchers.IO) {
        val customerId = "cust_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val userId = sessionManager.getUserId()
        val now = System.currentTimeMillis()

        val customer = CustomerEntity(
            id = customerId,
            userId = userId,
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim(),
            balance = openingBalance,
            type = type,
            photoURL = photoURL,
            gst = gst?.trim(),
            dueDate = dueDate,
            createdAt = now,
            updatedAt = now,
            syncStatus = Constants.SYNC_STATUS_PENDING
        )

        customerDao.insertOrUpdate(customer)

        val dto = FirebaseCustomerDto(
            userId = userId,
            name = customer.name,
            phone = customer.phone,
            email = customer.email,
            balance = customer.balance,
            type = customer.type,
            photoURL = customer.photoURL,
            gst = customer.gst,
            dueDate = customer.dueDate,
            createdAt = now,
            updatedAt = now
        )

        val outboxOp = OutboxOperationEntity(
            operationId = UUID.randomUUID().toString(),
            operationType = Constants.OP_CREATE_CUSTOMER,
            entityId = customerId,
            userId = userId,
            payloadJson = json.encodeToString(FirebaseCustomerDto.serializer(), dto),
            createdAt = now,
            status = Constants.SYNC_STATUS_PENDING
        )

        outboxDao.insertOperation(outboxOp)
        customerId
    }

    suspend fun updateCustomerOffline(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        val updated = customer.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = Constants.SYNC_STATUS_PENDING
        )
        customerDao.update(updated)

        val dto = FirebaseCustomerDto(
            userId = updated.userId,
            name = updated.name,
            phone = updated.phone,
            email = updated.email,
            balance = updated.balance,
            type = updated.type,
            photoURL = updated.photoURL,
            gst = updated.gst,
            dueDate = updated.dueDate,
            createdAt = updated.createdAt,
            updatedAt = updated.updatedAt
        )

        val outboxOp = OutboxOperationEntity(
            operationId = UUID.randomUUID().toString(),
            operationType = Constants.OP_UPDATE_CUSTOMER,
            entityId = updated.id,
            userId = updated.userId,
            payloadJson = json.encodeToString(FirebaseCustomerDto.serializer(), dto),
            createdAt = System.currentTimeMillis(),
            status = Constants.SYNC_STATUS_PENDING
        )

        outboxDao.insertOperation(outboxOp)
    }

    suspend fun deleteCustomerOffline(customerId: String) = withContext(Dispatchers.IO) {
        val userId = sessionManager.getUserId()
        customerDao.deleteById(customerId)
        transactionDao.deleteByCustomerId(customerId)

        val outboxOp = OutboxOperationEntity(
            operationId = UUID.randomUUID().toString(),
            operationType = Constants.OP_DELETE_CUSTOMER,
            entityId = customerId,
            userId = userId,
            payloadJson = customerId,
            createdAt = System.currentTimeMillis(),
            status = Constants.SYNC_STATUS_PENDING
        )

        outboxDao.insertOperation(outboxOp)
    }

    suspend fun createTransactionOffline(
        customerId: String,
        amount: Double,
        type: String,
        description: String,
        date: String,
        attachment: String? = null
    ): String = withContext(Dispatchers.IO) {
        val txId = "tx_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val clientTxId = UUID.randomUUID().toString()
        val userId = sessionManager.getUserId()
        val now = System.currentTimeMillis()

        val customer = customerDao.getCustomerById(customerId)
            ?: throw IllegalStateException("Customer not found")

        val currentBalance = customer.balance
        val newCustomerBalance = currentBalance + amount

        val transaction = TransactionEntity(
            id = txId,
            clientTxId = clientTxId,
            userId = userId,
            customerId = customerId,
            amount = amount,
            type = type,
            description = description.trim(),
            date = date,
            timestamp = now,
            balance = newCustomerBalance,
            attachment = attachment,
            syncStatus = Constants.SYNC_STATUS_PENDING
        )

        transactionDao.insertOrUpdate(transaction)
        customerDao.updateBalance(customerId, newCustomerBalance, now)

        val dto = FirebaseTransactionDto(
            userId = userId,
            customerId = customerId,
            amount = amount,
            type = type,
            description = description.trim(),
            date = date,
            timestamp = now,
            balance = newCustomerBalance,
            attachment = attachment,
            attachments = if (attachment != null) listOf(attachment) else emptyList(),
            clientTxId = clientTxId
        )

        val outboxOp = OutboxOperationEntity(
            operationId = UUID.randomUUID().toString(),
            operationType = Constants.OP_CREATE_TRANSACTION,
            entityId = txId,
            userId = userId,
            payloadJson = json.encodeToString(FirebaseTransactionDto.serializer(), dto),
            createdAt = now,
            status = Constants.SYNC_STATUS_PENDING
        )

        outboxDao.insertOperation(outboxOp)
        txId
    }

    suspend fun deleteTransactionOffline(customerId: String, txId: String, amount: Double) = withContext(Dispatchers.IO) {
        val userId = sessionManager.getUserId()
        val customer = customerDao.getCustomerById(customerId)
        if (customer != null) {
            val newBalance = customer.balance - amount
            customerDao.updateBalance(customerId, newBalance, System.currentTimeMillis())
        }
        transactionDao.deleteById(txId)

        val outboxOp = OutboxOperationEntity(
            operationId = UUID.randomUUID().toString(),
            operationType = Constants.OP_DELETE_TRANSACTION,
            entityId = txId,
            userId = userId,
            payloadJson = json.encodeToString(
                mapOf("customerId" to customerId, "txId" to txId, "amount" to amount.toString())
            ),
            createdAt = System.currentTimeMillis(),
            status = Constants.SYNC_STATUS_PENDING
        )

        outboxDao.insertOperation(outboxOp)
    }

    private data class ExecutionResult(
        val isSuccess: Boolean,
        val httpCode: Int = 200,
        val errorMessage: String? = null
    )

    private suspend fun refreshToken(): String? {
        val refreshToken = sessionManager.getRefreshToken()
        if (refreshToken.isBlank()) return null
        return try {
            val response = firebaseApi.refreshIdToken(
                apiKey = Constants.FIREBASE_API_KEY,
                refreshToken = refreshToken
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val newIdToken = body.idToken
                val newRefreshToken = body.refreshToken.ifBlank { refreshToken }
                sessionManager.updateTokens(newIdToken, newRefreshToken)
                newIdToken
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun executeOperation(op: OutboxOperationEntity, token: String): ExecutionResult {
        return when (op.operationType) {
            Constants.OP_CREATE_CUSTOMER, Constants.OP_UPDATE_CUSTOMER -> {
                val dto = json.decodeFromString(FirebaseCustomerDto.serializer(), op.payloadJson)
                val res = firebaseApi.putCustomer(op.entityId, dto, token)
                if (res.isSuccessful) {
                    customerDao.getCustomerById(op.entityId)?.let {
                        customerDao.update(it.copy(syncStatus = Constants.SYNC_STATUS_SYNCED))
                    }
                    ExecutionResult(isSuccess = true)
                } else {
                    ExecutionResult(isSuccess = false, httpCode = res.code(), errorMessage = res.message())
                }
            }
            Constants.OP_DELETE_CUSTOMER -> {
                val res = firebaseApi.deleteCustomer(op.entityId, token)
                if (res.isSuccessful) {
                    ExecutionResult(isSuccess = true)
                } else {
                    ExecutionResult(isSuccess = false, httpCode = res.code(), errorMessage = res.message())
                }
            }
            Constants.OP_CREATE_TRANSACTION -> {
                val dto = json.decodeFromString(FirebaseTransactionDto.serializer(), op.payloadJson)
                val res = firebaseApi.putTransaction(op.entityId, dto, token)
                if (res.isSuccessful) {
                    transactionDao.getTransactionById(op.entityId)?.let {
                        transactionDao.update(it.copy(syncStatus = Constants.SYNC_STATUS_SYNCED))
                    }
                    customerDao.getCustomerById(dto.customerId)?.let {
                        val currentBal = it.balance
                        val patchObj = kotlinx.serialization.json.buildJsonObject {
                            put("balance", kotlinx.serialization.json.JsonPrimitive(currentBal))
                            put("updatedAt", kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()))
                        }
                        try {
                            firebaseApi.patchCustomer(dto.customerId, patchObj, token)
                        } catch (_: Exception) {
                        }
                    }
                    ExecutionResult(isSuccess = true)
                } else {
                    ExecutionResult(isSuccess = false, httpCode = res.code(), errorMessage = res.message())
                }
            }
            Constants.OP_DELETE_TRANSACTION -> {
                val res = firebaseApi.deleteTransaction(op.entityId, token)
                if (res.isSuccessful) {
                    ExecutionResult(isSuccess = true)
                } else {
                    ExecutionResult(isSuccess = false, httpCode = res.code(), errorMessage = res.message())
                }
            }
            else -> ExecutionResult(isSuccess = true)
        }
    }

    suspend fun synchronize(): Boolean = withContext(Dispatchers.IO) {
        var token = sessionManager.getIdToken()
        val userId = sessionManager.getUserId()
        if (userId.isBlank()) return@withContext false

        if (token.isBlank()) {
            token = refreshToken() ?: return@withContext false
        }

        // Fetch all unsynced operations (including PENDING_SYNC, FAILED, and SYNCING)
        val pendingOps = outboxDao.getAllUnsyncedOperations()
        var allSucceeded = true

        for (op in pendingOps) {
            try {
                outboxDao.updateStatus(op.operationId, Constants.SYNC_STATUS_SYNCING)
                var result = executeOperation(op, token)

                // If unauthorized (token expired / 401), automatically refresh the token and retry once
                if (!result.isSuccess && result.httpCode == 401) {
                    val freshToken = refreshToken()
                    if (!freshToken.isNullOrBlank()) {
                        token = freshToken
                        result = executeOperation(op, token)
                    }
                }

                if (result.isSuccess) {
                    outboxDao.deleteOperation(op.operationId)
                } else {
                    allSucceeded = false
                    outboxDao.updateStatus(op.operationId, Constants.SYNC_STATUS_FAILED, result.errorMessage)
                }
            } catch (e: Exception) {
                allSucceeded = false
                outboxDao.updateStatus(op.operationId, Constants.SYNC_STATUS_FAILED, e.message)
            }
        }

        refreshDataFromCloud(token)
        allSucceeded
    }

    suspend fun refreshDataFromCloud(passedToken: String? = null) = withContext(Dispatchers.IO) {
        var token = passedToken ?: sessionManager.getIdToken()
        val userId = sessionManager.getUserId()
        if (userId.isBlank()) return@withContext

        if (token.isBlank()) {
            token = refreshToken() ?: return@withContext
        }

        try {
            var customersRes = firebaseApi.getCustomersByUser(equalTo = "\"$userId\"", auth = token)
            if (customersRes.code() == 401) {
                val fresh = refreshToken()
                if (fresh != null) {
                    token = fresh
                    customersRes = firebaseApi.getCustomersByUser(equalTo = "\"$userId\"", auth = token)
                }
            }
            if (customersRes.isSuccessful && customersRes.body() != null) {
                val cloudCustomers = customersRes.body()!!.map { (key, dto) ->
                    CustomerEntity(
                        id = key,
                        userId = dto.userId,
                        name = dto.name,
                        phone = dto.phone,
                        email = dto.email,
                        balance = dto.balance,
                        type = dto.type,
                        photoURL = dto.photoURL,
                        gst = dto.gst,
                        dueDate = dto.dueDate,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        syncStatus = Constants.SYNC_STATUS_SYNCED
                    )
                }
                customerDao.insertOrUpdateAll(cloudCustomers)
            }

            var txRes = firebaseApi.getTransactionsByUser(equalTo = "\"$userId\"", auth = token)
            if (txRes.code() == 401) {
                val fresh = refreshToken()
                if (fresh != null) {
                    token = fresh
                    txRes = firebaseApi.getTransactionsByUser(equalTo = "\"$userId\"", auth = token)
                }
            }
            if (txRes.isSuccessful && txRes.body() != null) {
                val cloudTxs = txRes.body()!!.map { (key, dto) ->
                    TransactionEntity(
                        id = key,
                        clientTxId = dto.clientTxId ?: key,
                        userId = dto.userId,
                        customerId = dto.customerId,
                        amount = dto.amount,
                        type = dto.type,
                        description = dto.description,
                        date = dto.date,
                        timestamp = dto.timestamp,
                        balance = dto.balance,
                        attachment = dto.attachment ?: dto.attachments.firstOrNull(),
                        syncStatus = Constants.SYNC_STATUS_SYNCED
                    )
                }
                transactionDao.insertOrUpdateAll(cloudTxs)
            }
        } catch (_: Exception) {
        }
    }
}
