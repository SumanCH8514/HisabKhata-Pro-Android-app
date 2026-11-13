package com.sumanonline.hisabkhata.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class UserSession(
    val userId: String = "",
    val idToken: String = "",
    val refreshToken: String = "",
    val email: String = "",
    val displayName: String = "",
    val businessName: String = "",
    val phone: String = "",
    val upiId: String = "",
    val role: String = "user",
    val biometricEnabled: Boolean = false,
    val photoURL: String = "",
    val businessAddress: String = "",
    val businessCategory: String = "",
    val businessType: String = "",
    val gstin: String = "",
    val bankAccount: String = "",
    val staffCount: String = "0"
)

@Singleton
class SecureSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "hisabkhata_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _sessionState = MutableStateFlow(readSession())
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private fun readSession(): UserSession {
        val userId = prefs.getString("user_id", "") ?: ""
        val token = prefs.getString("id_token", "") ?: ""
        val refreshToken = prefs.getString("refresh_token", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val displayName = prefs.getString("display_name", "") ?: ""
        val businessName = prefs.getString("business_name", "") ?: ""
        val phone = prefs.getString("phone", "") ?: ""
        val upiId = prefs.getString("upi_id", "") ?: ""
        val role = prefs.getString("role", "user") ?: "user"
        val biometric = prefs.getBoolean("biometric_enabled", false)
        val photoURL = prefs.getString("photo_url", "") ?: ""
        val businessAddress = prefs.getString("business_address", "") ?: ""
        val businessCategory = prefs.getString("business_category", "") ?: ""
        val businessType = prefs.getString("business_type", "") ?: ""
        val gstin = prefs.getString("gstin", "") ?: ""
        val bankAccount = prefs.getString("bank_account", "") ?: ""
        val staffCount = prefs.getString("staff_count", "0") ?: "0"

        return UserSession(
            userId = userId,
            idToken = token,
            refreshToken = refreshToken,
            email = email,
            displayName = displayName,
            businessName = businessName,
            phone = phone,
            upiId = upiId,
            role = role,
            biometricEnabled = biometric,
            photoURL = photoURL,
            businessAddress = businessAddress,
            businessCategory = businessCategory,
            businessType = businessType,
            gstin = gstin,
            bankAccount = bankAccount,
            staffCount = staffCount
        )
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString("user_id", session.userId)
            .putString("id_token", session.idToken)
            .putString("refresh_token", session.refreshToken)
            .putString("email", session.email)
            .putString("display_name", session.displayName)
            .putString("business_name", session.businessName)
            .putString("phone", session.phone)
            .putString("upi_id", session.upiId)
            .putString("role", session.role)
            .putBoolean("biometric_enabled", session.biometricEnabled)
            .putString("photo_url", session.photoURL)
            .putString("business_address", session.businessAddress)
            .putString("business_category", session.businessCategory)
            .putString("business_type", session.businessType)
            .putString("gstin", session.gstin)
            .putString("bank_account", session.bankAccount)
            .putString("staff_count", session.staffCount)
            .apply()

        _sessionState.value = session
    }

    fun updateTokens(idToken: String, refreshToken: String = "") {
        val current = _sessionState.value
        val updated = current.copy(
            idToken = idToken,
            refreshToken = if (refreshToken.isNotBlank()) refreshToken else current.refreshToken
        )
        saveSession(updated)
    }

    fun getRefreshToken(): String = _sessionState.value.refreshToken

    fun updateProfile(name: String, businessName: String, phone: String, upiId: String, photoURL: String? = null) {
        val current = _sessionState.value
        val updated = current.copy(
            displayName = name,
            businessName = businessName,
            phone = phone,
            upiId = upiId,
            photoURL = photoURL ?: current.photoURL
        )
        saveSession(updated)
    }

    fun updateField(key: String, value: String) {
        val current = _sessionState.value
        val updated = when (key) {
            "displayName" -> current.copy(displayName = value)
            "phone" -> current.copy(phone = value)
            "businessName" -> current.copy(businessName = value)
            "businessAddress" -> current.copy(businessAddress = value)
            "businessCategory" -> current.copy(businessCategory = value)
            "businessType" -> current.copy(businessType = value)
            "gstin" -> current.copy(gstin = value)
            "bankAccount" -> current.copy(bankAccount = value)
            "upiId" -> current.copy(upiId = value)
            "staffCount" -> current.copy(staffCount = value)
            "photoURL" -> current.copy(photoURL = value)
            else -> current
        }
        saveSession(updated)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _sessionState.value = _sessionState.value.copy(biometricEnabled = enabled)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _sessionState.value = UserSession()
    }

    fun isLoggedIn(): Boolean {
        return _sessionState.value.userId.isNotBlank() && _sessionState.value.idToken.isNotBlank()
    }

    fun getUserId(): String = _sessionState.value.userId

    fun getIdToken(): String = _sessionState.value.idToken

    suspend fun getFreshToken(firebaseApi: com.sumanonline.hisabkhata.core.network.FirebaseApiService): String {
        val currentToken = getIdToken()
        val refreshToken = getRefreshToken()
        if (refreshToken.isBlank()) return currentToken
        return try {
            val response = firebaseApi.refreshIdToken(
                apiKey = com.sumanonline.hisabkhata.core.common.Constants.FIREBASE_API_KEY,
                refreshToken = refreshToken
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val newIdToken = body.idToken
                val newRefreshToken = body.refreshToken.ifBlank { refreshToken }
                updateTokens(newIdToken, newRefreshToken)
                newIdToken
            } else {
                currentToken
            }
        } catch (_: Exception) {
            currentToken
        }
    }
}
