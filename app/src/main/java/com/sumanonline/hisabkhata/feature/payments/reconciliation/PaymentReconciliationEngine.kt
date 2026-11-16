package com.sumanonline.hisabkhata.feature.payments.reconciliation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sumanonline.hisabkhata.MainActivity
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.PendingPaymentDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import com.sumanonline.hisabkhata.core.network.BackendApiService
import com.sumanonline.hisabkhata.core.network.model.EmailRequest
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.sync.SyncEngine
import com.sumanonline.hisabkhata.feature.payments.model.DetectedPaymentInfo
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentRequest
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Singleton
class PaymentReconciliationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingPaymentDao: PendingPaymentDao,
    private val customerDao: CustomerDao,
    private val syncEngine: SyncEngine,
    private val sessionManager: SecureSessionManager,
    private val backendApi: BackendApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("hkp_payment_reconciliation", Context.MODE_PRIVATE)

    private val _activeRequest = MutableStateFlow<QrPaymentRequest?>(null)
    val activeRequest: StateFlow<QrPaymentRequest?> = _activeRequest.asStateFlow()

    companion object {
        private const val TAG = "PaymentReconciliation"
        private const val CHANNEL_ID = "hkp_payment_detection_channel"
        private const val KEY_PROCESSED_UTRS = "processed_utrs"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        const val PENDING_EXPIRY_MILLIS = 5 * 60 * 1000L
    }

    init {
        createNotificationChannel()
        cleanupExpiredPendingPayments()
    }

    fun cleanupExpiredPendingPayments() {
        scope.launch {
            try {

                pendingPaymentDao.deleteAllPendingAndCancelled()
                val current = _activeRequest.value
                if (current != null && current.isExpired) {
                    _activeRequest.value = current.copy(status = QrPaymentStatus.PAYMENT_EXPIRED)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clean up pending payments", e)
            }
        }
    }

    fun isMonitoringEnabled(): Boolean {
        return prefs.getBoolean(KEY_MONITORING_ENABLED, true)
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MONITORING_ENABLED, enabled).apply()
    }

    fun isUtrProcessed(utr: String?): Boolean {
        if (utr.isNullOrBlank()) return false
        val set = prefs.getStringSet(KEY_PROCESSED_UTRS, emptySet()) ?: emptySet()
        return set.contains(utr.trim())
    }

    fun markUtrProcessed(utr: String?) {
        if (utr.isNullOrBlank()) return
        val set = prefs.getStringSet(KEY_PROCESSED_UTRS, emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(utr.trim())
        prefs.edit().putStringSet(KEY_PROCESSED_UTRS, set).apply()
    }

    fun createPaymentRequest(
        customer: CustomerEntity,
        amount: Double,
        isCustomAmount: Boolean = false,
        upiUri: String = ""
    ): QrPaymentRequest {
        val userId = sessionManager.getUserId()
        val requestId = "qr_${System.currentTimeMillis()}"

        val request = QrPaymentRequest(
            id = requestId,
            merchantId = userId,
            customerId = customer.id,
            customerName = customer.name,
            amount = amount,
            isCustomAmount = isCustomAmount,
            upiUri = upiUri,
            requestedAt = System.currentTimeMillis(),
            status = QrPaymentStatus.WAITING_FOR_PAYMENT
        )

        _activeRequest.value = request
        return request
    }

    fun cancelPaymentRequest(requestId: String) {
        val current = _activeRequest.value
        if (current?.id == requestId) {
            _activeRequest.value = current.copy(status = QrPaymentStatus.CANCELLED)
        }
        scope.launch {
            try {
                pendingPaymentDao.deleteById(requestId)
            } catch (_: Exception) {}
        }
    }

    fun onPaymentDetected(detected: DetectedPaymentInfo) {
        if (!isMonitoringEnabled()) {
            Log.d(TAG, "Payment detected but monitoring is disabled by merchant")
            return
        }

        val utr = detected.transactionRef
        if (!utr.isNullOrBlank() && isUtrProcessed(utr)) {
            Log.w(TAG, "Ignored duplicate payment notification with already processed UTR: $utr")
            return
        }

        scope.launch {
            val current = _activeRequest.value

            if (current != null &&
                (current.status == QrPaymentStatus.WAITING_FOR_PAYMENT || current.status == QrPaymentStatus.PAYMENT_DETECTED) &&
                !current.isExpired &&
                abs(current.amount - detected.amount) < 0.01
            ) {
                Log.i(TAG, "Matched payment directly to active QR request for customer: ${current.customerName}")
                val updated = current.copy(
                    status = QrPaymentStatus.PAYMENT_DETECTED,
                    detectedPayment = detected,
                    manualUtr = utr
                )
                _activeRequest.value = updated

                pendingPaymentDao.updateStatusAndTxId(current.id, "payment_detected", utr)
                showPaymentDetectedNotification(updated, detected)
                return@launch
            }

            val userId = sessionManager.getUserId()
            if (userId.isNotBlank()) {
                val pendingList = pendingPaymentDao.getPendingPayments(userId)
                val matches = pendingList.filter {
                    abs(it.amount - detected.amount) < 0.01 &&
                            (System.currentTimeMillis() - it.timestamp) < (30 * 60 * 1000L)
                }

                if (matches.size == 1) {
                    val match = matches.first()
                    Log.i(TAG, "Unambiguous background match found for customer ${match.customerName}")
                    pendingPaymentDao.updateStatusAndTxId(match.id, "payment_detected", utr)

                    val matchedRequest = QrPaymentRequest(
                        id = match.id,
                        merchantId = match.merchantId,
                        customerId = match.customerId,
                        customerName = match.customerName,
                        amount = match.amount,
                        status = QrPaymentStatus.PAYMENT_DETECTED,
                        detectedPayment = detected,
                        manualUtr = utr
                    )
                    _activeRequest.value = matchedRequest
                    showPaymentDetectedNotification(matchedRequest, detected)
                } else if (matches.size > 1) {
                    Log.w(TAG, "Multiple ambiguous pending payments found for amount ${detected.amount}. Manual reconciliation required.")
                    showAmbiguousPaymentNotification(detected, matches.size)
                } else {
                    Log.d(TAG, "Payment detected of amount ${detected.amount} but no pending request matched.")
                }
            }
        }
    }

    suspend fun confirmAndCreditLedger(requestId: String, manualUtr: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val current = _activeRequest.value
        val request = if (current?.id == requestId) current else {
            val entity = pendingPaymentDao.getPaymentById(requestId)
                ?: return@withContext Result.failure(IllegalStateException("Payment request not found"))
            QrPaymentRequest(
                id = entity.id,
                merchantId = entity.merchantId,
                customerId = entity.customerId,
                customerName = entity.customerName,
                amount = entity.amount,
                status = QrPaymentStatus.PAYMENT_DETECTED,
                manualUtr = entity.transactionId
            )
        }

        val finalUtr = manualUtr?.trim()
            ?: request.detectedPayment?.transactionRef
            ?: request.manualUtr
            ?: "UPI_${System.currentTimeMillis()}"

        if (isUtrProcessed(finalUtr)) {
            return@withContext Result.failure(IllegalStateException("Transaction reference $finalUtr has already been credited! Duplicate prevented."))
        }

        try {
            val detected = request.detectedPayment
            val source = detected?.sourceApp?.ifBlank { "BHIM UPI" } ?: "BHIM UPI"
            val rawNotification = detected?.rawText?.ifBlank { detected.rawTitle }

            val dateTimeFormat = SimpleDateFormat("dd-MMM-yyyy_hh:mma", Locale.ENGLISH)
            val dateFormatted = dateTimeFormat.format(Date(detected?.timestamp ?: System.currentTimeMillis()))

            val note = if (!rawNotification.isNullOrBlank()) {
                val cleanNotification = rawNotification.trim().trimEnd('.')
                "Online Payment via $source (Note: $cleanNotification. at date and time($dateFormatted))"
            } else {
                "Online Payment via $source (Ref: $finalUtr at date and time($dateFormatted))"
            }

            val txId = syncEngine.createTransactionOffline(
                customerId = request.customerId,
                amount = Math.abs(request.amount),
                type = Constants.TX_TYPE_GOT,
                description = note,
                date = DateUtils.todayIso()
            )

            markUtrProcessed(finalUtr)

            pendingPaymentDao.insert(
                PendingPaymentEntity(
                    id = requestId,
                    merchantId = request.merchantId,
                    customerId = request.customerId,
                    customerName = request.customerName,
                    amount = request.amount,
                    transactionId = finalUtr,
                    screenshot = null,
                    status = "approved",
                    timestamp = System.currentTimeMillis()
                )
            )

            val finished = request.copy(
                status = QrPaymentStatus.LEDGER_CREDITED,
                manualUtr = finalUtr,
                creditTransactionId = txId
            )
            _activeRequest.value = finished

            try {
                syncEngine.synchronize()
            } catch (_: Exception) {}

            dispatchCustomerTransactionEmail(
                customerId = request.customerId,
                amount = Math.abs(request.amount),
                description = note
            )

            Log.i(TAG, "Successfully credited customer ledger with txId: $txId")
            Result.success(txId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to credit customer ledger", e)
            Result.failure(e)
        }
    }

    private fun dispatchCustomerTransactionEmail(
        customerId: String,
        amount: Double,
        description: String
    ) {
        scope.launch(Dispatchers.IO) {
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
                        String.format(Locale.US, "%.2f", absAmt)
                    }
                    val currentBal = customer.balance

                    val emailReq = EmailRequest(
                        to_email = customerEmail,
                        to_name = customer.name,
                        customer_name = customer.name,
                        subject = "Transaction Alert: ₹$formattedAmt - $merchantBusinessName",
                        message = "A new payment amount of ₹$formattedAmt has been recorded on your account. Please check the details below:",
                        merchant_name = merchantBusinessName,
                        merchant_phone = merchantPhone,
                        amount = absAmt,
                        balance = currentBal,
                        is_debit = currentBal < 0,
                        is_due = currentBal < 0,
                        tx_type = "Payment Received",
                        description = description.trim(),
                        action_url = "${Constants.WEB_PORTAL_BASE_URL}customer/share/$customerId",
                        type = "TRANSACTION"
                    )
                    val response = backendApi.sendEmail(emailReq)
                    if (response.isSuccessful) {
                        Log.i(TAG, "Payment collection transaction email sent successfully to $customerEmail (₹$formattedAmt)")
                    } else {
                        Log.w(TAG, "Payment collection email API returned error: code=${response.code()}, message=${response.message()}")
                    }
                } else {
                    Log.d(TAG, "Skipping payment collection email: customer has no valid email registered (customerId=$customerId)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while sending payment collection transaction email for customerId=$customerId", e)
            }
        }
    }

    suspend fun rejectPayment(requestId: String, reason: String? = "Merchant indicated payment not received") = withContext(Dispatchers.IO) {
        try {
            pendingPaymentDao.deleteById(requestId)
            val current = _activeRequest.value
            if (current?.id == requestId) {
                _activeRequest.value = current.copy(
                    status = QrPaymentStatus.PAYMENT_REJECTED,
                    rejectionReason = reason
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject payment request", e)
        }
    }

    fun clearActiveRequest() {
        _activeRequest.value = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Payment Detection Alerts"
            val descriptionText = "Alerts when incoming UPI payments are detected for customer requests"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showPaymentDetectedNotification(request: QrPaymentRequest, detected: DetectedPaymentInfo) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                request.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo_round)
                .setContentTitle("Payment Detected: ₹${detected.amount}")
                .setContentText("Payment received for ${request.customerName} via ${detected.sourceApp}. Tap to verify and credit ledger.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "Received ₹${detected.amount} for ${request.customerName} via ${detected.sourceApp}.\nRef: ${detected.transactionRef ?: "N/A"}\nPlease verify in your bank app and confirm to credit ledger."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(request.id.hashCode(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show payment detection notification", e)
        }
    }

    private fun showAmbiguousPaymentNotification(detected: DetectedPaymentInfo, matchCount: Int) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                detected.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo_round)
                .setContentTitle("Payment Detected: ₹${detected.amount}")
                .setContentText("Found $matchCount pending requests with this amount. Tap to reconcile.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(detected.id.hashCode(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show ambiguous payment notification", e)
        }
    }
}
