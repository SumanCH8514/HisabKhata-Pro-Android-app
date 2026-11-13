package com.sumanonline.hisabkhata.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.AppPreferencesManager
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.LedgerExportHelper
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import com.sumanonline.hisabkhata.core.network.FirebaseApiService
import com.sumanonline.hisabkhata.core.network.model.FirebasePasswordResetRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseUserDto
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.security.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseApi: FirebaseApiService,
    private val sessionManager: SecureSessionManager,
    val preferencesManager: AppPreferencesManager,
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val session: StateFlow<UserSession> = sessionManager.sessionState

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            try {
                val token = sessionManager.getIdToken()
                val userId = sessionManager.getUserId()
                if (token.isNotBlank() && userId.isNotBlank()) {
                    val res = firebaseApi.getUserProfile(userId, token)
                    if (res.isSuccessful && res.body() != null) {
                        val profile = res.body()!!
                        val current = sessionManager.sessionState.value
                        sessionManager.saveSession(
                            current.copy(
                                displayName = profile.name.ifBlank { current.displayName },
                                businessName = profile.businessName.ifBlank { current.businessName },
                                phone = profile.phone.ifBlank { current.phone },
                                upiId = profile.upiId.ifBlank { current.upiId },
                                photoURL = profile.photoURL.ifBlank { current.photoURL },
                                businessAddress = profile.businessAddress.ifBlank { current.businessAddress },
                                businessCategory = profile.businessCategory.ifBlank { current.businessCategory },
                                businessType = profile.businessType.ifBlank { current.businessType },
                                gstin = profile.gst.ifBlank { current.gstin },
                                bankAccount = profile.accountNumber.ifBlank { current.bankAccount }
                            )
                        )
                        if (profile.emailOtpVerification != preferencesManager.preferences.value.emailOtpVerification) {
                            preferencesManager.setEmailOtpVerification(profile.emailOtpVerification)
                        }
                        if (profile.twoFactorEnabled != preferencesManager.preferences.value.twoFactorEnabled) {
                            preferencesManager.setTwoFactorEnabled(profile.twoFactorEnabled)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateSingleField(key: String, value: String) {
        viewModelScope.launch {
            _isSaving.value = true
            sessionManager.updateField(key, value)
            try {
                val token = sessionManager.getIdToken()
                val userId = sessionManager.getUserId()
                if (token.isNotBlank() && userId.isNotBlank()) {
                    val fbKey = when (key) {
                        "displayName" -> "name"
                        "gstin" -> "gst"
                        "bankAccount" -> "accountNumber"
                        else -> key
                    }
                    val updates = kotlinx.serialization.json.buildJsonObject {
                        put(fbKey, kotlinx.serialization.json.JsonPrimitive(value.trim()))
                        put("updatedAt", kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()))
                    }
                    firebaseApi.patchUserProfile(userId, updates, token)
                }
            } catch (_: Exception) {
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateProfile(
        name: String,
        businessName: String,
        phone: String,
        upiId: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            sessionManager.updateProfile(name, businessName, phone, upiId)

            try {
                val token = sessionManager.getIdToken()
                val userId = sessionManager.getUserId()
                if (token.isNotBlank() && userId.isNotBlank()) {
                    val updates = kotlinx.serialization.json.buildJsonObject {
                        put("name", kotlinx.serialization.json.JsonPrimitive(name.trim()))
                        put("businessName", kotlinx.serialization.json.JsonPrimitive(businessName.trim()))
                        put("phone", kotlinx.serialization.json.JsonPrimitive(phone.trim()))
                        put("upiId", kotlinx.serialization.json.JsonPrimitive(upiId.trim()))
                        put("updatedAt", kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()))
                    }
                    firebaseApi.patchUserProfile(userId, updates, token)
                }
            } catch (_: Exception) {
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        sessionManager.setBiometricEnabled(enabled)
    }

    fun setEmailOtpVerification(enabled: Boolean) {
        preferencesManager.setEmailOtpVerification(enabled)
        viewModelScope.launch {
            try {
                val token = sessionManager.getIdToken()
                val userId = sessionManager.getUserId()
                if (token.isNotBlank() && userId.isNotBlank()) {
                    val updates = kotlinx.serialization.json.buildJsonObject {
                        put("emailOtpVerification", kotlinx.serialization.json.JsonPrimitive(enabled))
                        put("updatedAt", kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()))
                    }
                    firebaseApi.patchUserProfile(userId, updates, token)
                }
            } catch (_: Exception) {}
        }
    }

    fun setTwoFactorEnabled(enabled: Boolean) {
        preferencesManager.setTwoFactorEnabled(enabled)
        viewModelScope.launch {
            try {
                val token = sessionManager.getIdToken()
                val userId = sessionManager.getUserId()
                if (token.isNotBlank() && userId.isNotBlank()) {
                    val updates = kotlinx.serialization.json.buildJsonObject {
                        put("twoFactorEnabled", kotlinx.serialization.json.JsonPrimitive(enabled))
                        put("updatedAt", kotlinx.serialization.json.JsonPrimitive(System.currentTimeMillis()))
                    }
                    firebaseApi.patchUserProfile(userId, updates, token)
                }
            } catch (_: Exception) {}
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            onResult(false, "Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            try {
                val apiKey = Constants.FIREBASE_API_KEY
                val request = FirebasePasswordResetRequest(email = cleanEmail)
                val response = firebaseApi.sendPasswordReset(apiKey, request)
                if (response.isSuccessful) {
                    onResult(true, "Password reset instructions sent to $cleanEmail")
                } else {
                    val rawError = response.errorBody()?.string() ?: ""
                    val msg = if (rawError.contains("EMAIL_NOT_FOUND")) {
                        "This email is not registered"
                    } else {
                        "Failed to send reset email. Please try again."
                    }
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Network error. Please try again.")
            }
        }
    }

    fun exportAllLedger(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = sessionManager.sessionState.value
            val res = LedgerExportHelper.exportAllLedgerCsv(
                context = context,
                userId = user.userId,
                merchantName = user.displayName.ifBlank { user.businessName }.ifBlank { "User" },
                merchantPhone = user.phone,
                customerDao = customerDao,
                transactionDao = transactionDao
            )
            res.onSuccess { file ->
                LedgerExportHelper.shareExportedFile(context, file)
                val kb = (file.length() / 1024).coerceAtLeast(1)
                onResult(true, "Ledger CSV exported (${kb} KB)")
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Failed to export ledger data")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
