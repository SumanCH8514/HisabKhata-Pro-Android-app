package com.sumanonline.hisabkhata.feature.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar

private enum class EditableField(
    val title: String,
    val fieldKey: String,
    val keyboardType: KeyboardType = KeyboardType.Text
) {
    NAME("Name", "displayName"),
    PHONE("Registered Number", "phone", KeyboardType.Phone),
    BUSINESS_NAME("Business Name", "businessName"),
    BUSINESS_ADDRESS("Business Address", "businessAddress"),
    BUSINESS_CATEGORY("Business Category", "businessCategory"),
    BUSINESS_TYPE("Business Type", "businessType"),
    GSTIN("GSTIN", "gstin"),
    BANK_ACCOUNT("Bank Account", "bankAccount"),
    UPI_ID("UPI ID", "upiId"),
    STAFF_DETAILS("Staff Details", "staffCount", KeyboardType.Number),
    PHOTO_URL("Profile Photo", "photoURL")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountSettingsScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var activeEditField by remember { mutableStateOf<EditableField?>(null) }
    var fieldValueInput by remember { mutableStateOf("") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(BrandBlue, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HisabKhata",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDC2626))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "a SumanOnline Project",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            }
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.MORE,
                onTabSelected = onTabSelected
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Avatar & Edit Photo Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val sanitizedPhoto = remember(session.photoURL) {
                        ImageUtils.sanitizeImageSource(session.photoURL)
                    }

                    if (sanitizedPhoto != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(sanitizedPhoto)
                                .crossfade(true)
                                .build(),
                            contentDescription = session.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        CustomerAvatar(
                            name = session.businessName.ifBlank { session.displayName.ifBlank { "Merchant" } },
                            photoUrl = session.photoURL,
                            size = 90.dp,
                            backgroundColor = BrandBlue,
                            textColor = Color.White,
                            fontSize = 32.sp
                        )
                    }

                    // Floating camera edit badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(BrandBlue)
                            .clickable {
                                fieldValueInput = session.photoURL
                                activeEditField = EditableField.PHOTO_URL
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = "Edit Photo",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "EDIT PHOTO",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier
                        .clickable {
                            fieldValueInput = session.photoURL
                            activeEditField = EditableField.PHOTO_URL
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Strength
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PROFILE STRENGTH : ",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "STRONG",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                    Text(
                        text = "100%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF16A34A))
                )
            }

            // Section 1: PERSONAL INFO
            SectionHeader(title = "PERSONAL INFO")

            SettingsRowItem(
                icon = Icons.Outlined.Person,
                label = "NAME",
                value = session.displayName.ifBlank { "Suman Chakrabortty" },
                onClick = {
                    fieldValueInput = session.displayName
                    activeEditField = EditableField.NAME
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.Phone,
                label = "REGISTERED NUMBER",
                value = session.phone.ifBlank { "8918153949" },
                onClick = {
                    fieldValueInput = session.phone
                    activeEditField = EditableField.PHONE
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.Storefront,
                label = "BUSINESS NAME",
                value = session.businessName.ifBlank { "SumanOnlineService" },
                onClick = {
                    fieldValueInput = session.businessName
                    activeEditField = EditableField.BUSINESS_NAME
                }
            )

            // Section 2: BUSINESS INFO
            SectionHeader(title = "BUSINESS INFO")

            SettingsRowItem(
                icon = Icons.Outlined.LocationOn,
                label = "BUSINESS ADDRESS",
                value = session.businessAddress.ifBlank { "Karakberia, Kotulpur, WB, IND, 722154" },
                onClick = {
                    fieldValueInput = session.businessAddress.ifBlank { "Karakberia, Kotulpur, WB, IND, 722154" }
                    activeEditField = EditableField.BUSINESS_ADDRESS
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.LocalOffer,
                label = "BUSINESS CATEGORY",
                value = session.businessCategory.ifBlank { "Lending" },
                onClick = {
                    fieldValueInput = session.businessCategory.ifBlank { "Lending" }
                    activeEditField = EditableField.BUSINESS_CATEGORY
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.Apartment,
                label = "BUSINESS TYPE",
                value = session.businessType.ifBlank { "Personal" },
                onClick = {
                    fieldValueInput = session.businessType.ifBlank { "Personal" }
                    activeEditField = EditableField.BUSINESS_TYPE
                }
            )

            // Section 3: FINANCIAL INFO
            SectionHeader(title = "FINANCIAL INFO")

            SettingsRowItem(
                icon = Icons.Outlined.Description,
                label = "GSTIN",
                value = session.gstin.ifBlank { "NA" },
                onClick = {
                    fieldValueInput = session.gstin
                    activeEditField = EditableField.GSTIN
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.AccountBalance,
                label = "BANK ACCOUNT",
                value = session.bankAccount.ifBlank { "NA" },
                onClick = {
                    fieldValueInput = session.bankAccount
                    activeEditField = EditableField.BANK_ACCOUNT
                }
            )

            ItemDivider()

            SettingsRowItem(
                icon = Icons.Outlined.Smartphone,
                label = "UPI ID",
                value = session.upiId.ifBlank { "8514900224@upi" },
                onClick = {
                    fieldValueInput = session.upiId
                    activeEditField = EditableField.UPI_ID
                }
            )

            // Section 4: STAFF INFO
            SectionHeader(title = "STAFF INFO")

            SettingsRowItem(
                icon = Icons.Outlined.Group,
                label = "DETAILS",
                value = session.staffCount.ifBlank { "0" },
                onClick = {
                    fieldValueInput = session.staffCount.ifBlank { "0" }
                    activeEditField = EditableField.STAFF_DETAILS
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GiveRed),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = GiveRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal Bottom Sheet for Editing Fields
    if (activeEditField != null) {
        val field = activeEditField!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { activeEditField = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 4.dp)
                        .size(width = 44.dp, height = 4.dp)
                        .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edit ${field.title}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(
                        onClick = { activeEditField = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Quick chips for Business Category
                if (field == EditableField.BUSINESS_CATEGORY) {
                    val categories = listOf("Lending", "Retail / Kirana", "Wholesale", "Services", "Manufacturing", "Other")
                    Text(
                        text = "Select Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = fieldValueInput.equals(cat, ignoreCase = true),
                                onClick = { fieldValueInput = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Quick chips for Business Type
                if (field == EditableField.BUSINESS_TYPE) {
                    val types = listOf("Personal", "Proprietorship", "Partnership", "Private Limited", "LLP")
                    Text(
                        text = "Select Business Type",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                selected = fieldValueInput.equals(type, ignoreCase = true),
                                onClick = { fieldValueInput = type },
                                label = { Text(type, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = fieldValueInput,
                    onValueChange = { fieldValueInput = it },
                    label = { Text(field.title) },
                    singleLine = field != EditableField.BUSINESS_ADDRESS,
                    maxLines = if (field == EditableField.BUSINESS_ADDRESS) 3 else 1,
                    keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        focusedLabelColor = BrandBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.updateSingleField(field.fieldKey, fieldValueInput)
                        activeEditField = null
                        Toast.makeText(context, "${field.title} updated successfully", Toast.LENGTH_SHORT).show()
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
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

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        color = Color(0xFFF1F5F9),
        thickness = 1.dp,
        modifier = Modifier.padding(start = 72.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(20.dp)
        )
    }
}
