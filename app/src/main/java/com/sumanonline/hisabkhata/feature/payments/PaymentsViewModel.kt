package com.sumanonline.hisabkhata.feature.payments

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sumanonline.hisabkhata.core.database.dao.PendingPaymentDao
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val pendingPaymentDao: PendingPaymentDao,
    private val syncEngine: SyncEngine,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    val session = sessionManager.sessionState

    @OptIn(ExperimentalCoroutinesApi::class)
    val pendingPayments = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                pendingPaymentDao.observePendingPayments(s.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun approvePayment(payment: PendingPaymentEntity) {
        viewModelScope.launch {
            syncEngine.createTransactionOffline(
                customerId = payment.customerId,
                amount = Math.abs(payment.amount),
                type = "GOT",
                description = "Online UPI Payment (UTR: ${payment.transactionId ?: "N/A"})",
                date = com.sumanonline.hisabkhata.core.common.DateUtils.todayIso()
            )
            pendingPaymentDao.updateStatus(payment.id, "approved")
            syncEngine.synchronize()
        }
    }

    fun rejectPayment(paymentId: String) {
        viewModelScope.launch {
            pendingPaymentDao.updateStatus(paymentId, "rejected")
        }
    }
}
