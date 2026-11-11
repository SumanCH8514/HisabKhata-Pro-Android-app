package com.sumanonline.hisabkhata.feature.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.AppTopBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onTabSelected: (BottomTab) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var name by remember(session) { mutableStateOf(session.displayName) }
    var businessName by remember(session) { mutableStateOf(session.businessName) }
    var phone by remember(session) { mutableStateOf(session.phone) }
    var upiId by remember(session) { mutableStateOf(session.upiId) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Account & Settings",
                subtitle = session.email
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.MORE,
                onTabSelected = onTabSelected
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomerAvatar(
                        name = session.displayName.ifBlank { "Merchant" },
                        photoUrl = session.photoURL,
                        size = 56.dp,
                        backgroundColor = BrandBlue,
                        textColor = SurfaceWhite
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = session.displayName.ifBlank { "HisabKhata Merchant" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = session.businessName.ifBlank { "My Business" },
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = session.email,
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BUSINESS INFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Merchant / Contact Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Shop / Business Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID for Receiving Payments (e.g. mobile@upi)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.updateProfile(name, businessName, phone, upiId) },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SECURITY & PRIVACY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "Biometric Lock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Require fingerprint to open app", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = session.biometricEnabled,
                        onCheckedChange = { viewModel.setBiometricLock(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = BrandBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "HISABKHATA ECOSYSTEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pos.hisabkhata.sumanonline.com/"))
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = GotGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "POS Billing & Barcode App", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Instant counter billing & thermal receipts", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GiveRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = GiveRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = "Sign Out?",
            message = "Are you sure you want to sign out of your HisabKhata account?",
            confirmText = "Sign Out",
            isDestructive = true,
            onConfirm = {
                showLogoutConfirm = false
                viewModel.logout()
                onLogoutSuccess()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}
