package com.sumanonline.hisabkhata.feature.customer

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.ImageUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedLight
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.GotGreenLight
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: TransactionEntity,
    customerName: String,
    onDismiss: () -> Unit,
    onDelete: (txId: String, amount: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFullAttachment by remember { mutableStateOf(false) }

    val isGave = transaction.amount < 0 || transaction.type == "GAVE"
    val activeColor = if (isGave) GiveRed else GotGreen
    val headerBackground = if (isGave) GiveRedLight else GotGreenLight
    val typeLabel = if (isGave) "You Gave (Credit)" else "You Got (Payment)"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(headerBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = typeLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = activeColor)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = CurrencyFormatter.formatInr(Math.abs(transaction.amount)),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = activeColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = customerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date & Time", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = DateUtils.formatDisplayDateTime(transaction.timestamp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Running Balance", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = CurrencyFormatter.formatInr(Math.abs(transaction.balance)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.balance < 0) GiveRed else GotGreen
                )
            }

            if (transaction.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Note / Details", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        text = transaction.description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Sync Status", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = transaction.syncStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.syncStatus == "SYNCED") GotGreen else GiveRed
                )
            }

            if (!transaction.attachment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Attachment / Bill Receipt",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val imageModel = remember(transaction.attachment) {
                    ImageUtils.sanitizeImageSource(transaction.attachment)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BorderLight.copy(alpha = 0.3f))
                        .clickable { showFullAttachment = true },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Transaction Bill Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(TextPrimary.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Tap to expand",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SurfaceWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GiveRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = GiveRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Delete Entry", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showFullAttachment && !transaction.attachment.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showFullAttachment = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                val imageModel = remember(transaction.attachment) {
                    ImageUtils.sanitizeImageSource(transaction.attachment)
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
                IconButton(
                    onClick = { showFullAttachment = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close Fullscreen",
                        tint = SurfaceWhite
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete this transaction of ₹${Math.abs(transaction.amount)}? The customer balance will be recalculated accordingly.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDelete(transaction.id, transaction.amount)
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
