package com.sumanonline.hisabkhata.feature.payments.model

enum class QrPaymentStatus {
    WAITING_FOR_PAYMENT,
    PAYMENT_DETECTED,
    VERIFICATION_REQUIRED,
    MERCHANT_CONFIRMED,
    LEDGER_CREDITED,
    PAYMENT_REJECTED,
    PAYMENT_EXPIRED,
    CANCELLED
}

data class QrPaymentRequest(
    val id: String,
    val merchantId: String,
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val isCustomAmount: Boolean = false,
    val upiUri: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = requestedAt + (5 * 60 * 1000L),
    val status: QrPaymentStatus = QrPaymentStatus.WAITING_FOR_PAYMENT,
    val detectedPayment: DetectedPaymentInfo? = null,
    val manualUtr: String? = null,
    val creditTransactionId: String? = null,
    val rejectionReason: String? = null
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt && status == QrPaymentStatus.WAITING_FOR_PAYMENT
}
