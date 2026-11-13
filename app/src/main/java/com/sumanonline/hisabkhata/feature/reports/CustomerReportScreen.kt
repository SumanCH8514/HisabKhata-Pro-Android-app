package com.sumanonline.hisabkhata.feature.reports

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import com.sumanonline.hisabkhata.core.designsystem.components.ReportsScreenSkeleton
import com.sumanonline.hisabkhata.core.designsystem.components.shimmerBrush
import com.sumanonline.hisabkhata.feature.customer.CustomerViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CustomerReportDatePreset(val displayName: String, val menuLabel: String) {
    ALL_TIME("DATE RANGE", "All Time"),
    TODAY("TODAY", "Today"),
    YESTERDAY("YESTERDAY", "Yesterday"),
    LAST_7_DAYS("LAST 7 DAYS", "Last 7 Days"),
    THIS_MONTH("THIS MONTH", "This Month"),
    LAST_MONTH("LAST MONTH", "Last Month")
}

@Composable
fun CustomerReportScreen(
    customerId: String,
    customerViewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customerState = customerViewModel.getCustomerById(customerId).collectAsState(initial = null)
    val customer = customerState.value
    val session by customerViewModel.sessionState.collectAsState()
    val transactionsFlow = remember(customerId) { customerViewModel.getTransactionsForCustomer(customerId) }
    val transactions by transactionsFlow.collectAsState()
    val shimmer = shimmerBrush()

    var searchQuery by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var activePreset by remember { mutableStateOf(CustomerReportDatePreset.ALL_TIME) }
    var showPresetMenu by remember { mutableStateOf(false) }

    val shortDateFormat = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }

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

    // Filter transactions by date range and search query
    val filteredTransactions = remember(transactions, startDate, endDate, searchQuery) {
        transactions.filter { tx ->
            val matchesDate = (startDate == null || tx.timestamp >= startDate!!.time) &&
                    (endDate == null || tx.timestamp <= endDate!!.time)
            val matchesSearch = searchQuery.isBlank() ||
                    tx.description.contains(searchQuery, ignoreCase = true) ||
                    shortDateFormat.format(Date(tx.timestamp)).contains(searchQuery, ignoreCase = true) ||
                    Math.abs(tx.amount).toString().contains(searchQuery)
            matchesDate && matchesSearch
        }
    }

    val totalGave = remember(filteredTransactions) {
        filteredTransactions.filter { it.amount < 0 || it.type == "GAVE" }.sumOf { Math.abs(it.amount) }
    }

    val totalGot = remember(filteredTransactions) {
        filteredTransactions.filter { it.amount > 0 || it.type == "GOT" }.sumOf { Math.abs(it.amount) }
    }

    fun showDatePicker(initialDate: Date?, onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            if (initialDate != null) time = initialDate
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun applyPreset(preset: CustomerReportDatePreset) {
        activePreset = preset
        when (preset) {
            CustomerReportDatePreset.ALL_TIME -> {
                startDate = null
                endDate = null
            }
            CustomerReportDatePreset.TODAY -> {
                startDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time
                endDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.time
            }
            CustomerReportDatePreset.YESTERDAY -> {
                startDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time
                endDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.time
            }
            CustomerReportDatePreset.LAST_7_DAYS -> {
                startDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time
                endDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.time
            }
            CustomerReportDatePreset.THIS_MONTH -> {
                startDate = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time
                endDate = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.time
            }
            CustomerReportDatePreset.LAST_MONTH -> {
                startDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time
                endDate = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.time
            }
        }
    }

    fun createStatementPdf(): File {
        val startDateFormatted = if (startDate != null) shortDateFormat.format(startDate!!) else "All"
        val endDateFormatted = if (endDate != null) shortDateFormat.format(endDate!!) else DateUtils.todayIso()
        return PdfGenerator.generateStatementPdf(
            context = context,
            merchantName = session.businessName.ifBlank { "Merchant" },
            merchantPhone = session.phone,
            customerName = customer.name,
            startDate = startDateFormatted,
            endDate = endDateFormatted,
            transactions = filteredTransactions
        )
    }

    fun handleDownload() {
        val pdfFile = createStatementPdf()
        savePdfToDownloads(context, pdfFile, customer.name)
        Toast.makeText(context, "Statement saved to Downloads", Toast.LENGTH_SHORT).show()
        openPdfFile(context, pdfFile)
    }

    fun handleShare() {
        val pdfFile = createStatementPdf()
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Statement PDF"))
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue)
                    .statusBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SurfaceWhite
                        )
                    }
                    Text(
                        text = "Report of ${customer.name}",
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // Date Selectors Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start Date
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    showDatePicker(startDate) { picked ->
                                        startDate = picked
                                    }
                                }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Start Date",
                                tint = BrandBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (startDate != null) shortDateFormat.format(startDate!!).uppercase() else "START DATE",
                                color = BrandBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            if (startDate != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Start Date",
                                    tint = TextTertiary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { startDate = null }
                                )
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.6f)
                                .background(BorderLight)
                        )

                        // End Date
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    showDatePicker(endDate) { picked ->
                                        val endCal = Calendar.getInstance().apply {
                                            time = picked
                                            set(Calendar.HOUR_OF_DAY, 23)
                                            set(Calendar.MINUTE, 59)
                                            set(Calendar.SECOND, 59)
                                            set(Calendar.MILLISECOND, 999)
                                        }
                                        endDate = endCal.time
                                    }
                                }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "End Date",
                                tint = BrandBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (endDate != null) shortDateFormat.format(endDate!!).uppercase() else "END DATE",
                                color = BrandBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            if (endDate != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear End Date",
                                    tint = TextTertiary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { endDate = null }
                                )
                            }
                        }
                    }
                }

                // Search & Date Range Preset Row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search icon
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // Search Input Box with Border
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(BrandBlue),
                                modifier = Modifier.fillMaxWidth(if (searchQuery.isNotEmpty()) 0.85f else 1f),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search Entries",
                                            color = TextTertiary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search",
                                    tint = TextTertiary,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(16.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Date Range Dropdown Trigger
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable { showPresetMenu = true }
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activePreset.displayName,
                                    color = BrandBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Range",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false },
                                modifier = Modifier.background(SurfaceWhite)
                            ) {
                                CustomerReportDatePreset.entries.forEach { preset ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = preset.menuLabel,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (activePreset == preset) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (activePreset == preset) BrandBlue else TextPrimary
                                                )
                                                if (activePreset == preset) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = BrandBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            applyPreset(preset)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download Button
                    OutlinedButton(
                        onClick = { handleDownload() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, BrandBlue),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(1.5.dp, BrandBlue, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PDF",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOWNLOAD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandBlue
                        )
                    }

                    // Share Button
                    Button(
                        onClick = { handleShare() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHARE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = SurfaceWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWhite)
        ) {
            // Net Balance Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Balance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = CurrencyFormatter.formatInrClean(Math.abs(customer.balance), withSpace = true),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (customer.balance < 0) GiveRed else if (customer.balance > 0) GotGreen else TextSecondary
                )
            }

            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFF1F5F9))

            // 3-Column Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${filteredTransactions.size} Entries",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOU GAVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatInrClean(totalGave, withSpace = true),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiveRed
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "YOU GOT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatInrClean(totalGot, withSpace = true),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GotGreen
                    )
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFF1F5F9))

            // Transaction List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No transactions found",
                        message = if (searchQuery.isNotBlank() || startDate != null || endDate != null) {
                            "No transactions match the selected filter or date range."
                        } else {
                            "No financial activity recorded for this customer yet."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceWhite)
                ) {
                    items(filteredTransactions, key = { it.id }, contentType = { "customer_report_tx" }) { tx ->
                        val isDebit = tx.amount < 0 || tx.type == "GAVE"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = shortDateFormat.format(Date(tx.timestamp)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Balance Pill
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Bal. ${CurrencyFormatter.formatInrClean(tx.balance, withSpace = true)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Description
                                val rawDesc = tx.description.ifBlank { if (isDebit) "You gave" else "You got" }
                                val cleanDesc = rawDesc.trimStart { it == '•' || it == ' ' }
                                Text(
                                    text = "• $cleanDesc",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right Colored Amount Box
                            Box(
                                modifier = Modifier
                                    .width(116.dp)
                                    .height(60.dp)
                                    .background(
                                        color = if (isDebit) Color(0xFFFFF5F5) else Color(0xFFF0FDF4),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = CurrencyFormatter.formatInrClean(Math.abs(tx.amount), withSpace = true),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDebit) GiveRed else GotGreen
                                )
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F5F9))
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

private fun savePdfToDownloads(context: Context, sourceFile: File, customerName: String) {
    val cleanName = customerName.replace("\\s+".toRegex(), "_")
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Statement_${cleanName}_$timeStamp.pdf"

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/HisabKhata")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { inp ->
                        inp.copyTo(out)
                    }
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val hisabDir = File(downloadsDir, "HisabKhata").apply { mkdirs() }
            val destFile = File(hisabDir, fileName)
            sourceFile.copyTo(destFile, overwrite = true)
        }
    } catch (_: Exception) {
        // Fallback to internal storage copy
    }
}

private fun openPdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(viewIntent, "Open Statement PDF"))
    } catch (_: Exception) {
        // Ignored if no viewer
    }
}
