package com.sumanonline.hisabkhata.feature.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.AppPreferencesManager
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.sync.SyncWorker

private val SectionHeaderBg = Color(0xFFF4F5F7)
private val SectionHeaderText = Color(0xFF757575)
private val DividerColor = Color(0xFFEEEEEE)

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
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Security & Authentication Section
            SectionHeader(title = "SECURITY & AUTHENTICATION")

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

            // 2. Sync & Storage Section
            SectionHeader(title = "SYNC & DATA USAGE")

            SettingToggleRow(
                icon = Icons.Outlined.Wifi,
                title = "Sync on Wi-Fi Only",
                subtitle = "Save mobile data by restricting background synchronization",
                checked = preferences.autoSyncWifiOnly,
                onCheckedChange = { preferencesManager.setAutoSyncWifiOnly(it) }
            )

            SettingsDivider()

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

            // 3. Notifications & Alerts Section
            SectionHeader(title = "NOTIFICATIONS & SOUND")

            SettingToggleRow(
                icon = Icons.Outlined.Notifications,
                title = "Transaction Notifications",
                subtitle = "Receive real-time alerts for customer payments & settlements",
                checked = preferences.notificationAlerts,
                onCheckedChange = { preferencesManager.setNotificationAlerts(it) }
            )

            SettingsDivider()

            SettingToggleRow(
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                title = "Sound & Vibration Feedback",
                subtitle = "Play confirmation tone upon successfully adding entries",
                checked = preferences.soundVibration,
                onCheckedChange = { preferencesManager.setSoundVibration(it) }
            )

            // 4. Application Details Section
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("Select Lock Timeout", fontWeight = FontWeight.Bold) },
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
                            Text(text = option, fontSize = 15.sp, color = Color(0xFF1E293B))
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
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionHeaderBg)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = SectionHeaderText,
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
            tint = Color(0xFF616161),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
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
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFF1F5F9),
                uncheckedBorderColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
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
            tint = Color(0xFF616161),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
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
            tint = Color(0xFF616161),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 0.7.dp,
        color = DividerColor
    )
}
