package com.sumanonline.hisabkhata.feature.customer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.MaterialTheme
import com.sumanonline.hisabkhata.core.common.Constants
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.GreenPillBg
import com.sumanonline.hisabkhata.core.designsystem.NavyBluePrimary
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.RedPillBg
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import com.sumanonline.hisabkhata.core.designsystem.components.LedgerScreenSkeleton
import com.sumanonline.hisabkhata.core.designsystem.components.shimmerBrush
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    val transactionsFlow = remember(customerId) { viewModel.getTransactionsForCustomer(customerId) }
    val transactions by transactionsFlow.collectAsState()
    val shimmer = shimmerBrush()

    var showAddGaveSheet by remember { mutableStateOf(false) }
    var showAddGotSheet by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var fullScreenAttachmentUrl by remember { mutableStateOf<String?>(null) }
    var showPartyProfileSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    var savedScrollIndex by remember { mutableStateOf<Int?>(null) }
    var savedScrollOffset by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(fullScreenAttachmentUrl) {
        if (fullScreenAttachmentUrl == null && savedScrollIndex != null) {
            listState.scrollToItem(savedScrollIndex!!, savedScrollOffset ?: 0)
        }
    }

    if (customer == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
        ) {
            LedgerScreenSkeleton(brush = shimmer)
        }
        return
    }

    if (selectedTransaction != null) {
        EntryDetailsScreen(
            customer = customer,
            transaction = selectedTransaction!!,
            viewModel = viewModel,
            onNavigateBack = { selectedTransaction = null },
            onDelete = { txId, amount ->
                viewModel.deleteTransaction(customer.id, txId, amount)
                selectedTransaction = null
            }
        )
        return
    }

    val isGet = customer.balance < 0.0
    val isGive = customer.balance > 0.0
    val isSettled = customer.balance == 0.0

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue)
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "YOU GAVE ₹",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showAddGotSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GotGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "YOU GOT ₹",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item(key = "customer_header_and_balance_card", contentType = "header_and_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandBlue)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        Box(modifier = Modifier.size(42.dp)) {
                            if (!customer.photoURL.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(ImageUtils.sanitizeImageSource(customer.photoURL))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = customer.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                CustomerAvatar(
                                    name = customer.name,
                                    photoUrl = customer.photoURL,
                                    size = 42.dp,
                                    showActiveBadge = false
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showPartyProfileSheet = true }
                        ) {
                            Text(
                                text = customer.name,
                                color = Color.White,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showPartyProfileSheet = true }
                            ) {
                                Text(
                                    text = "View settings",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = ">",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (customer.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 14.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val statusLabel = when {
                                    isSettled -> "Settled"
                                    isGet -> "You will get"
                                    else -> "You will give"
                                }
                                Text(
                                    text = statusLabel,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = CurrencyFormatter.formatInrClean(Math.abs(customer.balance), withSpace = false),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSettled -> TextPrimary
                                        isGet -> GiveRed
                                        else -> GotGreen
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BorderLight, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showReminderDialog = true }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_calendar_reminder),
                                        contentDescription = null,
                                        tint = BrandBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SET COLLECTION REMINDER",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlue,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Text(
                                    text = if (customer.dueDate.isNullOrBlank()) "SET DATE" else customer.dueDate,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                    modifier = Modifier.clickable { showReminderDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            stickyHeader(key = "sticky_action_and_table_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickActionButton(
                            painter = painterResource(id = R.drawable.ic_action_pdf),
                            label = "Report",
                            onClick = { onNavigateToReport(customer.id) }
                        )

                        QuickActionButton(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            label = "WhatsApp",
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
                            painter = painterResource(id = R.drawable.ic_quick_sms),
                            label = "SMS",
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
                                        Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        QuickActionButton(
                            painter = painterResource(id = R.drawable.ic_quick_email),
                            label = "Email",
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
                            label = "Copy Link",
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = {
                                val shareUrl = "${Constants.WEB_PORTAL_BASE_URL}customer/share/${customer.id}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HisabKhata Share Link", shareUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(BorderStroke(1.dp, BorderLight))
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENTRIES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .weight(1.4f)
                                .padding(start = 16.dp)
                        )

                        Text(
                            text = "YOU GAVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(0.8f)
                        )

                        Text(
                            text = "YOU GOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }
            }

            if (transactions.isEmpty()) {
                item(contentType = "empty_state") {
                    EmptyStateView(
                        title = "No transactions yet",
                        message = "Record cash/online payments or credit sales using the buttons below."
                    )
                }
            } else {
                groupedTransactions.forEach { (dateKey, txList) ->
                    item(key = "date_header_$dateKey", contentType = "date_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(SurfaceWhite)
                                    .border(1.dp, BorderLight, RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = DateUtils.formatDatePill(dateKey, txList.firstOrNull()?.timestamp ?: System.currentTimeMillis()),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    items(txList, key = { it.id }, contentType = { "ledger_row" }) { tx ->
                        LedgerTableRowItem(
                            tx = tx,
                            onClick = { selectedTransaction = tx },
                            onAttachmentClick = { attachmentUrl ->
                                savedScrollIndex = listState.firstVisibleItemIndex
                                savedScrollOffset = listState.firstVisibleItemScrollOffset
                                fullScreenAttachmentUrl = attachmentUrl
                            }
                        )
                    }
                }

                item(contentType = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

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
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+7 Days", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomerDueDate(customer.id, 14)
                                showReminderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+14 Days", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.setCustomerDueDate(customer.id, 30)
                                showReminderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+30 Days", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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



        fullScreenAttachmentUrl?.let { rawUrl ->
            BackHandler {
                fullScreenAttachmentUrl = null
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .zIndex(100f)
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

    if (showPartyProfileSheet) {
        PartyProfileBottomSheet(
            customer = customer,
            onDismiss = { showPartyProfileSheet = false },
            onEditProfile = {
                showPartyProfileSheet = false
                showEditDialog = true
            },
            onImportLedger = {
                Toast.makeText(context, "Import ledger functionality coming soon", Toast.LENGTH_SHORT).show()
            },
            onDeleteParty = {
                showPartyProfileSheet = false
                showDeleteConfirm = true
            }
        )
    }

    if (showEditDialog) {
        EditCustomerProfileBottomSheet(
            customer = customer,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCustomer ->
                viewModel.updateCustomer(updatedCustomer)
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
 * 5 Quick Action circular buttons (Report, WhatsApp, SMS, Email, Copy Link)
 */
@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    iconTint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .border(1.dp, BorderLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Transaction Table Row matching the uploaded designs:
 * [ Left: Date & Time, Balance Pill, Note | Middle: YOU GAVE | Right: YOU GOT ]
 */
@Composable
private fun LedgerTableRowItem(
    tx: TransactionEntity,
    onClick: () -> Unit,
    onAttachmentClick: (String) -> Unit
) {
    val isGave = tx.amount < 0 || tx.type == "GAVE"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SurfaceWhite
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val dividerCol = BorderLight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val col1Right = size.width * (1.4f / 3.0f)
                        val col2Right = size.width * (2.2f / 3.0f)
                        drawLine(
                            color = dividerCol,
                            start = Offset(col1Right, 0f),
                            end = Offset(col1Right, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = dividerCol,
                            start = Offset(col2Right, 0f),
                            end = Offset(col2Right, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = DateUtils.formatDisplayDateTime(tx.timestamp),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val isZero = tx.balance == 0.0
                    val isNegative = tx.balance < 0.0
                    val absBal = Math.abs(tx.balance)
                    val balFormattedClean = CurrencyFormatter.formatInrClean(absBal, withSpace = false).replace("₹", "")
                    val balText = if (isZero) "Bal. ₹0" else if (isNegative) "Bal. ₹-$balFormattedClean" else "Bal. ₹$balFormattedClean"
                    val pillBg = if (isZero || !isNegative) GreenPillBg else RedPillBg
                    val pillText = if (isZero || !isNegative) GotGreen else GiveRed

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(pillBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = balText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = pillText
                        )
                    }

                    if (tx.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tx.description,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 18.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGave) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = CurrencyFormatter.formatInrClean(Math.abs(tx.amount), withSpace = false),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GiveRed
                            )
                            if (!tx.attachment.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_attachment_image),
                                    contentDescription = "Attachment",
                                    tint = BrandBlue,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onAttachmentClick(tx.attachment) }
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(horizontal = 4.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isGave) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = CurrencyFormatter.formatInrClean(Math.abs(tx.amount), withSpace = false),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GotGreen
                            )
                            if (!tx.attachment.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_attachment_image),
                                    contentDescription = "Attachment",
                                    tint = BrandBlue,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onAttachmentClick(tx.attachment) }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = BorderLight, thickness = 1.dp)
        }
    }
}
