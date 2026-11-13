package com.sumanonline.hisabkhata.feature.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.MaterialTheme
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedStroke
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.RedPillBg
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.appSwitchColors
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.components.TwoFactorQrDialog
import com.sumanonline.hisabkhata.core.common.ImageUtils
import kotlinx.coroutines.launch

private data class EditFieldSpec(
    val key: String,
    val title: String,
    val initialValue: String,
    val isMultiline: Boolean = false,
    val suggestions: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountProfileSettingsScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val preferences by viewModel.preferencesManager.preferences.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val scope = rememberCoroutineScope()

    var activeEditField by remember { mutableStateOf<EditFieldSpec?>(null) }
    var editFieldValue by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showPhotoUrlDialog by remember { mutableStateOf(false) }
    var photoUrlInput by remember { mutableStateOf("") }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriString = uri.toString()
            viewModel.updateSingleField("photoURL", uriString)
            Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
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
                        text = "Account Profile Settings",
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
            // Top Avatar & Profile Strength Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 22.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Avatar with Camera Badge
                Box(
                    modifier = Modifier.size(108.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (session.photoURL.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ImageUtils.sanitizeImageSource(session.photoURL))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        )
                    } else {
                        CustomerAvatar(
                            name = session.businessName.ifBlank { session.displayName.ifBlank { "User" } },
                            photoUrl = null,
                            size = 108.dp,
                            fontSize = 36.sp,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = Color.White
                        )
                    }

                    // Camera Icon Badge Overlay
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { showPhotoOptionsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "Edit photo" link button
                Text(
                    text = "Edit photo",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showPhotoOptionsDialog = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Green Progress Bar (right below Edit photo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GotGreen)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Profile Strength Row (Below Progress Bar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Profile strength : ",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Good",
                            fontSize = 13.sp,
                            color = GotGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "100%",
                        fontSize = 13.sp,
                        color = GotGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Personal Info Section
            SectionHeader(title = "Personal Info")

            SettingsRow(
                icon = Icons.Outlined.Person,
                label = "Name",
                value = session.displayName.ifBlank { "Suman Chakrabortty" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "displayName",
                        title = "Name",
                        initialValue = session.displayName.ifBlank { "Suman Chakrabortty" }
                    )
                    editFieldValue = session.displayName.ifBlank { "Suman Chakrabortty" }
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Phone,
                label = "Registered number",
                value = session.phone.ifBlank { "8918153949" },
                isClickable = false
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Storefront,
                label = "Business name",
                value = session.businessName.ifBlank { "SumanOnline Service" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "businessName",
                        title = "Business name",
                        initialValue = session.businessName.ifBlank { "SumanOnline Service" }
                    )
                    editFieldValue = session.businessName.ifBlank { "SumanOnline Service" }
                }
            )

            // 2. Business info Section
            SectionHeader(title = "Business info")

            SettingsRow(
                icon = Icons.Outlined.LocationOn,
                label = "Business address",
                value = session.businessAddress.ifBlank { "Karakberia, Karak Berya, Kotalpur, WB, IND, 722141" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "businessAddress",
                        title = "Business address",
                        initialValue = session.businessAddress.ifBlank { "Karakberia, Karak Berya, Kotalpur, WB, IND, 722141" },
                        isMultiline = true
                    )
                    editFieldValue = session.businessAddress.ifBlank { "Karakberia, Karak Berya, Kotalpur, WB, IND, 722141" }
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.ContentCopy,
                label = "Business Category",
                value = session.businessCategory.ifBlank { "Mobile" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "businessCategory",
                        title = "Business Category",
                        initialValue = session.businessCategory.ifBlank { "Mobile" },
                        suggestions = listOf(
                            "Mobile & Electronics",
                            "Lending / Finance",
                            "Retail / Grocery",
                            "Wholesale",
                            "Services",
                            "Restaurant / Cafe",
                            "Pharmacy",
                            "Garments / Fashion",
                            "Other"
                        )
                    )
                    editFieldValue = session.businessCategory.ifBlank { "Mobile" }
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Business,
                label = "Business Type",
                value = session.businessType.ifBlank { "Personal" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "businessType",
                        title = "Business Type",
                        initialValue = session.businessType.ifBlank { "Personal" },
                        suggestions = listOf(
                            "Personal",
                            "Proprietorship",
                            "Partnership",
                            "Private Limited",
                            "LLP"
                        )
                    )
                    editFieldValue = session.businessType.ifBlank { "Personal" }
                }
            )

            // 3. Financial info Section
            SectionHeader(title = "Financial info")

            SettingsRow(
                icon = Icons.Outlined.Description,
                label = "GSTIN",
                value = session.gstin.ifBlank { "NA" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "gstin",
                        title = "GSTIN",
                        initialValue = session.gstin.ifBlank { "NA" }
                    )
                    editFieldValue = if (session.gstin == "NA") "" else session.gstin
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.AccountBalance,
                label = "Bank Account",
                value = session.bankAccount.ifBlank { "NA" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "bankAccount",
                        title = "Bank Account",
                        initialValue = session.bankAccount.ifBlank { "NA" }
                    )
                    editFieldValue = if (session.bankAccount == "NA") "" else session.bankAccount
                }
            )

            SettingsDivider()

            SettingsRow(
                icon = Icons.Outlined.Smartphone,
                label = "UPI ID",
                value = session.upiId.ifBlank { "8514900224@upi" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "upiId",
                        title = "UPI ID",
                        initialValue = session.upiId.ifBlank { "8514900224@upi" }
                    )
                    editFieldValue = session.upiId.ifBlank { "8514900224@upi" }
                }
            )

            // 4. Staff info Section
            SectionHeader(title = "Staff info")

            SettingsRow(
                icon = Icons.Outlined.People,
                label = "Details",
                value = session.staffCount.ifBlank { "0" },
                isClickable = true,
                onClick = {
                    activeEditField = EditFieldSpec(
                        key = "staffCount",
                        title = "Staff Details",
                        initialValue = session.staffCount.ifBlank { "0" }
                    )
                    editFieldValue = session.staffCount.ifBlank { "0" }
                }
            )

            // 5. Account & Security Section
            SectionHeader(title = "Account & Security")

            SettingsRow(
                icon = Icons.Outlined.LockReset,
                label = "Change Password",
                value = "Send reset instructions",
                isClickable = true,
                onClick = { showPasswordResetDialog = true }
            )

            SettingsRow(
                icon = Icons.Outlined.VpnKey,
                label = "Two-Factor Auth",
                value = if (preferences.twoFactorEnabled) "Enabled" else "Protect with Authenticator",
                isClickable = true,
                onClick = { showTwoFactorDialog = true }
            )

            SettingsRow(
                icon = Icons.Outlined.MarkEmailRead,
                label = "Login Email OTP",
                value = if (preferences.emailOtpVerification) {
                    "Enabled (OTP required)"
                } else {
                    "Disabled (Normal login)"
                },
                isClickable = true,
                onClick = {
                    val next = !preferences.emailOtpVerification
                    viewModel.setEmailOtpVerification(next)
                    val msg = if (next) "Email OTP verification enabled" else "Email OTP verification disabled"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Out Button (Matching image style)
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = RedPillBg,
                    contentColor = GiveRed
                ),
                border = BorderStroke(1.dp, GiveRedStroke),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = GiveRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GiveRed
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Modal Bottom Sheet for editing individual field
    if (activeEditField != null) {
        val spec = activeEditField!!
        ModalBottomSheet(
            onDismissRequest = { activeEditField = null },
            sheetState = sheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Edit ${spec.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = editFieldValue,
                    onValueChange = { editFieldValue = it },
                    label = { Text(spec.title) },
                    singleLine = !spec.isMultiline,
                    minLines = if (spec.isMultiline) 3 else 1,
                    maxLines = if (spec.isMultiline) 4 else 1,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick suggestions if present
                if (spec.suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Suggestions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        spec.suggestions.forEach { suggestion ->
                            val isSelected = editFieldValue.equals(suggestion, ignoreCase = true)
                            SuggestionChip(
                                onClick = { editFieldValue = suggestion },
                                label = {
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else BorderLight
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { activeEditField = null },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val keyToSave = spec.key
                            val valueToSave = editFieldValue.trim()
                            viewModel.updateSingleField(keyToSave, valueToSave)
                            scope.launch {
                                sheetState.hide()
                                activeEditField = null
                            }
                            Toast.makeText(context, "${spec.title} updated", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp).navigationBarsPadding())
            }
        }
    }

    // Photo options dialog (Gallery, URL, Remove)
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Profile Photo", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Choose an option to update your profile photo:",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhotoOptionsDialog = false
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Select from Gallery", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPhotoOptionsDialog = false
                        photoUrlInput = session.photoURL
                        showPhotoUrlDialog = true
                    }
                ) {
                    Text("Enter Image URL", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Photo URL direct entry dialog
    if (showPhotoUrlDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoUrlDialog = false },
            title = { Text("Enter Photo URL", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = photoUrlInput,
                        onValueChange = { photoUrlInput = it },
                        label = { Text("Image URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSingleField("photoURL", photoUrlInput.trim())
                        showPhotoUrlDialog = false
                        Toast.makeText(context, "Profile photo updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoUrlDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Logout confirmation dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Are you sure you want to sign out of HisabKhata?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                        onLogoutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GiveRed)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Password Reset Dialog
    if (showPasswordResetDialog) {
        val targetEmail = session.email.ifBlank { "your registered email" }
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "We will send password reset instructions to your registered email address:",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = targetEmail,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Click the link in the received email to securely choose your new password.",
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordResetDialog = false
                        if (session.email.isNotBlank()) {
                            viewModel.sendPasswordReset(session.email) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No registered email found for this session", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Send Email", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Two-Factor Authentication Dialog with QR Code
    if (showTwoFactorDialog) {
        TwoFactorQrDialog(
            userEmail = session.email,
            userId = session.userId,
            isEnabled = preferences.twoFactorEnabled,
            onToggleEnabled = { enabled ->
                viewModel.setTwoFactorEnabled(enabled)
                val msg = if (enabled) "2FA authentication enabled" else "2FA authentication disabled"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showTwoFactorDialog = false }
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bare line icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Label and Value
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Blue chevron only for clickable items
        if (isClickable) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
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
