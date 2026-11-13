package com.sumanonline.hisabkhata.feature.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.database.entity.PendingPaymentEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BorderMedium
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedStroke
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.GotGreenLight
import com.sumanonline.hisabkhata.core.designsystem.GotGreenStroke
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.RedPillBg
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.WarningAmber
import com.sumanonline.hisabkhata.core.designsystem.WarningAmberLight
import com.sumanonline.hisabkhata.core.designsystem.isAppDark
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel,
    onTabSelected: (BottomTab) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLedger: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val session by viewModel.session.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredPayments by viewModel.filteredPayments.collectAsState()
    val customerMap by viewModel.customerMap.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedProofPayment by remember { mutableStateOf<PendingPaymentEntity?>(null) }
    var showStoreQrSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.PAYMENTS,
                onTabSelected = onTabSelected
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top action bar (Header row with refresh & QR code button)
            item(key = "header_action_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Payments & Collections",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isRefreshing) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.refreshPayments() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                                .background(SurfaceWhite)
                                .clickable {
                                    viewModel.ensureQrGenerated()
                                    showStoreQrSheet = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode2,
                                contentDescription = "My Store QR",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Store QR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 1. Top 3 Summary Cards
            item(key = "summary_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pending Card
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF94A3B8))
                            )
                        },
                        count = summary.pendingCount.toString(),
                        subtitle = CurrencyFormatter.formatInr(summary.pendingTotal),
                        subtitleColor = TextSecondary,
                        isSelected = selectedFilter == PaymentFilter.PENDING,
                        accentColor = WarningAmber,
                        onClick = {
                            viewModel.setFilter(
                                if (selectedFilter == PaymentFilter.PENDING) PaymentFilter.ALL else PaymentFilter.PENDING
                            )
                        }
                    )

                    // Approved Card
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Approved",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = GotGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        count = summary.approvedCount.toString(),
                        subtitle = CurrencyFormatter.formatInr(summary.approvedTotal),
                        subtitleColor = GotGreen,
                        isSelected = selectedFilter == PaymentFilter.APPROVED,
                        accentColor = GotGreen,
                        onClick = {
                            viewModel.setFilter(
                                if (selectedFilter == PaymentFilter.APPROVED) PaymentFilter.ALL else PaymentFilter.APPROVED
                            )
                        }
                    )

                    // Rejected Card
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Rejected",
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = null,
                                tint = GiveRed,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        count = summary.rejectedCount.toString(),
                        subtitle = "Recorded",
                        subtitleColor = TextSecondary,
                        isSelected = selectedFilter == PaymentFilter.REJECTED,
                        accentColor = GiveRed,
                        onClick = {
                            viewModel.setFilter(
                                if (selectedFilter == PaymentFilter.REJECTED) PaymentFilter.ALL else PaymentFilter.REJECTED
                            )
                        }
                    )
                }
            }

            // 2. Sticky Filter Pills Row (sticks at the top when scrolling, title & summary cards scroll away)
            stickyHeader(key = "sticky_filter_pills") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PageBackground)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPillChip(
                            label = "All",
                            count = summary.allCount,
                            isSelected = selectedFilter == PaymentFilter.ALL,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.setFilter(PaymentFilter.ALL) }
                        )
                        FilterPillChip(
                            label = "Pending",
                            count = summary.pendingCount,
                            isSelected = selectedFilter == PaymentFilter.PENDING,
                            accentColor = WarningAmber,
                            onClick = { viewModel.setFilter(PaymentFilter.PENDING) }
                        )
                        FilterPillChip(
                            label = "Approved",
                            count = summary.approvedCount,
                            isSelected = selectedFilter == PaymentFilter.APPROVED,
                            accentColor = GotGreen,
                            onClick = { viewModel.setFilter(PaymentFilter.APPROVED) }
                        )
                        FilterPillChip(
                            label = "Rejected",
                            count = summary.rejectedCount,
                            isSelected = selectedFilter == PaymentFilter.REJECTED,
                            accentColor = GiveRed,
                            onClick = { viewModel.setFilter(PaymentFilter.REJECTED) }
                        )
                    }
                }
            }

            // 3. Search Bar
            item(key = "search_bar") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Search by customer or ref...",
                                fontSize = 13.5.sp,
                                color = TextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }
            }

            // 4. Payments List
            if (filteredPayments.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payments,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching payments" else "No payments recorded",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer online payments submitted via payment links will appear here.",
                                fontSize = 12.5.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredPayments,
                    key = { it.id },
                    contentType = { "payment_card" }
                ) { payment ->
                    val customer = customerMap[payment.customerId]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        PaymentCardItem(
                            payment = payment,
                            customerPhotoUrl = customer?.photoURL,
                            onApprove = {
                                viewModel.approvePayment(payment) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Payment approved & added to ledger", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onReject = {
                                viewModel.rejectPayment(payment.id) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Payment rejected", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onCopyRef = { ref ->
                                clipboard.setPrimaryClip(ClipData.newPlainText("Transaction Ref", ref))
                                Toast.makeText(context, "Copied Reference ID", Toast.LENGTH_SHORT).show()
                            },
                            onViewProof = {
                                selectedProofPayment = payment
                            },
                            onNavigateToLedger = {
                                if (payment.customerId.isNotBlank()) {
                                    onNavigateToLedger(payment.customerId)
                                } else {
                                    Toast.makeText(context, "Customer details not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Fullscreen / Dialog Payment Proof Viewer
    if (selectedProofPayment != null) {
        val payment = selectedProofPayment!!
        Dialog(
            onDismissRequest = { selectedProofPayment = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val imageModel = remember(payment.screenshot) {
                    ImageUtils.sanitizeImageSource(payment.screenshot)
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Payment Proof",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 72.dp)
                )

                // Top bar with info and close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = payment.customerName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Amount: ${CurrencyFormatter.formatInr(payment.amount)} • Ref: ${payment.transactionId ?: "N/A"}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = { selectedProofPayment = null },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // If pending, allow 1-tap Approve/Reject right inside Proof preview
                if (payment.status.equals("pending", ignoreCase = true)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.rejectPayment(payment.id) {
                                    selectedProofPayment = null
                                    Toast.makeText(context, "Payment rejected", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GiveRed),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.approvePayment(payment) {
                                    selectedProofPayment = null
                                    Toast.makeText(context, "Payment approved & added to ledger", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GotGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Approve Payment", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // In-store QR Code Bottom Sheet
    if (showStoreQrSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val qrBitmap by viewModel.qrBitmap.collectAsState()
        val customAmount by viewModel.customAmount.collectAsState()
        val merchantName = session.businessName.ifBlank { session.displayName }
        val upiId = session.upiId.ifBlank { "merchant@upi" }

        ModalBottomSheet(
            onDismissRequest = { showStoreQrSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = merchantName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "UPI ID: $upiId",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "UPI QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (session.upiId.isNotBlank()) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Configure UPI ID in Profile to generate your payment QR",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    showStoreQrSheet = false
                                    onNavigateToProfile()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                            ) {
                                Text("Set UPI ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { viewModel.setCustomAmount(it) },
                    label = { Text("Specify Amount to Collect (₹ Optional)") },
                    leadingIcon = {
                        Text("₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextTertiary, modifier = Modifier.padding(start = 12.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val amountParam = if (customAmount.toDoubleOrNull() != null && customAmount.toDouble() > 0) "&am=$customAmount" else ""
                            val upiUri = Uri.parse("upi://pay?pa=$upiId&pn=${java.net.URLEncoder.encode(merchantName, "UTF-8")}$amountParam&cu=INR")
                            val intent = Intent(Intent.ACTION_VIEW, upiUri)
                            context.startActivity(Intent.createChooser(intent, "Pay via UPI App"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GotGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Open in UPI App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val amountParam = if (customAmount.toDoubleOrNull() != null && customAmount.toDouble() > 0) "&am=$customAmount" else ""
                            val upiUri = "upi://pay?pa=$upiId&pn=${java.net.URLEncoder.encode(merchantName, "UTF-8")}$amountParam&cu=INR"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Pay $merchantName via UPI: $upiUri")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Payment Link"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Link", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit,
    count: String,
    subtitle: String,
    subtitleColor: Color = TextSecondary,
    isSelected: Boolean,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else BorderLight
    val containerBg = if (isSelected) accentColor.copy(alpha = if (isAppDark) 0.12f else 0.05f) else SurfaceWhite

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                icon()
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = if (subtitleColor != TextSecondary) FontWeight.Bold else FontWeight.Normal,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun FilterPillChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val containerBg = if (isSelected) {
        accentColor.copy(alpha = if (isAppDark) 0.20f else 0.12f)
    } else {
        SurfaceWhite
    }
    val borderColor = if (isSelected) {
        accentColor
    } else {
        BorderLight
    }
    val textColor = if (isSelected) {
        accentColor
    } else {
        TextSecondary
    }
    val badgeBg = if (isSelected) {
        accentColor
    } else {
        if (count > 0) accentColor.copy(alpha = if (isAppDark) 0.22f else 0.12f) else BorderLight.copy(alpha = 0.5f)
    }
    val badgeTextColor = if (isSelected) {
        Color.White
    } else {
        if (count > 0) accentColor else TextSecondary
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(containerBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(badgeBg)
                .padding(horizontal = 7.dp, vertical = 1.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
        }
    }
}

@Composable
private fun PaymentCardItem(
    payment: PendingPaymentEntity,
    customerPhotoUrl: String?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onCopyRef: (String) -> Unit,
    onViewProof: () -> Unit,
    onNavigateToLedger: () -> Unit
) {
    val formattedAmount = remember(payment.amount) { CurrencyFormatter.formatInr(payment.amount) }
    val formattedDateTime = remember(payment.timestamp) { formatPaymentDateTime(payment.timestamp) }
    val displayRef = remember(payment.transactionId) {
        val raw = payment.transactionId
        if (raw.isNullOrBlank() || raw.equals("None", ignoreCase = true)) null
        else if (raw.length > 15) raw.take(12) + "…"
        else raw
    }
    val context = LocalContext.current
    val proofModel = remember(payment.screenshot) {
        payment.screenshot?.let { ImageUtils.sanitizeImageSource(it) }
    }
    val proofRequest = remember(proofModel) {
        if (proofModel != null) {
            ImageRequest.Builder(context)
                .data(proofModel)
                .size(60, 60)
                .crossfade(false)
                .build()
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Avatar, Name, Date/Time & Amount + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Avatar
                CustomerAvatarBadge(
                    name = payment.customerName,
                    photoUrl = customerPhotoUrl
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Customer Name and Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.customerName.ifBlank { "Customer" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDateTime,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount and Status Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedAmount,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    PaymentStatusBadge(status = payment.status)
                }
            }

            // Pending Review Action Row (if status is pending)
            if (payment.status.equals("pending", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GiveRedStroke),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = RedPillBg,
                            contentColor = GiveRed
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = GiveRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GotGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Ref on the left, Actions (Proof & Ledger) on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Ref ID (flexible, gracefully truncated if tight)
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ref: ",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )

                    if (displayRef != null && !payment.transactionId.isNullOrBlank()) {
                        val rawRef = payment.transactionId
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(0.5.dp, BorderLight, RoundedCornerShape(6.dp))
                                .background(BorderLight.copy(alpha = 0.25f))
                                .clickable { onCopyRef(rawRef) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayRef,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "None",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right side: Action pills (View Proof & Ledger)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View Proof Button (if screenshot attached)
                    if (proofRequest != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                .background(SurfaceWhite)
                                .clickable(onClick = onViewProof)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = proofRequest,
                                contentDescription = "Proof",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "View Proof",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Native Ledger Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isAppDark) 0.35f else 0.25f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isAppDark) 0.18f else 0.10f))
                            .clickable(onClick = onNavigateToLedger)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ledger",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "→",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(status: String) {
    when {
        status.equals("approved", ignoreCase = true) -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.8.dp, GotGreenStroke, RoundedCornerShape(12.dp))
                    .background(GotGreenLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = GotGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Approved",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GotGreen
                )
            }
        }
        status.equals("rejected", ignoreCase = true) -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.8.dp, GiveRedStroke, RoundedCornerShape(12.dp))
                    .background(RedPillBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = null,
                    tint = GiveRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Rejected",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GiveRed
                )
            }
        }
        else -> {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.8.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .background(WarningAmberLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Pending",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
            }
        }
    }
}

@Composable
private fun CustomerAvatarBadge(
    name: String,
    photoUrl: String?
) {
    if (!photoUrl.isNullOrBlank()) {
        val context = LocalContext.current
        val sanitized = remember(photoUrl) { ImageUtils.sanitizeImageSource(photoUrl) }
        val imageRequest = remember(sanitized) {
            ImageRequest.Builder(context)
                .data(sanitized)
                .size(132, 132)
                .crossfade(true)
                .build()
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    } else {
        val initial = remember(name) {
            name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        }
        val bgColor = remember(name) { getAvatarPaletteColor(name) }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getAvatarPaletteColor(name: String): Color {
    val palette = listOf(
        Color(0xFF4F46E5), // Indigo (like Soumyadarshan)
        Color(0xFF0284C7), // Sky Blue (like Sandip)
        Color(0xFF0D9488), // Teal
        Color(0xFF16A34A), // Emerald
        Color(0xFFD97706), // Amber
        Color(0xFF7C3AED), // Purple
        Color(0xFFDB2777), // Pink
        Color(0xFF2563EB)  // Blue
    )
    val hash = kotlin.math.abs(name.hashCode())
    return palette[hash % palette.size]
}

private val paymentDateFormatThreadLocal = ThreadLocal.withInitial {
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
}

private fun formatPaymentDateTime(timestamp: Long): String {
    return try {
        paymentDateFormatThreadLocal.get()?.format(Date(timestamp)) ?: DateUtils.formatDisplayDateTime(timestamp)
    } catch (_: Exception) {
        DateUtils.formatDisplayDateTime(timestamp)
    }
}
