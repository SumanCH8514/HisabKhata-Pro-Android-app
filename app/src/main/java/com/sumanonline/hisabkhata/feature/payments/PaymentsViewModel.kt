package com.sumanonline.hisabkhata.feature.payments

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.PendingPaymentDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import com.sumanonline.hisabkhata.core.network.FirebaseApiService
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.sync.SyncEngine
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

enum class PaymentFilter {
    ALL, PENDING, APPROVED, REJECTED
}

data class PaymentSummary(
    val allCount: Int = 0,
    val pendingCount: Int = 0,
    val pendingTotal: Double = 0.0,
    val approvedCount: Int = 0,
    val approvedTotal: Double = 0.0,
    val rejectedCount: Int = 0,
    val rejectedTotal: Double = 0.0
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val pendingPaymentDao: PendingPaymentDao,
    private val customerDao: CustomerDao,
    private val syncEngine: SyncEngine,
    private val firebaseApi: FirebaseApiService,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    val session = sessionManager.sessionState

    private val _selectedFilter = MutableStateFlow(PaymentFilter.ALL)
    val selectedFilter: StateFlow<PaymentFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPayments: StateFlow<List<PendingPaymentEntity>> = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                pendingPaymentDao.observePendingPayments(s.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val customerMap: StateFlow<Map<String, CustomerEntity>> = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                customerDao.observeCustomersByUser(s.userId).map { list ->
                    list.associateBy { it.id }
                }
            } else {
                flowOf(emptyMap())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val summary: StateFlow<PaymentSummary> = allPayments.map { list ->
        val pending = list.filter { it.status.equals("pending", ignoreCase = true) }
        val approved = list.filter { it.status.equals("approved", ignoreCase = true) }
        val rejected = list.filter { it.status.equals("rejected", ignoreCase = true) }
        PaymentSummary(
            allCount = list.size,
            pendingCount = pending.size,
            pendingTotal = pending.sumOf { it.amount },
            approvedCount = approved.size,
            approvedTotal = approved.sumOf { it.amount },
            rejectedCount = rejected.size,
            rejectedTotal = rejected.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentSummary())

    val filteredPayments: StateFlow<List<PendingPaymentEntity>> = combine(
        allPayments,
        _selectedFilter,
        _searchQuery
    ) { payments, filter, query ->
        payments.filter { payment ->
            val matchesFilter = when (filter) {
                PaymentFilter.ALL -> true
                PaymentFilter.PENDING -> payment.status.equals("pending", ignoreCase = true)
                PaymentFilter.APPROVED -> payment.status.equals("approved", ignoreCase = true)
                PaymentFilter.REJECTED -> payment.status.equals("rejected", ignoreCase = true)
            }
            val q = query.trim()
            val matchesQuery = if (q.isBlank()) {
                true
            } else {
                payment.customerName.contains(q, ignoreCase = true) ||
                        (payment.transactionId?.contains(q, ignoreCase = true) == true) ||
                        payment.amount.toString().contains(q)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customAmount = MutableStateFlow("")
    val customAmount: StateFlow<String> = _customAmount.asStateFlow()

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    init {
        viewModelScope.launch {
            session.collect { s ->
                generateQr(s.upiId, _customAmount.value)
            }
        }
        refreshPayments()
    }

    fun setFilter(filter: PaymentFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshPayments() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                syncEngine.refreshDataFromCloud()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setCustomAmount(amount: String) {
        _customAmount.value = amount
        generateQr(session.value.upiId, amount)
    }

    fun generateQr(upiId: String, amount: String) {
        viewModelScope.launch {
            if (upiId.isBlank()) {
                _qrBitmap.value = null
                return@launch
            }
            val merchantName = session.value.businessName.ifBlank { session.value.displayName }
            val cleanName = java.net.URLEncoder.encode(merchantName, "UTF-8")
            val amountParam = if (amount.toDoubleOrNull() != null && amount.toDouble() > 0) "&am=$amount" else ""
            val upiUri = "upi://pay?pa=$upiId&pn=$cleanName$amountParam&cu=INR"

            val bitmap = withContext(Dispatchers.Default) {
                try {
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, 512, 512)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                        }
                    }
                    bmp
                } catch (_: Exception) {
                    null
                }
            }
            _qrBitmap.value = bitmap
        }
    }

    fun approvePayment(payment: PendingPaymentEntity, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                // 1. Create offline transaction in Customer Ledger
                syncEngine.createTransactionOffline(
                    customerId = payment.customerId,
                    amount = Math.abs(payment.amount),
                    type = "GOT",
                    description = "Online UPI Payment (UTR: ${payment.transactionId ?: "N/A"})",
                    date = com.sumanonline.hisabkhata.core.common.DateUtils.todayIso()
                )
                // 2. Update local payment status
                pendingPaymentDao.updateStatus(payment.id, "approved")

                // 3. Patch status in Firebase
                val token = sessionManager.getIdToken()
                if (token.isNotBlank()) {
                    val patchObj = buildJsonObject {
                        put("status", JsonPrimitive("approved"))
                    }
                    try {
                        firebaseApi.patchPendingPayment(payment.id, patchObj, token)
                    } catch (_: Exception) {}
                }

                // 4. Push sync to server
                syncEngine.synchronize()
                onComplete?.invoke(true)
            } catch (_: Exception) {
                onComplete?.invoke(false)
            }
        }
    }

    fun rejectPayment(paymentId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                pendingPaymentDao.updateStatus(paymentId, "rejected")
                val token = sessionManager.getIdToken()
                if (token.isNotBlank()) {
                    val patchObj = buildJsonObject {
                        put("status", JsonPrimitive("rejected"))
                    }
                    try {
                        firebaseApi.patchPendingPayment(paymentId, patchObj, token)
                    } catch (_: Exception) {}
                }
                onComplete?.invoke(true)
            } catch (_: Exception) {
                onComplete?.invoke(false)
            }
        }
    }
}
