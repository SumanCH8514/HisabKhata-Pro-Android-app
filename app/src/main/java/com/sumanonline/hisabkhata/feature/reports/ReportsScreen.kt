package com.sumanonline.hisabkhata.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.AppTopBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onTabSelected: (BottomTab) -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.filteredTransactions.collectAsState()
    val totalDebit by viewModel.totalDebit.collectAsState()
    val totalCredit by viewModel.totalCredit.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val customerNameMap = customers.associate { it.id to it.name }

    val netBalance = totalCredit - totalDebit

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reports & Statements",
                subtitle = "Download GST-ready statements"
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.REPORTS,
                onTabSelected = onTabSelected
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.THIS_MONTH,
                    onClick = { viewModel.setPeriod(ReportPeriod.THIS_MONTH) },
                    label = { Text("This Month") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = SurfaceWhite
                    )
                )
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.LAST_MONTH,
                    onClick = { viewModel.setPeriod(ReportPeriod.LAST_MONTH) },
                    label = { Text("Last Month") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = SurfaceWhite
                    )
                )
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.LAST_7_DAYS,
                    onClick = { viewModel.setPeriod(ReportPeriod.LAST_7_DAYS) },
                    label = { Text("Last 7 Days") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = SurfaceWhite
                    )
                )
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.ALL,
                    onClick = { viewModel.setPeriod(ReportPeriod.ALL) },
                    label = { Text("All Time") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandBlue,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "TOTAL DEBIT (-)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = CurrencyFormatter.formatInr(totalDebit), fontSize = 16.sp, fontWeight = FontWeight.Black, color = GiveRed)
                        }
                        Column {
                            Text(text = "TOTAL CREDIT (+)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = CurrencyFormatter.formatInr(totalCredit), fontSize = 16.sp, fontWeight = FontWeight.Black, color = GotGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "NET BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${CurrencyFormatter.formatInr(Math.abs(netBalance))} ${if (netBalance < 0) "Dr" else "Cr"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (netBalance < 0) GiveRed else GotGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sharePdf(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Statement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.shareCsv(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Excel / CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                EmptyStateView(
                    title = "No entries in this period",
                    message = "Transactions recorded for this date range will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        val isDebit = tx.amount < 0 || tx.type == "GAVE"
                        val party = customerNameMap[tx.customerId] ?: "Party"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = party, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${DateUtils.formatDisplayDate(tx.timestamp)} • ${tx.description.ifBlank { if (isDebit) "Credit" else "Payment" }}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.formatInr(Math.abs(tx.amount)),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isDebit) GiveRed else GotGreen
                                )
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
