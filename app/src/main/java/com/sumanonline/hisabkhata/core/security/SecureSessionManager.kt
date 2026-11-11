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
    val photoURL: String = ""
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
            photoURL = photoURL
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
}
