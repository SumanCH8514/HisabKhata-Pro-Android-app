package com.sumanonline.hisabkhata.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.AppPreferencesManager
import com.sumanonline.hisabkhata.core.network.FirebaseApiService
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
    val preferencesManager: AppPreferencesManager
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

    fun logout() {
        sessionManager.clearSession()
    }
}
