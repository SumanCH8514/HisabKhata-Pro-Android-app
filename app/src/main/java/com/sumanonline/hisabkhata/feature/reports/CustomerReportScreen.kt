package com.sumanonline.hisabkhata.feature.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.components.AppTopBar
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import com.sumanonline.hisabkhata.core.designsystem.components.ReportsScreenSkeleton
import com.sumanonline.hisabkhata.core.designsystem.components.shimmerBrush
import com.sumanonline.hisabkhata.feature.customer.CustomerViewModel

@Composable
fun CustomerReportScreen(
    customerId: String,
    customerViewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customerState = customerViewModel.getCustomerById(customerId).collectAsState(initial = null)
    val customer = customerState.value
    val transactionsFlow = remember(customerId) { customerViewModel.getTransactionsForCustomer(customerId) }
    val transactions by transactionsFlow.collectAsState()
    val shimmer = shimmerBrush()

    if (customer == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
                .padding(16.dp)
        ) {
            ReportsScreenSkeleton(brush = shimmer)
        }
        return
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Account Statement",
                subtitle = customer.name,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        val pdfFile = PdfGenerator.generateStatementPdf(
                            context = context,
                            merchantName = "Merchant",
                            merchantPhone = "",
                            customerName = customer.name,
                            startDate = "All",
                            endDate = DateUtils.todayIso(),
                            transactions = transactions
                        )
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            pdfFile
                        )
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share PDF Statement"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "SHARE PDF STATEMENT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = PageBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(text = "CURRENT BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatInr(Math.abs(customer.balance)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (customer.balance < 0) GiveRed else GotGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (customer.balance < 0) "Customer owes you this amount" else if (customer.balance > 0) "Advance received from customer" else "Settled",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                EmptyStateView(
                    title = "No transactions found",
                    message = "No financial activity recorded for this customer yet."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }, contentType = { "customer_report_tx" }) { tx ->
                        val isDebit = tx.amount < 0 || tx.type == "GAVE"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateUtils.formatDisplayDate(tx.timestamp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tx.description.ifBlank { if (isDebit) "Debit (-)" else "Credit (+)" },
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyFormatter.formatInr(Math.abs(tx.amount)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isDebit) GiveRed else GotGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Bal: ${CurrencyFormatter.formatInr(Math.abs(tx.balance))}",
                                        fontSize = 11.sp,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
