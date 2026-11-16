package com.sumanonline.hisabkhata.feature.payments.model

import java.util.UUID

data class DetectedPaymentInfo(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val senderName: String? = null,
    val transactionRef: String? = null,
    val sourceApp: String,
    val sourcePackage: String,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val upiId: String? = null,
    val rawTitle: String = "",
    val rawText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
