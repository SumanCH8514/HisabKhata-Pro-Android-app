package com.sumanonline.hisabkhata.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.Resource
import com.sumanonline.hisabkhata.core.network.FirebaseApiService
import com.sumanonline.hisabkhata.core.network.model.FirebaseAuthSignInRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseAuthSignUpRequest
import com.sumanonline.hisabkhata.core.network.model.FirebasePasswordResetRequest
import com.sumanonline.hisabkhata.core.network.model.FirebaseSignInWithIdpRequest
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.security.UserSession
import com.sumanonline.hisabkhata.core.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

sealed interface AuthEvent {
    data object LoginSuccess : AuthEvent
    data class Error(val message: String) : AuthEvent
    data class PasswordResetSent(val email: String) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseApi: FirebaseApiService,
    private val sessionManager: SecureSessionManager,
    private val syncEngine: SyncEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val uiState: StateFlow<Resource<Unit>> = _uiState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow: SharedFlow<AuthEvent> = _eventFlow.asSharedFlow()

    val currentSession = sessionManager.sessionState

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun clearError() {
        _uiState.value = Resource.Success(Unit)
    }

    fun login(email: String, pass: String) {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            viewModelScope.launch {
                val msg = "Please enter both email and password"
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val apiKey = Constants.FIREBASE_API_KEY
                val request = FirebaseAuthSignInRequest(email = cleanEmail, password = cleanPass)
                val response = firebaseApi.signInWithPassword(apiKey, request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    val profile = try {
                        val profileRes = firebaseApi.getUserProfile(authData.localId, authData.idToken)
                        if (profileRes.isSuccessful) profileRes.body() else null
                    } catch (_: Exception) {
                        null
                    }

                    val session = UserSession(
                        userId = authData.localId,
                        idToken = authData.idToken,
                        refreshToken = authData.refreshToken,
                        email = authData.email,
                        displayName = profile?.name?.ifBlank { null } ?: authData.email.substringBefore("@"),
                        businessName = profile?.businessName?.ifBlank { null } ?: "My Shop",
                        phone = profile?.phone ?: "",
                        upiId = profile?.upiId ?: "",
                        role = profile?.role ?: "user",
                        photoURL = profile?.photoURL ?: ""
                    )
                    sessionManager.saveSession(session)
                    _uiState.value = Resource.Success(Unit)
                    _eventFlow.emit(AuthEvent.LoginSuccess)

                    viewModelScope.launch {
                        try {
                            syncEngine.synchronize()
                        } catch (_: Exception) {
                        }
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseFirebaseError(rawError)
                    _uiState.value = Resource.Error(errorMsg)
                    _eventFlow.emit(AuthEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is UnknownHostException -> "No internet connection. Please check your network."
                    is SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> e.localizedMessage ?: "Network connection error"
                }
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
        }
    }

    fun register(name: String, email: String, pass: String, phone: String, businessName: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        val cleanPhone = phone.trim()
        val cleanBiz = businessName.trim()

        if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPass.isBlank()) {
            viewModelScope.launch {
                val msg = "Please fill in all required fields"
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val apiKey = Constants.FIREBASE_API_KEY
                val request = FirebaseAuthSignUpRequest(email = cleanEmail, password = cleanPass)
                val response = firebaseApi.signUpWithPassword(apiKey, request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    val updates = buildJsonObject {
                        put("name", JsonPrimitive(cleanName))
                        put("email", JsonPrimitive(cleanEmail))
                        put("phone", JsonPrimitive(cleanPhone))
                        put("businessName", JsonPrimitive(cleanBiz.ifBlank { "My Shop" }))
                        put("role", JsonPrimitive("user"))
                        put("createdAt", JsonPrimitive(System.currentTimeMillis()))
                    }
                    try {
                        firebaseApi.patchUserProfile(authData.localId, updates, authData.idToken)
                    } catch (_: Exception) {
                    }

                    val session = UserSession(
                        userId = authData.localId,
                        idToken = authData.idToken,
                        refreshToken = authData.refreshToken,
                        email = authData.email,
                        displayName = cleanName,
                        businessName = cleanBiz.ifBlank { "My Shop" },
                        phone = cleanPhone
                    )
                    sessionManager.saveSession(session)
                    _uiState.value = Resource.Success(Unit)
                    _eventFlow.emit(AuthEvent.LoginSuccess)
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseFirebaseError(rawError)
                    _uiState.value = Resource.Error(errorMsg)
                    _eventFlow.emit(AuthEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is UnknownHostException -> "No internet connection. Please check your network."
                    is SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> e.localizedMessage ?: "Failed to register"
                }
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            viewModelScope.launch {
                val msg = "Please enter a valid email address"
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val apiKey = Constants.FIREBASE_API_KEY
                val request = FirebasePasswordResetRequest(email = cleanEmail)
                val response = firebaseApi.sendPasswordReset(apiKey, request)
                if (response.isSuccessful) {
                    _uiState.value = Resource.Success(Unit)
                    _eventFlow.emit(AuthEvent.PasswordResetSent(cleanEmail))
                } else {
                    val rawError = response.errorBody()?.string()
                    val err = parseFirebaseError(rawError)
                    _uiState.value = Resource.Error(err)
                    _eventFlow.emit(AuthEvent.Error(err))
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is UnknownHostException -> "No internet connection. Please check your network."
                    is SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> e.localizedMessage ?: "Error sending reset email"
                }
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun signInWithGoogleIdToken(
        googleIdToken: String,
        fallbackDisplayName: String? = null,
        fallbackEmail: String? = null,
        fallbackPhotoUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val apiKey = Constants.FIREBASE_API_KEY
                val postBody = "id_token=$googleIdToken&providerId=google.com"
                val request = FirebaseSignInWithIdpRequest(postBody = postBody)
                val response = firebaseApi.signInWithIdp(apiKey, request)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    val userId = authData.localId
                    val resolvedEmail = authData.email.ifBlank { fallbackEmail ?: "" }
                    val resolvedName = authData.displayName.ifBlank { fallbackDisplayName ?: resolvedEmail.substringBefore("@") }
                    val resolvedPhoto = authData.photoUrl.ifBlank { fallbackPhotoUrl ?: "" }

                    val existingProfile = try {
                        val profileRes = firebaseApi.getUserProfile(userId, authData.idToken)
                        if (profileRes.isSuccessful) profileRes.body() else null
                    } catch (_: Exception) {
                        null
                    }

                    if (existingProfile == null || authData.isNewUser) {
                        val updates = buildJsonObject {
                            put("name", JsonPrimitive(resolvedName))
                            put("email", JsonPrimitive(resolvedEmail))
                            put("photoURL", JsonPrimitive(resolvedPhoto))
                            put("businessName", JsonPrimitive("My Shop"))
                            put("role", JsonPrimitive("user"))
                            put("createdAt", JsonPrimitive(System.currentTimeMillis()))
                        }
                        try {
                            firebaseApi.patchUserProfile(userId, updates, authData.idToken)
                        } catch (_: Exception) {
                            // Non-fatal
                        }
                    }

                    val session = UserSession(
                        userId = userId,
                        idToken = authData.idToken,
                        refreshToken = authData.refreshToken,
                        email = resolvedEmail,
                        displayName = existingProfile?.name?.ifBlank { null } ?: resolvedName,
                        businessName = existingProfile?.businessName?.ifBlank { null } ?: "My Shop",
                        phone = existingProfile?.phone ?: "",
                        upiId = existingProfile?.upiId ?: "",
                        role = existingProfile?.role ?: "user",
                        photoURL = existingProfile?.photoURL?.ifBlank { null } ?: resolvedPhoto
                    )
                    sessionManager.saveSession(session)
                    _uiState.value = Resource.Success(Unit)
                    _eventFlow.emit(AuthEvent.LoginSuccess)

                    viewModelScope.launch {
                        try {
                            syncEngine.synchronize()
                        } catch (_: Exception) {
                            // Non-fatal background sync
                        }
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseFirebaseError(rawError)
                    _uiState.value = Resource.Error(errorMsg)
                    _eventFlow.emit(AuthEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is UnknownHostException -> "No internet connection. Please check your network."
                    is SocketTimeoutException -> "Connection timed out. Please try again."
                    else -> e.localizedMessage ?: "Failed to sign in with Google"
                }
                _uiState.value = Resource.Error(msg)
                _eventFlow.emit(AuthEvent.Error(msg))
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        viewModelScope.launch {
            _uiState.value = Resource.Error(message)
            _eventFlow.emit(AuthEvent.Error(message))
        }
    }

    private fun parseFirebaseError(rawJson: String?): String {
        if (rawJson.isNullOrBlank()) return "Authentication failed. Please check your credentials."
        return try {
            val root = json.parseToJsonElement(rawJson).jsonObject
            val errorObj = root["error"]?.jsonObject
            val rawMessage = errorObj?.get("message")?.jsonPrimitive?.content ?: ""
            when {
                rawMessage.contains("INVALID_LOGIN_CREDENTIALS") -> "Invalid email or password"
                rawMessage.contains("EMAIL_NOT_FOUND") -> "No account found with this email"
                rawMessage.contains("INVALID_PASSWORD") -> "Incorrect password. Please try again"
                rawMessage.contains("USER_DISABLED") -> "This account has been disabled"
                rawMessage.contains("TOO_MANY_ATTEMPTS_TRY_LATER") -> "Too many failed attempts. Please try again later"
                rawMessage.contains("EMAIL_EXISTS") -> "This email is already registered"
                rawMessage.contains("WEAK_PASSWORD") -> "Password must be at least 6 characters"
                rawMessage.contains("INVALID_EMAIL") -> "Please enter a valid email address"
                rawMessage.contains("API key not valid") -> "Configuration error: Invalid API key"
                rawMessage.isNotBlank() -> rawMessage.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                else -> "Authentication failed. Please check your credentials."
            }
        } catch (_: Exception) {
            if (!rawJson.contains("{")) rawJson else "Authentication failed. Please check your credentials."
        }
    }
}
