package com.sumanonline.hisabkhata.feature.autoreminder

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedLight
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.GotGreenLight
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.WarningAmber
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.AppTopBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.isAppDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoReminderScreen(
    viewModel: AutoReminderViewModel,
    onTabSelected: (BottomTab) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToLedger: (String) -> Unit
) {
    val context = LocalContext.current
    val rules by viewModel.rules.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val awaitingCustomers by viewModel.awaitingCustomers.collectAsState()
    val dispatchedLogs by viewModel.dispatchedLogs.collectAsState()
    val isRunningBatch by viewModel.isRunningBatch.collectAsState()

    var showEditRulesSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Reminders",
                subtitle = "Automated customer dues & reports",
                leadingContent = {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_round),
                        contentDescription = "HisabKhata Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateBack() }
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.runBatchNow { successes, failures ->
                                if (successes > 0) {
                                    Toast.makeText(
                                        context,
                                        "Batch run complete! $successes reminder emails dispatched successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else if (failures > 0) {
                                    Toast.makeText(context, "Failed to send batch reminder emails.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No customers awaiting email reminders.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = stats.awaitingCount > 0 && !isRunningBatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BrandBlue,
                            disabledContainerColor = Color.White.copy(alpha = 0.5f),
                            disabledContentColor = BrandBlue.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        if (isRunningBatch) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = BrandBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Running…",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run Now (${stats.awaitingCount})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.MORE,
                onTabSelected = onTabSelected
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Automated Daily Dues Reminder Card
            item(key = "cron_status_card") {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AutomatedDailyDuesCard(
                        rules = rules,
                        onToggleCron = { viewModel.toggleCronActive(it) },
                        onEditRulesClick = { showEditRulesSheet = true }
                    )
                }
            }

            // 2. 4 Metrics Cards (2x2 Grid)
            item(key = "kpi_metrics_grid") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricKpiCard(
                            modifier = Modifier.weight(1f),
                            title = "DISPATCHED",
                            value = stats.dispatchedCount.toString(),
                            subtitle = "Total logs saved",
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        MetricKpiCard(
                            modifier = Modifier.weight(1f),
                            title = "REMINDED DUES",
                            value = CurrencyFormatter.formatInr(stats.totalRemindedDues),
                            valueColor = GiveRed,
                            subtitle = "Customer receivables",
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = GiveRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricKpiCard(
                            modifier = Modifier.weight(1f),
                            title = "DELIVERY RATE",
                            value = "${stats.deliveryRate}%",
                            valueColor = GotGreen,
                            subtitle = "100% delivered",
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = GotGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        MetricKpiCard(
                            modifier = Modifier.weight(1f),
                            title = "AWAITING DUE",
                            value = stats.awaitingCount.toString(),
                            valueColor = Color(0xFF9333EA),
                            subtitle = "Ready for next run",
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // 3. Segmented Tabs Switcher
            item(key = "tab_switcher") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    SegmentedTabRow(
                        selectedTab = selectedTab,
                        dispatchedCount = stats.dispatchedCount,
                        awaitingCount = stats.awaitingCount,
                        onTabSelected = { viewModel.setSelectedTab(it) }
                    )
                }
            }

            // 4. Tab Content: Awaiting Reminders
            if (selectedTab == AutoReminderTab.AWAITING_REMINDERS) {
                item(key = "awaiting_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Group,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customers with Pending Dues (${awaitingCustomers.size})",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderLight.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = null,
                                tint = GotGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Email verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                if (awaitingCustomers.isEmpty()) {
                    item(key = "empty_awaiting") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = GotGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "All Clear! No Pending Reminders",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No customers currently match the pending dues threshold.",
                                    fontSize = 12.5.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = awaitingCustomers,
                        key = { it.id },
                        contentType = { "awaiting_card" }
                    ) { customer ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            AwaitingCustomerCard(
                                customer = customer,
                                onSendRemind = {
                                    viewModel.sendSingleReminder(customer) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onViewStatement = {
                                    onNavigateToLedger(customer.id)
                                }
                            )
                        }
                    }
                }
            } else {
                // Tab Content: Mail Reports
                item(key = "reports_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MarkEmailRead,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sent Email Logs (${dispatchedLogs.size})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                items(
                    items = dispatchedLogs,
                    key = { it.id },
                    contentType = { "log_card" }
                ) { log ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DispatchedMailReportCard(
                            log = log,
                            onResend = {
                                Toast.makeText(context, "Resent statement email to ${log.customerEmail}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Edit Rules Bottom Sheet
    if (showEditRulesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var tempSchedule by remember(rules) { mutableStateOf(rules.dailyScheduleTime) }
        var tempMinDue by remember(rules) { mutableStateOf(rules.minDueAmount.toInt().toString()) }
        var tempLink by remember(rules) { mutableStateOf(rules.isStatementLinkEnabled) }
        var tempVerify by remember(rules) { mutableStateOf(rules.autoEmailVerification) }

        ModalBottomSheet(
            onDismissRequest = { showEditRulesSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Cron Reminder Rules",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = { showEditRulesSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Daily Automated Schedule",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val schedules = listOf("09:30 AM IST", "11:00 AM IST", "06:00 PM IST", "08:00 PM IST")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    schedules.forEach { s ->
                        val isSelected = tempSchedule == s
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) BrandBlue else BorderLight,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(if (isSelected) BrandBlueLight else SurfaceWhite)
                                .clickable { tempSchedule = s }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandBlue else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tempMinDue,
                    onValueChange = { tempMinDue = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Minimum Due Threshold (₹)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include Digital Statement Link",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Customer can view, download, or verify their full ledger securely online.",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = tempLink,
                        onCheckedChange = { tempLink = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto Email Verification Check",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ensures customers without a valid email are skipped gracefully during runs.",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = tempVerify,
                        onCheckedChange = { tempVerify = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        val minDue = tempMinDue.toDoubleOrNull() ?: 0.0
                        viewModel.updateRules(
                            scheduleTime = tempSchedule,
                            minDue = minDue,
                            statementLink = tempLink,
                            autoVerifyEmail = tempVerify
                        )
                        showEditRulesSheet = false
                        Toast.makeText(context, "Cron reminder rules updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AutomatedDailyDuesCard(
    rules: CronReminderRules,
    onToggleCron: (Boolean) -> Unit,
    onEditRulesClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badges & Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    val activeBg = if (rules.isCronActive) GotGreen.copy(alpha = if (isAppDark) 0.22f else 0.12f) else BorderLight.copy(alpha = 0.5f)
                    val activeColor = if (rules.isCronActive) GotGreen else TextSecondary
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (rules.isCronActive) "• CRON ACTIVE" else "• CRON PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }

                    // Schedule Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Daily at ${rules.dailyScheduleTime}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Native Switch
                Switch(
                    checked = rules.isCronActive,
                    onCheckedChange = onToggleCron,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = BorderLight
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Automated Daily Dues Reminder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Scans customers with pending balances (You'll Get) and sends digital ledger statements automatically.",
                fontSize = 12.5.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info & Edit Rules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Min Due: ₹${rules.minDueAmount.toInt()} • Statement Link: ${if (rules.isStatementLinkEnabled) "ON" else "OFF"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Text(
                    text = "Edit Rules",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                    modifier = Modifier.clickable(onClick = onEditRulesClick)
                )
            }
        }
    }
}

@Composable
private fun MetricKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color = TextPrimary,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                icon()
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = if (value.length > 8) 18.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SegmentedTabRow(
    selectedTab: AutoReminderTab,
    dispatchedCount: Int,
    awaitingCount: Int,
    onTabSelected: (AutoReminderTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Tab 1: Mail Reports
        val isTab1 = selectedTab == AutoReminderTab.MAIL_REPORTS
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isTab1) BrandBlueLight else Color.Transparent)
                .clickable { onTabSelected(AutoReminderTab.MAIL_REPORTS) }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mail Reports",
                fontSize = 13.sp,
                fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                color = if (isTab1) BrandBlue else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isTab1) BrandBlue else BorderLight.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = dispatchedCount.toString(),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTab1) Color.White else TextSecondary
                )
            }
        }

        // Tab 2: Awaiting Reminders
        val isTab2 = selectedTab == AutoReminderTab.AWAITING_REMINDERS
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isTab2) BrandBlueLight else Color.Transparent)
                .clickable { onTabSelected(AutoReminderTab.AWAITING_REMINDERS) }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Awaiting Reminders",
                fontSize = 13.sp,
                fontWeight = if (isTab2) FontWeight.Bold else FontWeight.Medium,
                color = if (isTab2) BrandBlue else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isTab2) BrandBlue else BorderLight.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = awaitingCount.toString(),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTab2) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AwaitingCustomerCard(
    customer: CustomerEntity,
    onSendRemind: () -> Unit,
    onViewStatement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Name, Due Badge & Amount Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customer.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!customer.dueDate.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarningAmber.copy(alpha = if (isAppDark) 0.22f else 0.12f))
                                    .border(0.5.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Due: ${customer.dueDate}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = customer.email.ifBlank { "No email registered" },
                        fontSize = 12.sp,
                        color = if (customer.email.isNotBlank()) TextSecondary else GiveRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatInr(Math.abs(customer.balance)),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiveRed
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "BALANCE DUE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Actions: View Statement (left) and ✈ Remind (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onViewStatement)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Statement",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                OutlinedButton(
                    onClick = onSendRemind,
                    enabled = customer.email.isNotBlank(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandBlue
                    ),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Remind",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DispatchedMailReportCard(
    log: ReminderLogItem,
    onResend: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formatted = sdf.format(Date(log.timestamp))
        val relative = DateUtils.formatRelativeTime(log.timestamp)
        "$formatted • $relative"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.customerName.ifBlank { "Customer" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = log.customerEmail,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateStr,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatInr(log.amount),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GotGreen.copy(alpha = if (isAppDark) 0.22f else 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✓ Delivered",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = GotGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResend,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.8.dp, BorderLight),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Resend",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
