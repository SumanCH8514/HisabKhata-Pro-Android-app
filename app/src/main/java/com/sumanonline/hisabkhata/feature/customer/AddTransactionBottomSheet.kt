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
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.ImageUtils
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AttachedBillItem(
    val uri: Uri,
    val bitmap: Bitmap,
    val byteArray: ByteArray,
    val extension: String
)

@Composable
fun AddTransactionBottomSheet(
    customerName: String,
    customerPhotoUrl: String? = null,
    isGave: Boolean,
    onDismiss: () -> Unit,
    onUploadBill: suspend (imageBytes: ByteArray, extension: String) -> String?,
    onSave: (amount: Double, description: String, date: String, primaryAttachment: String?, allAttachments: List<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachedBills by remember { mutableStateOf<List<AttachedBillItem>>(emptyList()) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var isSaving by remember { mutableStateOf(false) }

    val displayDate = remember(selectedCalendar) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedCalendar.time)
    }
    val dateIso = remember(selectedCalendar) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remaining = 4 - attachedBills.size
            if (remaining <= 0) {
                Toast.makeText(context, "Maximum 4 bills can be attached", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            val newBills = mutableListOf<AttachedBillItem>()
            var invalidFormatCount = 0

            for (uri in uris) {
                if (attachedBills.size + newBills.size >= 4) {
                    Toast.makeText(context, "Maximum 4 bills limit reached", Toast.LENGTH_SHORT).show()
                    break
                }

                val mime = context.contentResolver.getType(uri)?.lowercase() ?: ""
                val uriStr = uri.toString().lowercase()
                val isImageFormatSupported = mime == "image/jpeg" || mime == "image/jpg" || mime == "image/png" ||
                        uriStr.endsWith(".jpg") || uriStr.endsWith(".jpeg") || uriStr.endsWith(".png")

                if (!isImageFormatSupported) {
                    invalidFormatCount++
                    continue
                }

                try {
                    val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }

                    val maxDim = 1200
                    val scaled = if (rawBitmap.width > maxDim || rawBitmap.height > maxDim) {
                        val scale = maxDim.toFloat() / maxOf(rawBitmap.width, rawBitmap.height)
                        Bitmap.createScaledBitmap(
                            rawBitmap,
                            (rawBitmap.width * scale).toInt().coerceAtLeast(1),
                            (rawBitmap.height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else {
                        rawBitmap
                    }

                    val ext = if (mime == "image/png" || uriStr.endsWith(".png")) "png" else "jpg"
                    val format = if (ext == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    val baos = ByteArrayOutputStream()
                    scaled.compress(format, 85, baos)
                    val bytes = baos.toByteArray()

                    newBills.add(AttachedBillItem(uri, scaled, bytes, ext))
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            if (invalidFormatCount > 0) {
                Toast.makeText(context, "Only JPG, JPEG, and PNG images are supported", Toast.LENGTH_SHORT).show()
            }

            if (newBills.isNotEmpty()) {
                attachedBills = attachedBills + newBills
                Toast.makeText(context, "${newBills.size} bill(s) attached (stored locally)", Toast.LENGTH_SHORT).show()
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
    val dynamicTitleAmount = amountText.ifBlank { "0" }
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

    val sanitizedPhoto = remember(customerPhotoUrl) { ImageUtils.sanitizeImageSource(customerPhotoUrl) }
    var photoLoadFailed by remember(sanitizedPhoto) { mutableStateOf(false) }

    // Intercept back navigation
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBgColor)
            .zIndex(100f)
    ) {
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

                    // Circular Avatar with Profile Picture or Initials
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sanitizedPhoto != null && !photoLoadFailed) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(sanitizedPhoto)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = customerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                onError = { photoLoadFailed = true }
                            )
                        } else {
                            Text(
                                text = customerInitials,
                                color = avatarText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = dynamicTitle,
                        color = headerTint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 21.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            bottomBar = {
                Surface(
                    color = pageBgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isAmountValid && !isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val uploadedUrls = mutableListOf<String>()
                                        for (item in attachedBills) {
                                            val r2Url = onUploadBill(item.byteArray, item.extension)
                                            if (r2Url != null) {
                                                uploadedUrls.add(r2Url)
                                            } else {
                                                val b64 = Base64.encodeToString(item.byteArray, Base64.NO_WRAP)
                                                val mime = if (item.extension == "png") "image/png" else "image/jpeg"
                                                uploadedUrls.add("data:$mime;base64,$b64")
                                            }
                                        }
                                        val primary = uploadedUrls.firstOrNull()
                                        onSave(currentAmount, description.trim(), dateIso, primary, uploadedUrls)
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = isAmountValid && !isSaving,
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
                            if (isSaving) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "SAVING & UPLOADING...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = "SAVE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
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
                        // Header row without overlap
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = "Transaction Details",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Optional)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Multi-line formats",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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

                        // 5 Quick action chips: Bullet, Bill No, Item List, Note, TNX
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

                            FormatPillChip(
                                label = "+ TNX",
                                onClick = {
                                    val tnxText = "Online payment received with transaction id: xxxx into SBI account xx137."
                                    description = when {
                                        description.isBlank() -> tnxText
                                        description.endsWith("\n") -> "$description$tnxText"
                                        else -> "$description\n$tnxText"
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
                                if (attachedBills.size < 4) {
                                    galleryLauncher.launch("image/*")
                                } else {
                                    Toast.makeText(context, "Maximum 4 bills attached", Toast.LENGTH_SHORT).show()
                                }
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
                                tint = if (attachedBills.isNotEmpty()) Color(0xFF0F5132) else Color(0xFF1F2937),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (attachedBills.size) {
                                    0 -> "Attach Bills"
                                    4 -> "4/4 Bills (Max)"
                                    else -> "${attachedBills.size}/4 Attached"
                                },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (attachedBills.isNotEmpty()) Color(0xFF0F5132) else Color(0xFF1F2937),
                                maxLines = 1
                            )
                        }
                    }
                }

                // 4. Attached Bills Preview Section (Up to 4 images, local until Save)
                if (attachedBills.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attached Bills (${attachedBills.size}/4)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "Transferred to R2 on Save",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            attachedBills.forEachIndexed { index, item ->
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                ) {
                                    Image(
                                        bitmap = item.bitmap.asImageBitmap(),
                                        contentDescription = "Attached Bill ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(BorderStroke(1.dp, Color(0xFFD1D5DB)), RoundedCornerShape(10.dp))
                                    )

                                    // Delete (X) badge button
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDC2626))
                                            .clickable {
                                                attachedBills = attachedBills.toMutableList().apply { removeAt(index) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove bill",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            if (attachedBills.size < 4) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            BorderStroke(1.2.dp, Color(0xFF9CA3AF)),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .background(Color.White)
                                        .clickable {
                                            galleryLauncher.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add bill",
                                            tint = Color(0xFF4B5563),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Add",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF4B5563)
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
