package com.sumanonline.hisabkhata.feature.customer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.sumanonline.hisabkhata.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.NavyBluePrimary
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CustomerLedgerScreen(
    customerId: String,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReport: (String) -> Unit
) {
    val context = LocalContext.current
    val customerState = viewModel.getCustomerById(customerId).collectAsState(initial = null)
    val customer = customerState.value
    val transactions by viewModel.getTransactionsForCustomer(customerId).collectAsState()

    var showAddGaveSheet by remember { mutableStateOf(false) }
    var showAddGotSheet by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var fullScreenAttachmentUrl by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(PageBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading party details…", color = TextSecondary)
        }
        return
    }

    val isGet = customer.balance < 0.0
    val isGive = customer.balance > 0.0
    val isSettled = customer.balance == 0.0

    Scaffold(
        topBar = {
            // Navy status bar header that remains solid while content scrolls under
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBluePrimary)
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddGaveSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GiveRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↗",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOU GAVE ₹",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = { showAddGotSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GotGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↙",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOU GOT ₹",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = PageBackground
    ) { innerPadding ->
        val groupedTransactions = remember(transactions) {
            transactions.groupBy { tx ->
                if (tx.date.isNotBlank()) tx.date else DateUtils.formatIsoDate(Date(tx.timestamp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Customer Name Navy Header Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyBluePrimary)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        CustomerAvatar(
                            name = customer.name,
                            photoUrl = customer.photoURL,
                            size = 40.dp,
                            showActiveBadge = true
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = customer.name,
                                    color = Color.White,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showEditDialog = true }
                            ) {
                                Text(
                                    text = "View settings & profile",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Call Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    if (customer.phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // More Options Button
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { showMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("View Statement / Report") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToReport(customer.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit Party Details") },
                                    onClick = {
                                        showMenu = false
                                        showEditDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Party", color = GiveRed) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Top Section 1: Net Due Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Row 1: Balance & QR Pay
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Net Due Balance",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.formatInr(Math.abs(customer.balance)),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            isSettled -> Color(0xFF0F172A)
                                            isGet -> GiveRed
                                            else -> GotGreen
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isSettled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF0FDF4))
                                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Settled",
                                                    color = Color(0xFF16A34A),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    } else if (isGet) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFEF2F2))
                                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "You'll Get",
                                                color = GiveRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF0FDF4))
                                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "You'll Give",
                                                color = GotGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            // QR Pay Box
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val shareUrl = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Pay using QR or online link: $shareUrl"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share QR Pay"))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "QR Pay",
                                        tint = NavyBluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "QR Pay",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyBluePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2: Set Collection Reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = NavyBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SET COLLECTION REMINDER",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                    .clickable { showReminderDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (customer.dueDate.isNullOrBlank()) "SET DATE" else customer.dueDate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyBluePrimary
                                )
                            }
                        }
                    }
                }
            }

            // Top Section 2: Quick Action Buttons Row (Report, WhatsApp, SMS, Email, Pay Link)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Description,
                            label = "Report",
                            iconTint = Color(0xFF2563EB),
                            circleBg = Color(0xFFEFF6FF),
                            onClick = { onNavigateToReport(customer.id) }
                        )

                        QuickActionButton(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            label = "WhatsApp",
                            iconTint = Color(0xFF16A34A),
                            circleBg = Color(0xFFDCFCE7),
                            onClick = {
                                if (customer.phone.isNotBlank()) {
                                    try {
                                        val rawPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
                                        val targetPhone = if (rawPhone.length == 10) "91$rawPhone" else rawPhone
                                        val shareUrl = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"
                                        val balFormatted = CurrencyFormatter.formatInr(Math.abs(customer.balance))
                                        val msg = "👋 *Hello ${customer.name},*\n\n📢 *Payment Reminder from HisabKhata Web*\n\nYour outstanding balance is: *$balFormatted*\n\n🔗 *View your full digital statement here:*\n$shareUrl\n\n🙏 Please verify your transactions and clear dues. Thank you!"
                                        val waIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(msg)}")
                                        }
                                        context.startActivity(waIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        QuickActionButton(
                            icon = Icons.Default.Sms,
                            label = "SMS",
                            iconTint = Color(0xFFD97706),
                            circleBg = Color(0xFFFEF3C7),
                            onClick = {
                                if (customer.phone.isNotBlank()) {
                                    val rawPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
                                    val targetPhone = if (rawPhone.length == 10) "91$rawPhone" else rawPhone
                                    val shareUrl = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"
                                    val balFormatted = CurrencyFormatter.formatInr(Math.abs(customer.balance))
                                    val msg = "👋 *Hello ${customer.name},*\n\n📢 *Payment Reminder from HisabKhata Web*\n\nYour outstanding balance is: *$balFormatted*\n\n🔗 *View your full digital statement here:*\n$shareUrl\n\n🙏 Please verify your transactions and clear dues. Thank you!"
                                    val smsUri = Uri.parse("smsto:$targetPhone")
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                        putExtra("sms_body", msg)
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    try {
                                        context.startActivity(smsIntent)
                                    } catch (_: Exception) {
                                        try {
                                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$targetPhone")).apply {
                                                putExtra("sms_body", msg)
                                                putExtra(Intent.EXTRA_TEXT, msg)
                                            }
                                            context.startActivity(fallbackIntent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        QuickActionButton(
                            icon = Icons.Default.Email,
                            label = "Email",
                            iconTint = Color(0xFFDC2626),
                            circleBg = Color(0xFFFEE2E2),
                            onClick = {
                                if (customer.email.isNotBlank()) {
                                    Toast.makeText(context, "Sending reminder email to ${customer.email}…", Toast.LENGTH_SHORT).show()
                                    viewModel.sendEmailReminder(customer) { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No email address saved for ${customer.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        QuickActionButton(
                            icon = Icons.Default.Link,
                            label = "Pay Link",
                            iconTint = Color(0xFF7C3AED),
                            circleBg = Color(0xFFF3E8FF),
                            onClick = {
                                val shareUrl = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HisabKhata Payment Link", shareUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Payment link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Section 3: Entries Table Header Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.35f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENTRIES",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort entries",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "YOU GAVE",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiveRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.85f)
                    )

                    Text(
                        text = "YOU GOT",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GotGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.85f)
                    )
                }
            }

            // Section 4: Transactions List with Date Grouping & Table Layout
            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No transactions yet",
                        message = "Record cash/online payments or credit sales using the buttons below."
                    )
                }
            } else {
                groupedTransactions.forEach { (dateKey, txList) ->
                    // Date Pill Header
                    item(key = "date_header_$dateKey") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceWhite)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = formatDatePill(dateKey, txList.firstOrNull()?.timestamp ?: System.currentTimeMillis()),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Table Transaction Rows
                    items(txList, key = { it.id }) { tx ->
                        LedgerTableRowItem(
                            tx = tx,
                            onClick = { selectedTransaction = tx },
                            onAttachmentClick = { attachmentUrl ->
                                fullScreenAttachmentUrl = attachmentUrl
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Reminder Date Selection Dialog
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Set Collection Reminder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a reminder timeframe for ${customer.name}:", color = TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setCustomerDueDate(customer.id, 7)
                                showReminderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+7 Days", color = NavyBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomerDueDate(customer.id, 14)
                                showReminderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+14 Days", color = NavyBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomerDueDate(customer.id, 30)
                                showReminderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+30 Days", color = NavyBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (!customer.dueDate.isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            viewModel.clearCustomerDueDate(customer.id)
                            showReminderDialog = false
                        }
                    ) {
                        Text("Clear Reminder", color = GiveRed, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showAddGaveSheet) {
        AddTransactionBottomSheet(
            customerName = customer.name,
            isGave = true,
            onDismiss = { showAddGaveSheet = false },
            onSave = { amount, description, date ->
                viewModel.addTransaction(customer.id, amount, true, description, date)
                showAddGaveSheet = false
            }
        )
    }

    if (showAddGotSheet) {
        AddTransactionBottomSheet(
            customerName = customer.name,
            isGave = false,
            onDismiss = { showAddGotSheet = false },
            onSave = { amount, description, date ->
                viewModel.addTransaction(customer.id, amount, false, description, date)
                showAddGotSheet = false
            }
        )
    }

    selectedTransaction?.let { tx ->
        TransactionDetailBottomSheet(
            transaction = tx,
            customerName = customer.name,
            onDismiss = { selectedTransaction = null },
            onDelete = { txId, amount ->
                viewModel.deleteTransaction(customer.id, txId, amount)
                selectedTransaction = null
            }
        )
    }

    fullScreenAttachmentUrl?.let { rawUrl ->
        Dialog(
            onDismissRequest = { fullScreenAttachmentUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val imageModel = remember(rawUrl) {
                    ImageUtils.sanitizeImageSource(rawUrl)
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Attachment",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Attachment Preview",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { fullScreenAttachmentUrl = null },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AddEditCustomerDialog(
            existingCustomer = customer,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, phone, email, _, _, type ->
                viewModel.updateCustomer(
                    customer.copy(name = name, phone = phone, email = email, type = type)
                )
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Party?",
            message = "Are you sure you want to delete ${customer.name}? All ledger records and history will be permanently deleted.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteCustomer(customer.id)
                onNavigateBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/**
 * 5 Quick Action circular buttons (Report, WhatsApp, SMS, Email, Pay Link)
 */
@Composable
private fun QuickActionButton(
    label: String,
    iconTint: Color,
    circleBg: Color,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Transaction Table Row: 3 columns matching the reference row layout
 * [ Details (Time, Note, Bal, Badges) | YOU GAVE | YOU GOT ]
 */
@Composable
private fun LedgerTableRowItem(
    tx: TransactionEntity,
    onClick: () -> Unit,
    onAttachmentClick: (String) -> Unit
) {
    val isGave = tx.amount < 0 || tx.type == "GAVE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Details (Time, Note, Badges)
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .padding(10.dp)
            ) {
                // Top line: Clock + Date & Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTxDateTime(tx.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Middle: Note / Description
                Text(
                    text = tx.description.ifBlank { if (isGave) "Credit given" else "Payment received" },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom: Separate Balance Pill (Red for negative, Green for surplus) & Separate Attachment Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Running Balance Pill
                    val isNegative = tx.balance < 0
                    val balFormatted = CurrencyFormatter.formatInr(Math.abs(tx.balance))
                    val balText = if (isNegative) "Bal. -₹${balFormatted.removePrefix("₹")}" else "Bal. $balFormatted"
                    val pillBg = if (isNegative) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                    val pillBorder = if (isNegative) Color(0xFFFECACA) else Color(0xFFBBF7D0)
                    val pillText = if (isNegative) Color(0xFFDC2626) else Color(0xFF16A34A)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(pillBg)
                            .border(1.dp, pillBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = balText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = pillText
                        )
                    }

                    // Separate Attachment Icon (Blue outline photo icon matching uploaded reference)
                    if (!tx.attachment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onAttachmentClick(tx.attachment) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_attachment_image),
                                contentDescription = "View Attachment",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // Vertical Divider 1
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFF1F5F9))
            )

            // Column 2: YOU GAVE
            Box(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGave) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = CurrencyFormatter.formatInr(Math.abs(tx.amount)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GiveRed
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Credit given",
                            fontSize = 10.sp,
                            color = GiveRed.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Vertical Divider 2
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFF1F5F9))
            )

            // Column 3: YOU GOT
            Box(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isGave) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = CurrencyFormatter.formatInr(Math.abs(tx.amount)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GotGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "✓ Received",
                            fontSize = 10.sp,
                            color = GotGreen.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTxDateTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.ENGLISH).format(Date(timestamp))
}

/**
 * Formats date pill header like "07 NOV 26 • 4 DAYS AGO"
 */
private fun formatDatePill(dateIso: String, timestamp: Long): String {
    val dateObj = DateUtils.parseIsoDate(dateIso) ?: Date(timestamp)
    val datePart = SimpleDateFormat("dd MMM yy", Locale.ENGLISH).format(dateObj).uppercase()

    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val targetCal = Calendar.getInstance().apply {
        time = dateObj
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffDays = ((todayCal.timeInMillis - targetCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()

    val relativePart = when {
        diffDays <= 0 -> "TODAY"
        diffDays == 1 -> "YESTERDAY"
        diffDays < 30 -> "$diffDays DAYS AGO"
        diffDays < 365 -> "${diffDays / 30} MONTHS AGO"
        else -> "${diffDays / 365} YEARS AGO"
    }

    return "$datePart • $relativePart"
}
