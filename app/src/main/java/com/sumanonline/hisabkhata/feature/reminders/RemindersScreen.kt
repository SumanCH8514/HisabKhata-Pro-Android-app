package com.sumanonline.hisabkhata.feature.reminders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.DueStatus
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
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView

@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onTabSelected: (BottomTab) -> Unit,
    onCustomerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val dueCustomers by viewModel.dueCustomers.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Reminders",
                subtitle = "${dueCustomers.size} Pending Receivables"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (dueCustomers.isEmpty()) {
                EmptyStateView(
                    title = "All clear! No pending dues",
                    message = "None of your customers have outstanding dues at this time."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dueCustomers, key = { it.id }, contentType = { "reminder_customer" }) { customer ->
                        val dueStatus = DateUtils.getDueDateStatus(customer.dueDate)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
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
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GiveRed.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                if (dueStatus !is DueStatus.None) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    when (dueStatus) {
                                        is DueStatus.DueToday -> {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(WarningAmberLight)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text("Due Today (${customer.dueDate})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                                            }
                                        }
                                        is DueStatus.Overdue -> {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GiveRedLight)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text("Overdue by ${dueStatus.days} days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GiveRed)
                                            }
                                        }
                                        is DueStatus.DueSoon -> {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BrandBlueLight)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text("Due in ${dueStatus.days} days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                            }
                                        }
                                        else -> Unit
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.sendWhatsAppReminder(context, customer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GotGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WhatsApp Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (customer.email.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { viewModel.sendEmailReminder(context, customer) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(40.dp)
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                                        }
                                    }
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
