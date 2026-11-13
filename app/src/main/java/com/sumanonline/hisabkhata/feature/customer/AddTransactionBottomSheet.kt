package com.sumanonline.hisabkhata.feature.customer

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTransactionBottomSheet(
    customerName: String,
    isGave: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, date: String, attachment: String?) -> Unit
) {
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentBase64 by remember { mutableStateOf<String?>(null) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val displayDate = remember(selectedCalendar) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedCalendar.time)
    }
    val dateIso = remember(selectedCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val width = bitmap.width
                val height = bitmap.height
                val targetSize = 1024
                val scaled = if (width > targetSize || height > targetSize) {
                    val scale = targetSize.toFloat() / maxOf(width, height)
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    bitmap
                }
                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                attachmentBase64 = "data:image/jpeg;base64,$base64"
                Toast.makeText(context, "Bill attached", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Color theme mappings matching reference screenshots
    val pageBgColor = if (isGave) Color(0xFFFAF5F6) else Color(0xFFF6FBF8)
    val headerTint = if (isGave) Color(0xFF5A1A1A) else Color(0xFF0F5132)
    val avatarBg = if (isGave) Color(0xFFDEC5C8) else Color(0xFF2E7D32)
    val avatarText = if (isGave) Color(0xFF5A1A1A) else Color.White
    val amountBoxBg = if (isGave) Color(0xFFF2D9DB) else Color(0xFFD7F4E5)
    val amountLabel = if (isGave) "Amount (₹)" else "Amount"
    val saveButtonActiveBg = if (isGave) Color(0xFF7F1D1D) else Color(0xFF0F5132)
    val saveButtonDisabledBg = if (isGave) Color(0xFFDEC5C8) else Color(0xFFBBE5D0)

    val currentAmount = amountText.toDoubleOrNull() ?: 0.0
    val isAmountValid = currentAmount > 0.0
    val dynamicTitleAmount = if (amountText.isNotBlank()) amountText else "0"
    val dynamicTitle = if (isGave) {
        "You gave ₹ $dynamicTitleAmount to $customerName"
    } else {
        "You got ₹ $dynamicTitleAmount from $customerName"
    }

    val customerInitials = remember(customerName) {
        val parts = customerName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.isEmpty() -> "C"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onDismiss() }

        Scaffold(
            containerColor = pageBgColor,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = headerTint
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Circular Avatar with Initials
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customerInitials,
                            color = avatarText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = dynamicTitle,
                        color = headerTint,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Button(
                        onClick = {
                            if (isAmountValid) {
                                onSave(currentAmount, description.trim(), dateIso, attachmentBase64)
                            }
                        },
                        enabled = isAmountValid,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = saveButtonActiveBg,
                            disabledContainerColor = saveButtonDisabledBg,
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.95f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "SAVE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Amount Box Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(amountBoxBg)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            text = amountLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = headerTint
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "₹",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        amountText = input
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(headerTint),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (amountText.isEmpty()) {
                                            Text(
                                                text = "0",
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Transaction Details (Optional) Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaction Details (Optional)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Supports multi-line & formats",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Inner outlined text area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, Color(0xFFD1D5DB)), RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = TextStyle(
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1F2937),
                                    lineHeight = 20.sp
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                                decorationBox = { innerTextField ->
                                    if (description.isEmpty()) {
                                        Text(
                                            text = "Enter details (Items, bill no., quantity, rate, etc.)",
                                            fontSize = 13.5.sp,
                                            color = Color(0xFF9CA3AF),
                                            lineHeight = 20.sp
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Subtle resize grip canvas at bottom-right
                            Canvas(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                val stroke = 1.5.dp.toPx()
                                val gripColor = Color(0xFF9CA3AF).copy(alpha = 0.6f)
                                drawLine(gripColor, Offset(size.width, size.height * 0.35f), Offset(size.width * 0.35f, size.height), stroke)
                                drawLine(gripColor, Offset(size.width, size.height * 0.72f), Offset(size.width * 0.72f, size.height), stroke)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4 Quick action chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormatPillChip(
                                label = "• Bullet",
                                onClick = {
                                    description = when {
                                        description.isBlank() -> "• "
                                        description.endsWith("\n") -> "$description• "
                                        else -> "$description\n• "
                                    }
                                }
                            )

                            FormatPillChip(
                                label = "+ Bill No",
                                onClick = {
                                    description = when {
                                        description.isBlank() -> "Bill No: "
                                        description.endsWith("\n") -> "${description}Bill No: "
                                        else -> "$description\nBill No: "
                                    }
                                }
                            )

                            FormatPillChip(
                                label = "+ Item List",
                                onClick = {
                                    val itemTemplate = "Items:\n1. \n2. "
                                    description = when {
                                        description.isBlank() -> itemTemplate
                                        description.endsWith("\n") -> "$description$itemTemplate"
                                        else -> "$description\n$itemTemplate"
                                    }
                                }
                            )

                            FormatPillChip(
                                label = "+ Note",
                                onClick = {
                                    description = when {
                                        description.isBlank() -> "Note: "
                                        description.endsWith("\n") -> "${description}Note: "
                                        else -> "$description\nNote: "
                                    }
                                }
                            )
                        }
                    }
                }

                // 3. Date & Attach Bills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Pill Button: Date
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.2.dp, Color(0xFF374151)), RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        selectedCalendar = newCal
                                    },
                                    selectedCalendar.get(Calendar.YEAR),
                                    selectedCalendar.get(Calendar.MONTH),
                                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = displayDate,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Date",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Right Pill Button: Attach Bills
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.2.dp, Color(0xFF374151)), RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .clickable {
                                galleryLauncher.launch("image/*")
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Attach Bills",
                                tint = if (attachmentBase64 != null) Color(0xFF0F5132) else Color(0xFF1F2937),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (attachmentBase64 != null) "1 Bill Attached" else "Attach Bills",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (attachmentBase64 != null) Color(0xFF0F5132) else Color(0xFF1F2937),
                                maxLines = 1
                            )
                            if (attachmentBase64 != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { attachmentBase64 = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Bill",
                                        tint = Color(0xFF991B1B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatPillChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Color(0xFFD1D5DB)), RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}
