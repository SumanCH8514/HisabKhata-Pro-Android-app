package com.sumanonline.hisabkhata.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val success: Boolean,
    val url: String? = null,
    val key: String? = null,
    val folder: String? = null,
    val filename: String? = null,
    val error: String? = null
)

@Serializable
data class EmailRequest(
    val to_email: String,
    val to_name: String? = null,
    val customer_name: String? = null,
    val merchant_name: String? = null,
    val merchant_phone: String? = null,
    val subject: String? = null,
    val message: String? = null,
    val amount: Double? = null,
    val balance: Double? = null,
    val tx_type: String? = null,
    val description: String? = null,
    val action_url: String? = null,
    val type: String,
    val otp: String? = null
)

@Serializable
data class EmailResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class BackendHealthResponse(
    val status: String,
    val r2_connected: Boolean = false,
    val timestamp: String = ""
)

@Serializable
data class FirebaseCustomerDto(
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val balance: Double = 0.0,
    val type: String = "Customer",
    val photoURL: String? = null,
    val gst: String? = null,
    val dueDate: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class FirebaseTransactionDto(
    val userId: String = "",
    val customerId: String = "",
    val amount: Double = 0.0,
    val type: String = "GAVE",
    val description: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val balance: Double = 0.0,
    val attachment: String? = null,
    val attachments: List<String> = emptyList(),
    val clientTxId: String? = null
)

@Serializable
data class FirebaseUserDto(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "user",
    val businessName: String = "",
    val businessCategory: String = "",
    val businessType: String = "",
    val businessAddress: String = "",
    val upiId: String = "",
    val pan: String = "",
    val gst: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val photoURL: String = "",
    val emailOtpVerification: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class FirebaseAuthSignInRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
data class FirebaseAuthSignUpRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
data class FirebaseAuthResponse(
    val idToken: String = "",
    val email: String = "",
    val refreshToken: String = "",
    val expiresIn: String = "",
    val localId: String = "",
    val registered: Boolean = false
)

@Serializable
data class FirebasePasswordResetRequest(
    val requestType: String = "PASSWORD_RESET",
    val email: String
)

@Serializable
data class FirebasePasswordResetResponse(
    val email: String = ""
)

@Serializable
data class FirebaseSignInWithIdpRequest(
    val postBody: String,
    val requestUri: String = "http://localhost",
    val returnSecureToken: Boolean = true,
    val returnIdpCredential: Boolean = true
)

@Serializable
data class FirebaseSignInWithIdpResponse(
    val idToken: String = "",
    val email: String = "",
    val refreshToken: String = "",
    val expiresIn: String = "",
    val localId: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isNewUser: Boolean = false
)

@Serializable
data class FirebaseTokenRefreshResponse(
    @SerialName("expires_in") val expiresIn: String = "",
    @SerialName("token_type") val tokenType: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("id_token") val idToken: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("project_id") val projectId: String = ""
)

@Serializable
data class FirebasePendingPaymentDto(
    val merchantId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val amount: Double = 0.0,
    val transactionId: String? = null,
    val screenshot: String? = null,
    val status: String = "pending",
    val timestamp: Long = 0L
)

