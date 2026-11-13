package com.sumanonline.hisabkhata.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.DueStatus
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.security.UserSession
import com.sumanonline.hisabkhata.core.sync.SyncEngine
import com.sumanonline.hisabkhata.core.network.BackendApiService
import com.sumanonline.hisabkhata.core.network.model.EmailRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

enum class CustomerFilter {
    ALL,
    YOULL_GET,
    YOULL_GIVE,
    SETTLED,
    DUE_TODAY,
    OVERDUE
}

enum class CustomerSort {
    RECENT,
    NAME,
    AMOUNT_HIGH,
    AMOUNT_LOW
}

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val syncEngine: SyncEngine,
    private val sessionManager: SecureSessionManager,
    private val backendApi: BackendApiService
) : ViewModel() {

    val pendingSyncCount: StateFlow<Int> = syncEngine.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(CustomerFilter.ALL)
    val currentFilter: StateFlow<CustomerFilter> = _currentFilter.asStateFlow()

    private val _currentSort = MutableStateFlow(CustomerSort.RECENT)
    val currentSort: StateFlow<CustomerSort> = _currentSort.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rawCustomers: StateFlow<List<CustomerEntity>> = sessionManager.sessionState
        .flatMapLatest { session ->
            if (session.userId.isNotBlank()) {
                customerDao.observeCustomersByUser(session.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            rawCustomers.collect {
                _isLoading.value = false
            }
        }
    }

    val totalGet: StateFlow<BigDecimal> = rawCustomers.combine(_searchQuery) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { c ->
            if (c.balance < 0.0) {
                sum = sum.add(BigDecimal.valueOf(Math.abs(c.balance)))
            }
        }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val totalGive: StateFlow<BigDecimal> = rawCustomers.combine(_searchQuery) { list, _ ->
        var sum = BigDecimal.ZERO
        list.forEach { c ->
            if (c.balance > 0.0) {
                sum = sum.add(BigDecimal.valueOf(c.balance))
            }
        }
        sum
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val sessionState: StateFlow<UserSession> = sessionManager.sessionState

    val partiesCount: StateFlow<Int> = rawCustomers.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val givePartiesCount: StateFlow<Int> = rawCustomers.map { list -> list.count { it.balance > 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val getPartiesCount: StateFlow<Int> = rawCustomers.map { list -> list.count { it.balance < 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overduePartiesCount: StateFlow<Int> = rawCustomers.map { list ->
        list.count { DateUtils.getDueDateStatus(it.dueDate) is DueStatus.Overdue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val netReceivableBalance: StateFlow<BigDecimal> = combine(totalGet, totalGive) { get, give ->
        get.subtract(give)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    fun logout() {
        sessionManager.clearSession()
    }

    val filteredCustomers: StateFlow<List<CustomerEntity>> = combine(
        rawCustomers,
        _searchQuery,
        _currentFilter,
        _currentSort
    ) { customers, query, filter, sort ->
        var filtered = if (query.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }

        filtered = when (filter) {
            CustomerFilter.ALL -> filtered
            CustomerFilter.YOULL_GET -> filtered.filter { it.balance < 0.0 }
            CustomerFilter.YOULL_GIVE -> filtered.filter { it.balance > 0.0 }
            CustomerFilter.SETTLED -> filtered.filter { it.balance == 0.0 }
            CustomerFilter.DUE_TODAY -> filtered.filter {
                DateUtils.getDueDateStatus(it.dueDate) is DueStatus.DueToday
            }
            CustomerFilter.OVERDUE -> filtered.filter {
                DateUtils.getDueDateStatus(it.dueDate) is DueStatus.Overdue
            }
        }

        when (sort) {
            CustomerSort.RECENT -> filtered.sortedByDescending { it.updatedAt }
            CustomerSort.NAME -> filtered.sortedBy { it.name.lowercase() }
            CustomerSort.AMOUNT_HIGH -> filtered.sortedByDescending { Math.abs(it.balance) }
            CustomerSort.AMOUNT_LOW -> filtered.sortedBy { Math.abs(it.balance) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: CustomerFilter) {
        _currentFilter.value = filter
    }

    fun setSort(sort: CustomerSort) {
        _currentSort.value = sort
    }

    fun triggerSync(onComplete: ((success: Boolean, message: String) -> Unit)? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            val success = syncEngine.synchronize()
            _isSyncing.value = false
            if (onComplete != null) {
                val msg = if (success) "Sync complete. All entries up to date." else "Some entries could not sync. Will retry automatically."
                onComplete(success, msg)
            }
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        email: String,
        openingBalance: Double,
        balanceType: String,
        type: String,
        photoURL: String? = null,
        gst: String? = null,
        dueDate: String? = null
    ) {
        viewModelScope.launch {
            val signedBalance = if (balanceType.equals("get", ignoreCase = true)) {
                -Math.abs(openingBalance)
            } else if (balanceType.equals("give", ignoreCase = true)) {
                Math.abs(openingBalance)
            } else {
                0.0
            }

            syncEngine.createCustomerOffline(
                name = name,
                phone = phone,
                email = email,
                openingBalance = signedBalance,
                type = type,
                photoURL = photoURL,
                gst = gst,
                dueDate = dueDate
            )
            triggerSync()
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            syncEngine.updateCustomerOffline(customer)
            triggerSync()
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            syncEngine.deleteCustomerOffline(customerId)
            triggerSync()
        }
    }

    fun setCustomerDueDate(customerId: String, days: Int) {
        viewModelScope.launch {
            val dueDateStr = DateUtils.addDaysToToday(days)
            customerDao.updateDueDate(customerId, dueDateStr)
            val customer = customerDao.getCustomerById(customerId)
            if (customer != null) {
                syncEngine.updateCustomerOffline(customer)
            }
        }
    }

    fun clearCustomerDueDate(customerId: String) {
        viewModelScope.launch {
            customerDao.updateDueDate(customerId, null)
            val customer = customerDao.getCustomerById(customerId)
            if (customer != null) {
                syncEngine.updateCustomerOffline(customer)
            }
        }
    }

    fun getTransactionsForCustomer(customerId: String): StateFlow<List<TransactionEntity>> {
        return transactionDao.observeTransactionsByCustomer(customerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    suspend fun uploadBillToR2(
        imageBytes: ByteArray,
        extension: String = "jpg",
        customerId: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cleanExt = if (extension.equals("png", ignoreCase = true)) "png" else "jpg"
            val mime = if (cleanExt == "png") "image/png" else "image/jpeg"
            val b64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            val dataUri = "data:$mime;base64,$b64"
            val custPrefix = if (!customerId.isNullOrBlank()) "tx_${customerId}" else "tx"
            val filename = "${custPrefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$cleanExt"

            // 1. Primary method: JSON upload (exact same as HisabKhata React app)
            val jsonReq = com.sumanonline.hisabkhata.core.network.model.JsonUploadRequest(
                image = dataUri,
                folder = "transaction_attachments",
                filename = filename
            )
            val jsonRes = backendApi.uploadFileJson(jsonReq)
            if (jsonRes.isSuccessful && jsonRes.body()?.success == true && !jsonRes.body()?.url.isNullOrBlank()) {
                return@withContext jsonRes.body()?.url
            }

            // 2. Secondary fallback: Multipart upload
            val mediaType = mime.toMediaTypeOrNull()
            val requestBody = imageBytes.toRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("file", filename, requestBody)
            val folderPart = "transaction_attachments".toRequestBody("text/plain".toMediaTypeOrNull())

            val mpRes = backendApi.uploadFile(filePart, folderPart)
            if (mpRes.isSuccessful && mpRes.body()?.success == true && !mpRes.body()?.url.isNullOrBlank()) {
                return@withContext mpRes.body()?.url
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    fun addTransaction(
        customerId: String,
        amount: Double,
        isGave: Boolean,
        description: String,
        date: String,
        attachment: String? = null,
        attachments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val signedAmount = if (isGave) {
                -Math.abs(amount)
            } else {
                Math.abs(amount)
            }
            val txType = if (isGave) Constants.TX_TYPE_GAVE else Constants.TX_TYPE_GOT

            syncEngine.createTransactionOffline(
                customerId = customerId,
                amount = signedAmount,
                type = txType,
                description = description,
                date = date,
                attachment = attachment,
                attachments = attachments
            )
            triggerSync()

            // Dispatch transaction alert email (matches HisabKhata React project dbService.addTransaction)
            try {
                val customer = customerDao.getCustomerById(customerId)
                val customerEmail = customer?.email?.trim()
                if (!customerEmail.isNullOrBlank() && customerEmail.contains("@")) {
                    val session = sessionManager.sessionState.value
                    val merchantBusinessName = session.businessName.ifBlank {
                        session.displayName.ifBlank { "HisabKhata Merchant" }
                    }
                    val merchantPhone = session.phone
                    val absAmt = Math.abs(amount)
                    val formattedAmt = if (absAmt % 1.0 == 0.0) {
                        absAmt.toLong().toString()
                    } else {
                        String.format(java.util.Locale.US, "%.2f", absAmt)
                    }
                    val typeStr = if (!isGave) "Payment Received" else "Credit Given"
                    val currentBal = customer.balance

                    val emailReq = EmailRequest(
                        to_email = customerEmail,
                        to_name = customer.name,
                        customer_name = customer.name,
                        subject = "Transaction Alert: ₹$formattedAmt - $merchantBusinessName",
                        message = "A new transaction amount of ₹$formattedAmt has been recorded on your account. Please check the details below:",
                        merchant_name = merchantBusinessName,
                        merchant_phone = merchantPhone,
                        amount = signedAmount,
                        balance = currentBal,
                        is_debit = currentBal < 0,
                        is_due = currentBal < 0,
                        tx_type = typeStr,
                        description = description.trim(),
                        action_url = "${Constants.WEB_PORTAL_BASE_URL}customer/share/$customerId",
                        type = "TRANSACTION"
                    )
                    backendApi.sendEmail(emailReq)
                }
            } catch (_: Exception) {
                // Keep local transaction saving resilient to email/network failures
            }
        }
    }

    fun deleteTransaction(customerId: String, txId: String, amount: Double) {
        viewModelScope.launch {
            syncEngine.deleteTransactionOffline(customerId, txId, amount)
            triggerSync()
        }
    }

    fun updateTransaction(
        customerId: String,
        txId: String,
        oldAmount: Double,
        newAmount: Double,
        isGave: Boolean,
        description: String,
        date: String,
        attachment: String? = null,
        attachments: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val signedNewAmount = if (isGave) -Math.abs(newAmount) else Math.abs(newAmount)
            val txType = if (isGave) Constants.TX_TYPE_GAVE else Constants.TX_TYPE_GOT

            syncEngine.updateTransactionOffline(
                customerId = customerId,
                txId = txId,
                oldAmount = oldAmount,
                newAmount = signedNewAmount,
                type = txType,
                description = description,
                date = date,
                attachment = attachment,
                attachments = attachments
            )
            triggerSync()
        }
    }

    fun getCustomerById(customerId: String) = customerDao.observeCustomerById(customerId)

    fun sendEmailReminder(
        customer: CustomerEntity,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        if (customer.email.isBlank()) {
            onResult(false, "No email address registered for ${customer.name}")
            return
        }

        viewModelScope.launch {
            try {
                val session = sessionManager.sessionState.value
                val merchantName = session.businessName.ifBlank { session.displayName }
                val request = EmailRequest(
                    to_email = customer.email,
                    customer_name = customer.name,
                    merchant_name = merchantName,
                    merchant_phone = session.phone,
                    amount = Math.abs(customer.balance),
                    balance = customer.balance,
                    tx_type = "Payment Reminder",
                    action_url = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}",
                    type = "PAYMENT_REMINDER"
                )
                val response = backendApi.sendEmail(request)
                if (response.isSuccessful) {
                    onResult(true, "Payment reminder email sent directly to ${customer.email}!")
                } else {
                    val errMsg = response.errorBody()?.string()?.ifBlank { null } ?: response.message()
                    onResult(false, "Failed to send email: $errMsg")
                }
            } catch (e: Exception) {
                onResult(false, "Error sending email: ${e.localizedMessage ?: e.message}")
            }
        }
    }
}
