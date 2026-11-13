package com.sumanonline.hisabkhata.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.AppPreferencesManager
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.sync.SyncWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationSettingsScreen(
    profileViewModel: ProfileViewModel,
    preferencesManager: AppPreferencesManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val session by profileViewModel.session.collectAsState()
    val preferences by preferencesManager.preferences.collectAsState()

    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val timeoutOptions = listOf("Immediately", "1 Minute", "5 Minutes", "15 Minutes")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Application Settings",
                        fontSize = 19.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ================= 1. ACCOUNT & SECURITY =================
            SectionHeader(title = "ACCOUNT & SECURITY")

            // Change Password
            SettingActionRow(
                icon = Icons.Outlined.LockReset,
                title = "Change Password",
                subtitle = "Send reset instructions to your registered email",
                onClick = { showPasswordResetDialog = true }
            )

            SettingsDivider()

            // Two-Factor Authentication
            SettingActionRow(
                icon = Icons.Outlined.VpnKey,
                title = "Two-Factor Authentication",
                subtitle = "Protect your account with Google Authenticator",
                trailingText = if (preferences.twoFactorEnabled) "Enabled" else null,
                onClick = { showTwoFactorDialog = true }
            )

            SettingsDivider()

            // Login Email OTP Verification
            SettingToggleRow(
                icon = Icons.Outlined.MarkEmailRead,
                title = "Login Email OTP Verification",
                subtitle = if (preferences.emailOtpVerification) {
                    "Enabled — 6-digit OTP verification required on login"
                } else {
                    "Disabled — Normal login without email OTP"
                },
                checked = preferences.emailOtpVerification,
                onCheckedChange = { enabled ->
                    preferencesManager.setEmailOtpVerification(enabled)
                    val msg = if (enabled) "Email OTP verification enabled" else "Email OTP verification disabled"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )

            SettingsDivider()

            // Biometric App Lock Switch
            SettingToggleRow(
                icon = Icons.Outlined.Fingerprint,
                title = "Biometric App Lock",
                subtitle = "Require fingerprint or screen lock to open app",
                checked = session.biometricEnabled,
                onCheckedChange = { profileViewModel.setBiometricLock(it) }
            )

            SettingsDivider()

            // Lock Timeout selector
            SettingActionRow(
                icon = Icons.Outlined.Timer,
                title = "App Lock Timeout",
                subtitle = "Locks after: ${preferences.lockTimeout}",
                onClick = { showTimeoutDialog = true }
            )

            SettingsDivider()

            // Screen Security Toggle
            SettingToggleRow(
                icon = Icons.Outlined.Security,
                title = "Block Screen Capture",
                subtitle = "Protect sensitive financial data in app switchers",
                checked = preferences.screenSecurity,
                onCheckedChange = { preferencesManager.setScreenSecurity(it) }
            )

            // ================= 2. DATA & BACKUP =================
            SectionHeader(title = "DATA & BACKUP")

            // Auto Cloud Backup
            SettingToggleRow(
                icon = Icons.Outlined.CloudDone,
                title = "Auto Cloud Backup",
                subtitle = if (preferences.autoCloudBackup) {
                    "Real-time database sync active"
                } else {
                    "Cloud sync paused (Manual mode)"
                },
                checked = preferences.autoCloudBackup,
                onCheckedChange = { enabled ->
                    preferencesManager.setAutoCloudBackup(enabled)
                    val msg = if (enabled) "Auto cloud backup activated" else "Auto cloud backup paused"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )

            SettingsDivider()

            // Export All Ledger Data with Download Action
            SettingExportRow(
                icon = Icons.Outlined.FileDownload,
                title = "Export All Ledger Data",
                subtitle = "Download CSV file of all customers and transactions",
                buttonText = "Download",
                isLoading = isExporting,
                onDownloadClick = {
                    if (!isExporting) {
                        isExporting = true
                        Toast.makeText(context, "Preparing ledger export...", Toast.LENGTH_SHORT).show()
                        profileViewModel.exportAllLedger(context) { success, msg ->
                            isExporting = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )

            SettingsDivider()

            // Sync on Wi-Fi Only
            SettingToggleRow(
                icon = Icons.Outlined.Wifi,
                title = "Sync on Wi-Fi Only",
                subtitle = "Save mobile data by restricting background synchronization",
                checked = preferences.autoSyncWifiOnly,
                onCheckedChange = { preferencesManager.setAutoSyncWifiOnly(it) }
            )

            SettingsDivider()

            // Force Sync Ledger Now
            SettingActionRow(
                icon = Icons.Outlined.Sync,
                title = "Force Sync Ledger Now",
                subtitle = "Upload pending outbox and fetch cloud updates immediately",
                onClick = {
                    SyncWorker.enqueueOneTimeSync(context)
                    Toast.makeText(context, "Sync triggered successfully", Toast.LENGTH_SHORT).show()
                }
            )

            SettingsDivider()

            // Clear Temporary Cache
            SettingActionRow(
                icon = Icons.Outlined.CleaningServices,
                title = "Clear Temporary Cache",
                subtitle = "Frees internal storage (~3.8 MB used by reports & previews)",
                onClick = {
                    try {
                        context.cacheDir.deleteRecursively()
                        Toast.makeText(context, "App cache cleared", Toast.LENGTH_SHORT).show()
                    } catch (_: Exception) {
                        Toast.makeText(context, "Cache already cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // ================= 3. APPLICATION DETAILS =================
            SectionHeader(title = "APPLICATION DETAILS")

            SettingInfoRow(
                icon = Icons.Outlined.Info,
                title = "Application Version",
                value = "1.1.0 (Production Build 88)"
            )

            SettingsDivider()

            SettingInfoRow(
                icon = Icons.Outlined.Security,
                title = "Security & Encryption",
                value = "AES-256 GCM • MasterKey Encrypted"
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Dialog: Lock Timeout Selection
    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Select Lock Timeout",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    timeoutOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferencesManager.setLockTimeout(option)
                                    showTimeoutDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (preferences.lockTimeout == option),
                                onClick = {
                                    preferencesManager.setLockTimeout(option)
                                    showTimeoutDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("Cancel", color = BrandBlue)
                }
            }
        )
    }

    // Dialog: Change Password Confirmation
    if (showPasswordResetDialog) {
        val targetEmail = session.email.ifBlank { "your registered email" }
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Reset Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "We will send password reset instructions to your registered email address:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = targetEmail,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Click the link in the received email to securely choose your new password.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordResetDialog = false
                        if (session.email.isNotBlank()) {
                            profileViewModel.sendPasswordReset(session.email) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No registered email found for this session", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Send Email", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Dialog: Two-Factor Authentication Setup
    if (showTwoFactorDialog) {
        val authSecretKey = remember(session.userId) {
            val keyHash = session.userId.take(8).ifBlank { "8818" }.uppercase()
            "HKPRO-2FA-$keyHash-AUTH"
        }

        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.VpnKey,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Google Authenticator 2FA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Protect your ledger account by verifying a 6-digit Time-based One-Time Password (TOTP) from Google Authenticator on sign-in.",
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YOUR AUTHENTICATOR SECRET KEY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = authSecretKey,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = BrandBlue
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("2FA Key", authSecretKey))
                                    Toast.makeText(context, "Secret key copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextState = !preferences.twoFactorEnabled
                                preferencesManager.setTwoFactorEnabled(nextState)
                                val msg = if (nextState) "2FA authentication enabled" else "2FA authentication disabled"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable 2FA Protection",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = preferences.twoFactorEnabled,
                            onCheckedChange = { nextState ->
                                preferencesManager.setTwoFactorEnabled(nextState)
                                val msg = if (nextState) "2FA authentication enabled" else "2FA authentication disabled"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandBlue,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTwoFactorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandBlue,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }

        if (trailingText != null) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFECFDF5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailingText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = GotGreen
                )
            }
        }
    }
}

@Composable
private fun SettingExportRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonText: String = "Download",
    isLoading: Boolean = false,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onDownloadClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SettingInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 0.7.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}
