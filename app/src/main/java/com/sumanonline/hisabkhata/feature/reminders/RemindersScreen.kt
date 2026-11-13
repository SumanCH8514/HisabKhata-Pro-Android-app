package com.sumanonline.hisabkhata.feature.reminders

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.DueStatus
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.designsystem.BorderLight
import com.sumanonline.hisabkhata.core.designsystem.BrandBlue
import com.sumanonline.hisabkhata.core.designsystem.BrandBlueLight
import com.sumanonline.hisabkhata.core.designsystem.GiveRed
import com.sumanonline.hisabkhata.core.designsystem.GiveRedLight
import com.sumanonline.hisabkhata.core.designsystem.GotGreen
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.WarningAmber
import com.sumanonline.hisabkhata.core.designsystem.WarningAmberLight
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.AppTopBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import com.sumanonline.hisabkhata.core.designsystem.isAppDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onTabSelected: (BottomTab) -> Unit,
    onCustomerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val dueCustomers by viewModel.dueCustomers.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val overdueCustomers = remember(dueCustomers) {
        dueCustomers.filter { DateUtils.getDueDateStatus(it.dueDate) is DueStatus.Overdue }
    }
    val dueTodayCustomers = remember(dueCustomers) {
        dueCustomers.filter { DateUtils.getDueDateStatus(it.dueDate) is DueStatus.DueToday }
    }
    val dueSoonCustomers = remember(dueCustomers) {
        dueCustomers.filter { DateUtils.getDueDateStatus(it.dueDate) is DueStatus.DueSoon }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Reminders",
                subtitle = "${dueCustomers.size} Pending Receivables",
                leadingContent = {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_round),
                        contentDescription = "HisabKhata Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.REMINDERS,
                onTabSelected = onTabSelected
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Sticky Filter Pills Row (pins to top on scroll)
            stickyHeader(key = "sticky_reminder_filters") {
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
                        ReminderPillChip(
                            label = "All",
                            count = dueCustomers.size,
                            isSelected = selectedFilter == ReminderFilter.ALL,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.setFilter(ReminderFilter.ALL) }
                        )
                        ReminderPillChip(
                            label = "Overdue",
                            count = overdueCustomers.size,
                            isSelected = selectedFilter == ReminderFilter.OVERDUE,
                            accentColor = GiveRed,
                            onClick = { viewModel.setFilter(ReminderFilter.OVERDUE) }
                        )
                        ReminderPillChip(
                            label = "Due Today",
                            count = dueTodayCustomers.size,
                            isSelected = selectedFilter == ReminderFilter.DUE_TODAY,
                            accentColor = WarningAmber,
                            onClick = { viewModel.setFilter(ReminderFilter.DUE_TODAY) }
                        )
                        ReminderPillChip(
                            label = "Due Soon",
                            count = dueSoonCustomers.size,
                            isSelected = selectedFilter == ReminderFilter.DUE_SOON,
                            accentColor = BrandBlue,
                            onClick = { viewModel.setFilter(ReminderFilter.DUE_SOON) }
                        )
                    }
                }
            }

            // 2. Search Bar
            item(key = "reminder_search_bar") {
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
                                text = "Search by customer name or phone...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedLeadingIconColor = Color(0xFF64748B),
                            unfocusedLeadingIconColor = Color(0xFF64748B),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Customer Reminders List
            if (filteredCustomers.isEmpty()) {
                item(key = "empty_reminders_view") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp, bottom = 40.dp)
                    ) {
                        EmptyStateView(
                            title = if (searchQuery.isNotBlank() || selectedFilter != ReminderFilter.ALL) {
                                "No matching reminders"
                            } else {
                                "All clear! No pending dues"
                            },
                            message = if (searchQuery.isNotBlank() || selectedFilter != ReminderFilter.ALL) {
                                "Try resetting search or filter to see all due customers."
                            } else {
                                "None of your customers have outstanding dues at this time."
                            }
                        )
                    }
                }
            } else {
                items(filteredCustomers, key = { it.id }, contentType = { "reminder_customer" }) { customer ->
                    ReminderCustomerCard(
                        customer = customer,
                        onCardClick = { onCustomerClick(customer.id) },
                        onSendWhatsApp = { viewModel.sendWhatsAppReminder(context, customer) },
                        onSendSms = { viewModel.sendSmsReminder(context, customer) },
                        onSendEmail = { viewModel.sendEmailReminder(context, customer) }
                    )
                }
            }

            item(key = "reminders_bottom_spacer") {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ReminderPillChip(
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
private fun ReminderCustomerCard(
    customer: CustomerEntity,
    onCardClick: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onSendSms: () -> Unit,
    onSendEmail: () -> Unit
) {
    val dueStatus = remember(customer.dueDate) {
        DateUtils.getDueDateStatus(customer.dueDate)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderLight),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = customer.phone.ifBlank { "No phone registered" },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatInr(Math.abs(customer.balance)),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = GiveRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "YOU'LL GET",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GiveRed.copy(alpha = 0.85f)
                    )
                }
            }

            if (dueStatus !is DueStatus.None) {
                Spacer(modifier = Modifier.height(10.dp))
                when (dueStatus) {
                    is DueStatus.DueToday -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarningAmberLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Due Today (${customer.dueDate})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }
                    is DueStatus.Overdue -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GiveRedLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Overdue by ${dueStatus.days} days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GiveRed
                            )
                        }
                    }
                    is DueStatus.DueSoon -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandBlueLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Due in ${dueStatus.days} days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    else -> Unit
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. WhatsApp Reminder
                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GotGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "WhatsApp Reminder",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // 2. SMS Button (between WhatsApp and Email)
                OutlinedButton(
                    onClick = onSendSms,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SMS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }

                // 3. Email Button
                OutlinedButton(
                    onClick = onSendEmail,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Email",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
