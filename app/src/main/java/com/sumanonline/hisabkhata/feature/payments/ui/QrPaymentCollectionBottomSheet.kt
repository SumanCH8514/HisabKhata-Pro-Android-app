package com.sumanonline.hisabkhata.feature.payments.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentRequest
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.WarningAmber
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.isAppDark
import com.sumanonline.hisabkhata.feature.customer.CustomerViewModel
import com.sumanonline.hisabkhata.feature.payments.model.QrPaymentStatus
import com.sumanonline.hisabkhata.feature.payments.notification.NotificationListenerPermissionHelper
import com.sumanonline.hisabkhata.feature.payments.util.PaymentQrPosterGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs

enum class QrAmountMode {
    OUTSTANDING_BALANCE,
    CUSTOM_AMOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPaymentCollectionBottomSheet(
    customer: CustomerEntity,
    viewModel: CustomerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by viewModel.session.collectAsState()
    val activeRequest by viewModel.activePaymentRequest.collectAsState()

    val outstandingDue = remember(customer.balance) {
        if (customer.balance < 0.0) abs(customer.balance) else 0.0
    }

    var selectedMode by remember {
        mutableStateOf(if (outstandingDue > 0.0) QrAmountMode.OUTSTANDING_BALANCE else QrAmountMode.CUSTOM_AMOUNT)
    }
    var customAmountText by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGeneratingQr by remember { mutableStateOf(false) }
    var isProcessingLedgerCredit by remember { mutableStateOf(false) }
    var currentRequestId by remember { mutableStateOf<String?>(null) }
    var customUtrInput by remember { mutableStateOf("") }
    var showManualVerifyDialog by remember { mutableStateOf(false) }

    var editUpiId by remember(session.upiId) { mutableStateOf(session.upiId) }
    var isSavingUpi by remember { mutableStateOf(false) }

    var isNotificationPermissionGranted by remember {
        mutableStateOf(NotificationListenerPermissionHelper.isPermissionGranted(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationPermissionGranted = NotificationListenerPermissionHelper.isPermissionGranted(context)
                if (isNotificationPermissionGranted) {
                    NotificationListenerPermissionHelper.requestServiceRebind(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentRequestId?.let { viewModel.cancelPaymentRequest(it) }
            viewModel.clearActivePaymentRequest()
        }
    }

    val effectiveAmount: Double = remember(selectedMode, outstandingDue, customAmountText) {
        when (selectedMode) {
            QrAmountMode.OUTSTANDING_BALANCE -> outstandingDue
            QrAmountMode.CUSTOM_AMOUNT -> customAmountText.toDoubleOrNull() ?: 0.0
        }
    }

    fun triggerQrGeneration(amount: Double) {
        if (session.upiId.isBlank() || amount <= 0.0) {
            qrBitmap = null
            return
        }

        scope.launch {
            isGeneratingQr = true
            val merchantName = session.businessName.ifBlank { session.displayName.ifBlank { "HisabKhata Merchant" } }
            val cleanName = URLEncoder.encode(merchantName, "UTF-8")
            val amountFormatted = String.format(Locale.US, "%.2f", amount)
            val note = URLEncoder.encode("Payment for ${customer.name}", "UTF-8")

            val request = viewModel.createQrPaymentRequest(
                customer = customer,
                amount = amount,
                isCustomAmount = selectedMode == QrAmountMode.CUSTOM_AMOUNT,
                upiUri = ""
            )
            currentRequestId = request.id

            val upiUri = "upi://pay?pa=${session.upiId.trim()}&pn=$cleanName&am=$amountFormatted&cu=INR&tr=${request.id}&tn=$note"

            val bmp = withContext(Dispatchers.Default) {
                try {
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, 512, 512)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                        }
                    }
                    bitmap
                } catch (_: Exception) {
                    null
                }
            }
            qrBitmap = bmp
            isGeneratingQr = false
        }
    }

    LaunchedEffect(selectedMode, effectiveAmount, session.upiId) {
        if (effectiveAmount > 0.0 && session.upiId.isNotBlank()) {
            triggerQrGeneration(effectiveAmount)
        } else {
            qrBitmap = null
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden
        }
    )

    LaunchedEffect(activeRequest?.status) {
        if (activeRequest?.status == QrPaymentStatus.LEDGER_CREDITED) {
            viewModel.clearActivePaymentRequest()
            onDismiss()
        }
    }

    if (activeRequest?.status == QrPaymentStatus.PAYMENT_DETECTED || activeRequest?.status == QrPaymentStatus.VERIFICATION_REQUIRED) {
        FullScreenPaymentDetectedPreview(
            customer = customer,
            request = activeRequest,
            merchantUpiId = session.upiId,
            isProcessing = isProcessingLedgerCredit,
            onApprove = {
                scope.launch {
                    isProcessingLedgerCredit = true
                    val reqId = activeRequest?.id ?: currentRequestId ?: return@launch
                    val res = viewModel.confirmAndCreditQrPayment(reqId)
                    isProcessingLedgerCredit = false
                    if (res.isSuccess) {
                        viewModel.clearActivePaymentRequest()
                        onDismiss()
                        Toast.makeText(context, "Payment Approved & Ledger Credited!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to credit ledger", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onReject = {
                scope.launch {
                    val reqId = activeRequest?.id ?: currentRequestId ?: return@launch
                    viewModel.rejectQrPayment(reqId)
                    Toast.makeText(context, "Payment marked as rejected/unverified", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val view = LocalView.current
    DisposableEffect(view) {
        var context = view.context
        var activity: Activity? = null
        while (context is ContextWrapper) {
            if (context is Activity) {
                activity = context
                break
            }
            context = context.baseContext
        }
        val dialogWindow = (view as? DialogWindowProvider)?.window
            ?: (view.parent as? DialogWindowProvider)?.window
            ?: activity?.window

        val insetsController = dialogWindow?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val prevLightStatusBars = insetsController?.isAppearanceLightStatusBars

        insetsController?.isAppearanceLightStatusBars = false

        onDispose {
            if (insetsController != null && prevLightStatusBars != null) {
                insetsController.isAppearanceLightStatusBars = prevLightStatusBars
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {

        },
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Collect Payment",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Generate UPI QR to collect instantly",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            currentRequestId?.let { viewModel.cancelPaymentRequest(it) }
                            viewModel.clearActivePaymentRequest()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PageBackground),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CustomerAvatar(
                            name = customer.name,
                            photoUrl = customer.photoURL,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = customer.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = customer.phone.ifBlank { "No phone registered" },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (outstandingDue > 0) GiveRed.copy(alpha = 0.12f) else GotGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (outstandingDue > 0) "Due: ${CurrencyFormatter.formatInr(outstandingDue)}" else "Settled",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (outstandingDue > 0) GiveRed else GotGreen
                        )
                    }
                }
            }

            if (session.upiId.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UPI ID Required",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set your merchant VPA/UPI ID (e.g. mobile@upi or name@okaxis) to generate dynamic QR codes and collect payments.",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editUpiId,
                                onValueChange = { editUpiId = it },
                                placeholder = { Text("e.g. 9876543210@upi", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = BorderLight
                                )
                            )
                            Button(
                                onClick = {
                                    if (editUpiId.isNotBlank() && editUpiId.contains("@")) {
                                        isSavingUpi = true
                                        viewModel.updateMerchantUpiId(editUpiId.trim())
                                        isSavingUpi = false
                                        Toast.makeText(context, "UPI ID saved successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter a valid UPI ID (e.g. name@bank)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                enabled = !isSavingUpi && editUpiId.isNotBlank()
                            ) {
                                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = activeRequest?.status ?: QrPaymentStatus.WAITING_FOR_PAYMENT,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "payment_status_banner"
            ) { status ->
                when (status) {
                    QrPaymentStatus.PAYMENT_DETECTED, QrPaymentStatus.VERIFICATION_REQUIRED -> {
                        Box(modifier = Modifier.height(0.dp))
                    }

                    QrPaymentStatus.LEDGER_CREDITED -> {
                        Box(modifier = Modifier.height(0.dp))
                    }

                    QrPaymentStatus.PAYMENT_REJECTED -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, GiveRed)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Close, contentDescription = null, tint = GiveRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payment marked as not received",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GiveRed
                                    )
                                }
                                TextButton(onClick = { triggerQrGeneration(effectiveAmount) }) {
                                    Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                }
                            }
                        }
                    }

                    else -> {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = BrandBlue,
                                    strokeWidth = 1.5.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Waiting for customer payment…",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = "Manual Verify",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue,
                                modifier = Modifier.clickable { showManualVerifyDialog = true }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.size(230.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Payment QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (isGeneratingQr) {
                            CircularProgressIndicator(color = BrandBlue)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(52.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (session.upiId.isBlank()) "Enter UPI ID above" else "Enter valid amount",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = session.businessName.ifBlank { session.displayName.ifBlank { "HisabKhata Merchant" } },
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "UPI: ${session.upiId.ifBlank { "Not Set" }}",
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )
                    if (session.upiId.isNotBlank()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy UPI",
                            tint = BrandBlue,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Merchant UPI", session.upiId))
                                    Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Amount: ${CurrencyFormatter.formatInr(effectiveAmount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = GotGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        val rawBmp = qrBitmap ?: return@OutlinedButton
                        scope.launch(Dispatchers.IO) {
                            try {
                                val posterBmp = PaymentQrPosterGenerator.generatePoster(
                                    qrBitmap = rawBmp,
                                    amount = effectiveAmount,
                                    customerName = customer.name,
                                    businessName = session.businessName.ifBlank { session.displayName }
                                )
                                val cacheDir = File(context.cacheDir, "qr")
                                cacheDir.mkdirs()
                                val file = File(cacheDir, "qr_${System.currentTimeMillis()}.png")
                                FileOutputStream(file).use { out ->
                                    posterBmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                                }
                                val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    putExtra(Intent.EXTRA_TEXT, "Pay ${CurrencyFormatter.formatInr(effectiveAmount)} to ${session.businessName.ifBlank { session.displayName }} via UPI: ${session.upiId}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                withContext(Dispatchers.Main) {
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Payment QR"))
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to share QR", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    enabled = qrBitmap != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Share",
                            color = BrandBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val rawBmp = qrBitmap ?: return@OutlinedButton
                        scope.launch(Dispatchers.IO) {
                            val posterBmp = PaymentQrPosterGenerator.generatePoster(
                                qrBitmap = rawBmp,
                                amount = effectiveAmount,
                                customerName = customer.name,
                                businessName = session.businessName.ifBlank { session.displayName }
                            )
                            val uri = saveQrToGallery(context, posterBmp, customer.name, effectiveAmount)
                            withContext(Dispatchers.Main) {
                                if (uri != null) {
                                    Toast.makeText(context, "QR saved to Pictures/HisabKhata", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save QR image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    enabled = qrBitmap != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            color = BrandBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                OutlinedButton(
                    onClick = { triggerQrGeneration(effectiveAmount) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    enabled = effectiveAmount > 0
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refresh",
                            color = BrandBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Select Payment Amount",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val dueBorderColor by animateColorAsState(
                    targetValue = if (selectedMode == QrAmountMode.OUTSTANDING_BALANCE) BrandBlue else BorderLight,
                    animationSpec = tween(250),
                    label = "due_border"
                )
                val customBorderColor by animateColorAsState(
                    targetValue = if (selectedMode == QrAmountMode.CUSTOM_AMOUNT) BrandBlue else BorderLight,
                    animationSpec = tween(250),
                    label = "custom_border"
                )
                val dueBgColor by animateColorAsState(
                    targetValue = if (selectedMode == QrAmountMode.OUTSTANDING_BALANCE) {
                        if (isAppDark) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFFEFF6FF)
                    } else SurfaceWhite,
                    animationSpec = tween(250),
                    label = "due_bg"
                )
                val customBgColor by animateColorAsState(
                    targetValue = if (selectedMode == QrAmountMode.CUSTOM_AMOUNT) {
                        if (isAppDark) Color(0xFF1E3A8A).copy(alpha = 0.3f) else Color(0xFFEFF6FF)
                    } else SurfaceWhite,
                    animationSpec = tween(250),
                    label = "custom_bg"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = outstandingDue > 0) {
                                selectedMode = QrAmountMode.OUTSTANDING_BALANCE
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = dueBgColor),
                        border = BorderStroke(
                            width = if (selectedMode == QrAmountMode.OUTSTANDING_BALANCE) 1.5.dp else 1.dp,
                            color = dueBorderColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Due Balance",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedMode == QrAmountMode.OUTSTANDING_BALANCE) BrandBlue else TextSecondary
                                )
                                AnimatedVisibility(
                                    visible = selectedMode == QrAmountMode.OUTSTANDING_BALANCE,
                                    enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                                    exit = scaleOut(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(BrandBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.formatInr(outstandingDue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (outstandingDue > 0) TextPrimary else TextTertiary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedMode = QrAmountMode.CUSTOM_AMOUNT
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = customBgColor),
                        border = BorderStroke(
                            width = if (selectedMode == QrAmountMode.CUSTOM_AMOUNT) 1.5.dp else 1.dp,
                            color = customBorderColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Amount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedMode == QrAmountMode.CUSTOM_AMOUNT) BrandBlue else TextSecondary
                                )
                                AnimatedVisibility(
                                    visible = selectedMode == QrAmountMode.CUSTOM_AMOUNT,
                                    enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                                    exit = scaleOut(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(BrandBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (customAmountText.isNotBlank()) "₹$customAmountText" else "Enter Amount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (customAmountText.isNotBlank()) TextPrimary else TextTertiary
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedMode == QrAmountMode.CUSTOM_AMOUNT,
                    enter = expandVertically(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioLowBouncy
                        )
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PageBackground),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enter Amount to Collect",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                                if (customAmountText.isNotEmpty()) {
                                    Text(
                                        text = "Clear",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GiveRed,
                                        modifier = Modifier.clickable { customAmountText = "" }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customAmountText,
                                onValueChange = { text ->
                                    if (text.isEmpty() || text.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                        customAmountText = text
                                    }
                                },
                                placeholder = { Text("0.00", fontSize = 16.sp, color = TextTertiary) },
                                leadingIcon = {
                                    Text(
                                        "₹",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlue,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (customAmountText.isNotEmpty()) {
                                        IconButton(
                                            onClick = { customAmountText = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Clear",
                                                tint = TextTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlue,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(100, 500, 1000, 2000).forEach { delta ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                                            .background(SurfaceWhite)
                                            .clickable {
                                                val current = customAmountText.toDoubleOrNull() ?: 0.0
                                                val next = current + delta
                                                customAmountText = String.format(Locale.US, "%.0f", next)
                                            }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+₹$delta",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isNotificationPermissionGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Automatic Payment Detection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enable payment notification monitoring to detect incoming payments from GPay, PhonePe, and Paytm, matching them with this QR code.",
                            fontSize = 11.5.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = GotGreen, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% private, on-device only",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = {
                                    NotificationListenerPermissionHelper.openNotificationListenerSettings(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Enable Access", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showManualVerifyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showManualVerifyDialog = false },
            title = {
                Text("Confirm Payment Manually", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Verify in your bank/UPI application that ₹${CurrencyFormatter.formatInrClean(effectiveAmount)} has been credited from ${customer.name}.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customUtrInput,
                        onValueChange = { customUtrInput = it },
                        label = { Text("Bank UTR / Ref (Optional)") },
                        placeholder = { Text("e.g. 123456789012") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualVerifyDialog = false
                        scope.launch {
                            isProcessingLedgerCredit = true
                            val reqId = currentRequestId ?: return@launch
                            val res = viewModel.confirmAndCreditQrPayment(reqId, customUtrInput.ifBlank { null })
                            isProcessingLedgerCredit = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Ledger Credited Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GotGreen)
                ) {
                    Text("Confirm & Credit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualVerifyDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

private fun saveQrToGallery(context: Context, bitmap: Bitmap, customerName: String, amount: Double): Uri? {
    val cleanName = customerName.replace(Regex("""[^a-zA-Z0-9]"""), "_")
    val filename = "HisabKhata_QR_${cleanName}_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HisabKhata")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
    try {
        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        return uri
    } catch (_: Exception) {
        return null
    }
}

@Composable
fun FullScreenPaymentDetectedPreview(
    customer: CustomerEntity,
    request: QrPaymentRequest?,
    merchantUpiId: String,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val detected = request?.detectedPayment
    val amount = detected?.amount ?: request?.amount ?: 0.0
    val source = detected?.sourceApp?.ifBlank { "BHIM UPI" } ?: "BHIM UPI"
    val utr = detected?.transactionRef?.ifBlank { "N/A" } ?: (request?.manualUtr?.ifBlank { "N/A" } ?: "N/A")

    val bankName = detected?.bankName?.ifBlank { null }
    val accountNum = detected?.accountNumber?.ifBlank { null }
    val bankDisplay = when {
        bankName != null && accountNum != null -> "$bankName ($accountNum)"
        bankName != null -> bankName
        accountNum != null -> "Bank Account ($accountNum)"
        else -> "State Bank of India (XX7137)"
    }

    val rawUpi = when {
        !detected?.upiId.isNullOrBlank() -> detected.upiId!!
        merchantUpiId.isNotBlank() -> merchantUpiId
        else -> "4jio@axl"
    }
    val upiIdDisplay = formatMaskedUpiId(rawUpi)

    val rawSender = detected?.senderName?.trim()?.ifBlank { null }
        ?: parseSenderFromNotification(detected?.rawTitle.orEmpty(), detected?.rawText.orEmpty())
    val senderNameDisplay = rawSender ?: customer.name.ifBlank { "N/A" }

    val timeFormatted = remember(detected?.timestamp) {
        val ts = detected?.timestamp ?: System.currentTimeMillis()
        SimpleDateFormat("dd-MMM-yyyy, hh:mm a", Locale.ENGLISH).format(Date(ts))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val checkScale = remember { Animatable(0.2f) }
    LaunchedEffect(Unit) {
        checkScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            var parent = view.parent
            var dialogWindow: android.view.Window? = (view as? DialogWindowProvider)?.window
            while (dialogWindow == null && parent != null) {
                if (parent is DialogWindowProvider) {
                    dialogWindow = parent.window
                    break
                }
                parent = parent.parent
            }
            dialogWindow?.let { win ->
                win.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                WindowCompat.setDecorFitsSystemWindows(win, false)
            }
            onDispose {}
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PageBackground
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReject,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BorderLight.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .scale(pulseScale)
                                .alpha(pulseAlpha)
                                .clip(CircleShape)
                                .background(GotGreen.copy(alpha = 0.35f))
                        )
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(GotGreen.copy(alpha = 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(checkScale.value)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GotGreen, Color(0xFF15803D))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Payment Successfully Detected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Incoming UPI payment matched to customer request",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Amount",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = CurrencyFormatter.formatInr(amount),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GotGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderLight)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            PaymentDetailRow(label = "Amount:", value = "₹${String.format(Locale.US, "%.2f", amount)}")
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "Source:", value = source)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "Ref / UTR:", value = utr)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "Bank:", value = bankDisplay)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "UPI ID:", value = upiIdDisplay)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "Sender Name:", value = senderNameDisplay)
                            Spacer(modifier = Modifier.height(8.dp))
                            PaymentDetailRow(label = "Date & Time:", value = timeFormatted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verify in your bank or UPI app that money is credited before approving into ledger.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, BorderLight),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFF1F2),
                                contentColor = Color(0xFFDC2626)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Reject",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reject",
                                color = Color(0xFFDC2626),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Button(
                            onClick = onApprove,
                            modifier = Modifier
                                .weight(1.6f)
                                .height(52.dp)
                                .shadow(8.dp, shape = RoundedCornerShape(16.dp), spotColor = GotGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !isProcessing
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF16A34A), Color(0xFF15803D))
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.22f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Approve",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Approve",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.3.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

private fun parseSenderFromNotification(title: String, text: String): String? {
    val fullText = "$title $text"
    val fromPattern = java.util.regex.Pattern.compile(
        """(?:from|by|transfer\s+from)\s+([A-Za-z0-9\s.]+?)(?:\s+(?:via|into|to|using|on|Ref|UTR|UPI|\(|\.)|$)""",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    val fromMatcher = fromPattern.matcher(fullText)
    if (fromMatcher.find()) {
        val s = fromMatcher.group(1)?.trim()?.trimEnd('.', ',', '!', ':', '-')
        if (!s.isNullOrBlank() && s.length in 2..50 && !s.contains("account", ignoreCase = true)) {
            return s
        }
    }
    val sentPattern = java.util.regex.Pattern.compile(
        """^([A-Za-z0-9\s.]+?)\s+(?:sent you|has paid you|paid you|transferred)""",
        java.util.regex.Pattern.CASE_INSENSITIVE
    )
    val sentMatcher = sentPattern.matcher(title)
    if (sentMatcher.find()) {
        val s = sentMatcher.group(1)?.trim()?.trimEnd('.', ',', '!', ':', '-')
        if (!s.isNullOrBlank() && s.length in 2..50) {
            return s
        }
    }
    return null
}

private fun formatMaskedUpiId(rawUpi: String): String {
    val clean = rawUpi.trim()
    if (clean.isBlank()) return "XXXX4jio@axl"
    if (!clean.contains("@")) {
        return if (clean.startsWith("XXXX", ignoreCase = true)) clean else "XXXX$clean"
    }
    val parts = clean.split("@", limit = 2)
    val handle = parts[0]
    val provider = parts[1]

    return if (handle.startsWith("XXXX", ignoreCase = true)) {
        "$handle@$provider"
    } else if (handle.startsWith("XX", ignoreCase = true)) {
        "XX$handle@$provider"
    } else {
        "XXXX$handle@$provider"
    }
}

@Composable
private fun PaymentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 13.5.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
