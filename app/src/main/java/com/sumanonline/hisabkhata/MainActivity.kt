package com.sumanonline.hisabkhata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.sumanonline.hisabkhata.core.designsystem.HisabKhataTheme
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import com.sumanonline.hisabkhata.core.sync.SyncWorker
import com.sumanonline.hisabkhata.feature.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SecureSessionManager

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var isBiometricAuthenticated by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SyncWorker.schedulePeriodicSync(this)
        SyncWorker.enqueueOneTimeSync(this)

        val session = sessionManager.sessionState.value
        val needsBiometric = session.biometricEnabled && sessionManager.isLoggedIn()
        isBiometricAuthenticated = !needsBiometric

        renderContent()

        if (needsBiometric) {
            promptBiometricAuthentication()
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
                    finish()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("HisabKhata App Lock")
            .setSubtitle("Authenticate using your fingerprint or screen lock")
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun renderContent() {
        setContent {
            HisabKhataTheme {
                AppNavigation(
                    isBiometricAuthenticated = isBiometricAuthenticated
                )
            }
        }
    }
}
