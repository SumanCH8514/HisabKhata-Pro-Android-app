package com.sumanonline.hisabkhata

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.sumanonline.hisabkhata.core.common.AppPreferencesManager
import com.sumanonline.hisabkhata.core.designsystem.HisabKhataTheme
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.sync.SyncWorker
import com.sumanonline.hisabkhata.feature.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SecureSessionManager

    @Inject
    lateinit var preferencesManager: AppPreferencesManager

    private val deepLinkUriState = MutableStateFlow<Uri?>(null)

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var isBiometricAuthenticated by mutableStateOf(true)
    private var lastBackgroundTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
        enableEdgeToEdge()

        val initialPrefs = preferencesManager.preferences.value
        SyncWorker.schedulePeriodicSync(this, initialPrefs.autoSyncWifiOnly)
        SyncWorker.enqueueOneTimeSync(this)

        // Observe screen security preference to block screenshots & app switcher previews
        lifecycleScope.launch {
            preferencesManager.preferences.collect { prefs ->
                if (prefs.screenSecurity) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        val session = sessionManager.sessionState.value
        val needsBiometric = session.biometricEnabled && sessionManager.isLoggedIn()
        isBiometricAuthenticated = !needsBiometric

        renderContent()

        if (needsBiometric) {
            promptBiometricAuthentication()
        }
    }

    override fun onStop() {
        super.onStop()
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        if (lastBackgroundTimestamp > 0L) {
            val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
            val timeoutMs = when (preferencesManager.preferences.value.lockTimeout) {
                "Immediately" -> 0L
                "1 Minute" -> 60_000L
                "5 Minutes" -> 300_000L
                "15 Minutes" -> 900_000L
                else -> 0L
            }
            val session = sessionManager.sessionState.value
            if (elapsed >= timeoutMs && session.biometricEnabled && sessionManager.isLoggedIn()) {
                isBiometricAuthenticated = false
                promptBiometricAuthentication()
            }
        }
    }

    private fun promptBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isBiometricAuthenticated = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        finish()
                    }
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("HisabKhata App Lock")
            .setSubtitle("Authenticate using your fingerprint or screen lock")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        deepLinkUriState.value = uri
    }

    private fun renderContent() {
        setContent {
            val preferences by preferencesManager.preferences.collectAsState()
            HisabKhataTheme(
                themeMode = preferences.themeMode,
                accentColor = preferences.accentColor,
                fontSize = preferences.fontSize,
                fontFamily = preferences.fontFamily
            ) {
                AppNavigation(
                    isBiometricAuthenticated = isBiometricAuthenticated,
                    deepLinkFlow = deepLinkUriState
                )
            }
        }
    }
}
