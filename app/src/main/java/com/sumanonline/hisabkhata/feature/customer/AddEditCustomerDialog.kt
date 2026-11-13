package com.sumanonline.hisabkhata.feature.customer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    existingCustomer: CustomerEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        email: String,
        openingBalance: Double,
        balanceType: String,
        type: String,
        photoUrl: String?,
        gst: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(existingCustomer) { mutableStateOf(existingCustomer?.name ?: "") }
    var phone by remember(existingCustomer) {
        val raw = existingCustomer?.phone ?: ""
        val clean = if (raw.startsWith("+91")) raw.removePrefix("+91").trim()
        else if (raw.startsWith("+")) raw.removePrefix("+").trim()
        else raw.trim()
        mutableStateOf(clean)
    }
    var email by remember(existingCustomer) { mutableStateOf(existingCustomer?.email ?: "") }
    var openingBalance by remember(existingCustomer) {
        mutableStateOf(
            if (existingCustomer != null && existingCustomer.balance != 0.0) {
                Math.abs(existingCustomer.balance).toString()
            } else ""
        )
    }
    // "give" corresponds to "YOU GAVE" (red pill in screenshot), "get" corresponds to "YOU GOT" (green pill)
    var balanceType by remember(existingCustomer) {
        mutableStateOf(
            if (existingCustomer != null && existingCustomer.balance < 0) "get" else "give"
        )
    }
    var showBalanceTypeMenu by remember { mutableStateOf(false) }

    var partyType by remember(existingCustomer) {
        mutableStateOf(existingCustomer?.type ?: "Customer")
    }

    var photoUrl by remember(existingCustomer) {
        mutableStateOf(existingCustomer?.photoURL)
    }

    // Expandable GSTIN & Address section
    var showGstAddress by remember { mutableStateOf(!existingCustomer?.gst.isNullOrBlank()) }
    var gst by remember(existingCustomer) { mutableStateOf(existingCustomer?.gst ?: "") }
    var address by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

    // Dialog state for photo options
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showPasteUrlDialog by remember { mutableStateOf(false) }
    var pastedUrlInput by remember { mutableStateOf("") }

    // Image Picker from Gallery
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
                val targetSize = 256
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
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                photoUrl = "data:image/jpeg;base64,$base64"
                Toast.makeText(context, "Photo selected", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                photoUrl = uri.toString()
            }
        }
    }

    // Android Contact Picker
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = result.data?.data
            if (contactUri != null) {
                try {
                    var resolvedName: String? = null
                    var resolvedPhone: String? = null
                    var resolvedPhoto: String? = null

                    // 1. Try querying with CommonDataKinds.Phone projection
                    try {
                        val projection = arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                        )
                        context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                                if (nameIdx != -1) resolvedName = cursor.getString(nameIdx)
                                if (numIdx != -1) resolvedPhone = cursor.getString(numIdx)
                                if (photoIdx != -1) resolvedPhoto = cursor.getString(photoIdx)
                            }
                        }
                    } catch (_: Exception) {}

                    // 2. Fallback query if contactUri was from Contacts instead of Phone
                    if (resolvedName.isNullOrBlank() || resolvedPhone.isNullOrBlank()) {
                        try {
                            context.contentResolver.query(
                                contactUri,
                                arrayOf(
                                    ContactsContract.Contacts._ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.Contacts.PHOTO_URI
                                ),
                                null, null, null
                            )?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                                    val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

                                    if (nameIdx != -1 && resolvedName.isNullOrBlank()) {
                                        resolvedName = cursor.getString(nameIdx)
                                    }
                                    if (photoIdx != -1 && resolvedPhoto.isNullOrBlank()) {
                                        resolvedPhoto = cursor.getString(photoIdx)
                                    }
                                    if (idIdx != -1 && resolvedPhone.isNullOrBlank()) {
                                        val contactId = cursor.getString(idIdx)
                                        context.contentResolver.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                            arrayOf(contactId),
                                            null
                                        )?.use { phoneCursor ->
                                            if (phoneCursor.moveToFirst()) {
                                                resolvedPhone = phoneCursor.getString(0)
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    if (!resolvedName.isNullOrBlank()) {
                        name = resolvedName!!.trim()
                    }
                    if (!resolvedPhone.isNullOrBlank()) {
                        var clean = resolvedPhone!!.replace("[\\s\\-()]".toRegex(), "").trim()
                        if (clean.startsWith("+91")) {
                            clean = clean.removePrefix("+91").trim()
                        } else if (clean.startsWith("91") && clean.length == 12) {
                            clean = clean.removePrefix("91").trim()
                        } else if (clean.startsWith("0") && clean.length == 11) {
                            clean = clean.removePrefix("0").trim()
                        }
                        phone = clean
                    }
                    if (!resolvedPhoto.isNullOrBlank() && photoUrl.isNullOrBlank()) {
                        photoUrl = resolvedPhoto
                    }
                    Toast.makeText(context, "Contact selected: ${name.ifBlank { phone }}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not read contact: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun launchContactPicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                contactPickerLauncher.launch(fallbackIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "No Contacts app available on device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: Title and Close 'X'
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingCustomer != null) "Edit Party" else "Add New Party",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Photo Circle with edit pencil badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clickable { showPhotoOptionsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCE7FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(ImageUtils.sanitizeImageSource(photoUrl))
                                    .crossfade(true)
                                    .error(R.drawable.no_contact_image)
                                    .fallback(R.drawable.no_contact_image)
                                    .build(),
                                contentDescription = "Party Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera_add),
                                contentDescription = "Add Photo",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Small edit pencil badge at bottom right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit photo",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap to add photo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    modifier = Modifier.clickable { showPhotoOptionsDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Party Name Label Row with "Select from Contacts" Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Party Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFDCE7FE),
                    modifier = Modifier.clickable { launchContactPicker() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact_book),
                            contentDescription = "Contacts",
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select from Contacts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Party Name Input Field with trailing contact book icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (name.isEmpty()) {
                            Text(
                                text = "Enter Party Name",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(BrandBlue),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    IconButton(
                        onClick = { launchContactPicker() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_contact_book),
                            contentDescription = "Select from contacts",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Phone Number Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phone Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "(OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phone Number Input Container: +91 | [phone] | 📇
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
                    .background(Color.White, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+91",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF94A3B8))
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (phone.isEmpty()) {
                        Text(
                            text = "Enter Phone Number",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    BasicTextField(
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 10) {
                                phone = digits
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                IconButton(
                    onClick = { launchContactPicker() },
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_contact_book),
                        contentDescription = "Select from contacts",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Email Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "(OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Email Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (email.isEmpty()) {
                    Text(
                        text = "Enter Email Address",
                        fontSize = 15.sp,
                        color = Color(0xFF64748B)
                    )
                }
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A)
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(BrandBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Opening Balance Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opening Balance",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "(OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Opening Balance Composite Input: [ ₹ | Enter amount | YOU GAVE v ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
                    .background(Color.White, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF94A3B8))
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (openingBalance.isEmpty()) {
                        Text(
                            text = "Enter amount",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    BasicTextField(
                        value = openingBalance,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                openingBalance = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BrandBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Balance Type Pill (YOU GAVE in pink/red, YOU GOT in light green)
                val isGive = balanceType.equals("give", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGive) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                        .clickable { showBalanceTypeMenu = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isGive) "YOU GAVE" else "YOU GOT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGive) Color(0xFF991B1B) else Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Balance Type",
                            tint = if (isGive) Color(0xFF991B1B) else Color(0xFF166534),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showBalanceTypeMenu,
                        onDismissRequest = { showBalanceTypeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "YOU GAVE (Advance)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            },
                            onClick = {
                                balanceType = "give"
                                showBalanceTypeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "YOU GOT (Due)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            },
                            onClick = {
                                balanceType = "get"
                                showBalanceTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. "Who are they?" Section
            Text(
                text = "Who are they?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Customer Card
                val isCustomer = partyType.equals("Customer", ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { partyType = "Customer" },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCustomer) Color(0xFFDCE7FE) else Color.White,
                    border = BorderStroke(
                        width = if (isCustomer) 1.5.dp else 1.dp,
                        color = if (isCustomer) Color(0xFF1E3A8A) else Color(0xFF94A3B8)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCustomer) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1E3A8A),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(1.5.dp, Color(0xFF64748B), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer",
                            fontWeight = if (isCustomer) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Supplier Card
                val isSupplier = partyType.equals("Supplier", ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { partyType = "Supplier" },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSupplier) Color(0xFFDCE7FE) else Color.White,
                    border = BorderStroke(
                        width = if (isSupplier) 1.5.dp else 1.dp,
                        color = if (isSupplier) Color(0xFF1E3A8A) else Color(0xFF94A3B8)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSupplier) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1E3A8A),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(1.5.dp, Color(0xFF64748B), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Supplier",
                            fontWeight = if (isSupplier) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Collapsible "Add GSTIN & Address (Optional)" Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showGstAddress = !showGstAddress },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(1.5.dp, Color(0xFF0F172A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Add GSTIN & Address (Optional)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Icon(
                            imageVector = if (showGstAddress) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (showGstAddress) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // GSTIN Number
                        Text(
                            text = "GSTIN Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (gst.isEmpty()) {
                                Text(
                                    text = "e.g. 19ABCDE1234F1Z5",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = gst,
                                onValueChange = { gst = it.uppercase() },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Next
                                ),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Billing Address
                        Text(
                            text = "Billing Address",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (address.isEmpty()) {
                                Text(
                                    text = "Flat / Building / Street Address",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = address,
                                onValueChange = { address = it },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pincode and State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pincode",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (pincode.isEmpty()) {
                                        Text("Pincode", fontSize = 14.sp, color = Color(0xFF94A3B8))
                                    }
                                    BasicTextField(
                                        value = pincode,
                                        onValueChange = { pincode = it.filter { c -> c.isDigit() }.take(6) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A)),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "State",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(8.dp))
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (state.isEmpty()) {
                                        Text("State", fontSize = 14.sp, color = Color(0xFF94A3B8))
                                    }
                                    BasicTextField(
                                        value = state,
                                        onValueChange = { state = it },
                                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A)),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7. Bottom Action Button: ADD CUSTOMER / ADD SUPPLIER / SAVE CHANGES
            val isFormValid = name.isNotBlank()
            val buttonLabel = when {
                existingCustomer != null -> "SAVE CHANGES"
                partyType.equals("Supplier", ignoreCase = true) -> "ADD SUPPLIER"
                else -> "ADD CUSTOMER"
            }

            Button(
                onClick = {
                    if (isFormValid) {
                        val balance = openingBalance.toDoubleOrNull() ?: 0.0
                        val formattedPhone = if (phone.isNotBlank() && !phone.startsWith("+")) {
                            "+91$phone"
                        } else {
                            phone
                        }
                        onConfirm(
                            name.trim(),
                            formattedPhone.trim(),
                            email.trim(),
                            balance,
                            balanceType,
                            partyType,
                            photoUrl,
                            gst.trim().ifBlank { null }
                        )
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    disabledContainerColor = Color(0xFF94A3B8),
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = buttonLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Photo selection options dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Text("Party Photo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option 1: Gallery
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoOptionsDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = BrandBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from Gallery", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    // Option 2: Paste URL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoOptionsDialog = false
                                showPasteUrlDialog = true
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = BrandBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Paste Image URL", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    // Option 3: Remove photo (if set)
                    if (!photoUrl.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    photoUrl = null
                                    showPhotoOptionsDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = GiveRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Remove Photo", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GiveRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Paste Image URL Dialog
    if (showPasteUrlDialog) {
        AlertDialog(
            onDismissRequest = { showPasteUrlDialog = false },
            title = {
                Text("Paste Image URL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text("Enter direct web link to profile image:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedUrlInput,
                        onValueChange = { pastedUrlInput = it },
                        placeholder = { Text("https://example.com/photo.jpg") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedUrlInput.isNotBlank()) {
                            photoUrl = pastedUrlInput.trim()
                            pastedUrlInput = ""
                            showPasteUrlDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteUrlDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
