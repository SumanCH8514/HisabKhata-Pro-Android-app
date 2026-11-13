package com.sumanonline.hisabkhata.feature.customer

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.common.DueStatus
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.designsystem.NavyBlueDark
import com.sumanonline.hisabkhata.core.designsystem.NavyBluePrimary
import com.sumanonline.hisabkhata.core.designsystem.PageBackground
import com.sumanonline.hisabkhata.core.designsystem.SurfaceWhite
import com.sumanonline.hisabkhata.core.designsystem.TextPrimary
import com.sumanonline.hisabkhata.core.designsystem.TextSecondary
import com.sumanonline.hisabkhata.core.designsystem.TextTertiary
import com.sumanonline.hisabkhata.core.designsystem.components.AppBottomBar
import com.sumanonline.hisabkhata.core.designsystem.components.BottomTab
import com.sumanonline.hisabkhata.core.designsystem.components.ConfirmDialog
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerAvatar
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerListItemSkeleton
import com.sumanonline.hisabkhata.core.designsystem.components.CustomerListSkeleton
import com.sumanonline.hisabkhata.core.designsystem.components.EmptyStateView
import com.sumanonline.hisabkhata.core.designsystem.components.SummaryBalanceCards
import com.sumanonline.hisabkhata.core.designsystem.components.shimmerBrush
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (String) -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    onLogoutSuccess: () -> Unit = {}
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val totalGet by viewModel.totalGet.collectAsState()
    val totalGive by viewModel.totalGive.collectAsState()
    val netBalance by viewModel.netReceivableBalance.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val pendingCount by viewModel.pendingSyncCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val context = LocalContext.current

    val partiesCount by viewModel.partiesCount.collectAsState()
    val givePartiesCount by viewModel.givePartiesCount.collectAsState()
    val getPartiesCount by viewModel.getPartiesCount.collectAsState()
    val overduePartiesCount by viewModel.overduePartiesCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val isFabExpanded by remember { derivedStateOf { !listState.isScrollInProgress } }
    val shimmer = shimmerBrush()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBluePrimary)
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            AppBottomBar(
                currentTab = BottomTab.CUSTOMERS,
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                expanded = isFabExpanded,
                icon = {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Customer",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "ADD CUSTOMER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                },
                containerColor = Color(0xFF0052CC),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 6.dp, end = 2.dp)
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item(contentType = "home_header") {
                HomeHeaderBar(
                    businessName = session.businessName.ifBlank { "M/S Suman Enterprise" },
                    isSyncing = isSyncing,
                    pendingSyncCount = pendingCount,
                    onSyncClick = {
                        viewModel.triggerSync { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onLogoutClick = { showLogoutConfirm = true },
                    onQrClick = { onTabSelected(BottomTab.PAYMENTS) }
                )
            }

            item(contentType = "summary_cards") {
                SummaryBalanceCards(
                    totalGet = totalGet,
                    totalGive = totalGive,
                    givePartiesCount = givePartiesCount,
                    getPartiesCount = getPartiesCount,
                    onGetClick = { viewModel.setFilter(CustomerFilter.YOULL_GET) },
                    onGiveClick = { viewModel.setFilter(CustomerFilter.YOULL_GIVE) }
                )
            }

            item(contentType = "receivable_banner") {
                NetReceivableBanner(
                    netBalance = netBalance,
                    onViewClick = { onTabSelected(BottomTab.REPORTS) }
                )
            }

            stickyHeader(contentType = "sticky_search_filters") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PageBackground
                ) {
                    StickySearchAndFiltersCard(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onFiltersClick = { showSortMenu = true }
                    )
                }
            }

            item(contentType = "filter_chips") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAll = currentFilter == CustomerFilter.ALL
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isAll) NavyBluePrimary else SurfaceWhite)
                                .then(if (!isAll) Modifier.border(1.dp, Color(0xFFCBD5E1), CircleShape) else Modifier)
                                .clickable { viewModel.setFilter(CustomerFilter.ALL) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "All ($partiesCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAll) Color.White else Color(0xFF334155)
                            )
                        }

                        val isGet = currentFilter == CustomerFilter.YOULL_GET
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isGet) Color(0xFFDC2626) else SurfaceWhite)
                                .then(if (!isGet) Modifier.border(1.dp, Color(0xFFCBD5E1), CircleShape) else Modifier)
                                .clickable { viewModel.setFilter(CustomerFilter.YOULL_GET) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isGet) Color.White else Color(0xFFDC2626))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You'll Get ($getPartiesCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isGet) Color.White else Color(0xFF334155)
                            )
                        }

                        val isGive = currentFilter == CustomerFilter.YOULL_GIVE
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isGive) Color(0xFF16A34A) else SurfaceWhite)
                                .then(if (!isGive) Modifier.border(1.dp, Color(0xFFCBD5E1), CircleShape) else Modifier)
                                .clickable { viewModel.setFilter(CustomerFilter.YOULL_GIVE) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isGive) Color.White else Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You'll Give ($givePartiesCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isGive) Color.White else Color(0xFF334155)
                            )
                        }

                        val isOverdue = currentFilter == CustomerFilter.OVERDUE
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isOverdue) Color(0xFFDC2626) else SurfaceWhite)
                                .then(if (!isOverdue) Modifier.border(1.dp, Color(0xFFCBD5E1), CircleShape) else Modifier)
                                .clickable { viewModel.setFilter(CustomerFilter.OVERDUE) }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isOverdue) Color.White else Color(0xFFDC2626),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Overdue ($overduePartiesCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isOverdue) Color.White else Color(0xFF334155)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.setSort(
                                    if (currentSort == CustomerSort.NAME) CustomerSort.RECENT else CustomerSort.NAME
                                )
                            }
                        ) {
                            Text(
                                text = "NAME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort by name",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.setSort(
                                    if (currentSort == CustomerSort.AMOUNT_HIGH) CustomerSort.AMOUNT_LOW else CustomerSort.AMOUNT_HIGH
                                )
                            }
                        ) {
                            Text(
                                text = "AMOUNT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sort by amount",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                }
            }

            if (isLoading) {
                items(6, contentType = { "loading_skeleton_item" }) {
                    CustomerListItemSkeleton(brush = shimmer)
                }
            } else if (customers.isEmpty()) {
                item(contentType = "empty_state") {
                    EmptyStateView(
                        title = if (searchQuery.isNotEmpty()) "No matching party found" else "No parties added yet",
                        message = if (searchQuery.isNotEmpty()) "Try searching with a different name or number" else "Add your first customer to track credits and debits easily.",
                        actionButtonText = if (searchQuery.isEmpty()) "+ Add Customer" else null,
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(customers, key = { it.id }, contentType = { "customer_item" }) { customer ->
                    CustomerListItem(
                        customer = customer,
                        onClick = { onCustomerClick(customer.id) }
                    )
                }
            }

            item(contentType = "bottom_spacer") {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    if (showSortMenu) {
        FilterSortBottomSheet(
            currentSort = currentSort,
            currentFilter = currentFilter,
            totalCount = partiesCount,
            getCount = getPartiesCount,
            giveCount = givePartiesCount,
            overdueCount = overduePartiesCount,
            onSortSelected = { sort ->
                viewModel.setSort(sort)
            },
            onFilterSelected = { filter ->
                viewModel.setFilter(filter)
            },
            onReset = {
                viewModel.setSort(CustomerSort.RECENT)
                viewModel.setFilter(CustomerFilter.ALL)
            },
            onDismiss = { showSortMenu = false }
        )
    }

    if (showAddDialog) {
        AddEditCustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, email, openingBalance, balanceType, type ->
                viewModel.addCustomer(
                    name = name,
                    phone = phone,
                    email = email,
                    openingBalance = openingBalance,
                    balanceType = balanceType,
                    type = type
                )
                showAddDialog = false
            }
        )
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = "Log Out?",
            message = "Are you sure you want to log out of your merchant account? Your offline entries remain securely saved.",
            confirmText = "Log Out",
            isDestructive = true,
            onConfirm = {
                showLogoutConfirm = false
                viewModel.logout()
                onLogoutSuccess()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

/**
 * Sticky Search & Filters Card matching the uploaded image exactly:
 * [ Search Icon | Inset Search Customer box | Vertical Divider | Filters funnel + FILTERS text ]
 */
@Composable
private fun StickySearchAndFiltersCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFiltersClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search Customer by nam…",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier
                .height(44.dp)
                .clickable(onClick = onFiltersClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = NavyBluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FILTERS",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBluePrimary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun HomeHeaderBar(
    businessName: String,
    isSyncing: Boolean,
    pendingSyncCount: Int,
    onSyncClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onQrClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyBluePrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "HisabKhata",
                        tint = NavyBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HisabKhata",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDC2626))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        text = if (isSyncing) "Syncing data…" else if (pendingSyncCount > 0) "$pendingSyncCount offline entries" else "a SumanOnline Project",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
                val syncRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "syncAngle"
                )

                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                rotationZ = if (isSyncing) syncRotation else 0f
                            }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavyBlueDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = businessName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier
                    .clickable(onClick = onQrClick)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR Code",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "QR Code",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NetReceivableBanner(
    netBalance: BigDecimal,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onViewClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = NavyBluePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Net Receivable Balance: ",
                fontSize = 12.sp,
                color = Color(0xFF475569)
            )
            Text(
                text = CurrencyFormatter.formatInr(netBalance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBluePrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "VIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBluePrimary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = NavyBluePrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerListItem(
    customer: CustomerEntity,
    onClick: () -> Unit
) {
    val isGet = customer.balance < 0.0
    val isGive = customer.balance > 0.0
    val isSettled = customer.balance == 0.0

    val dueStatus = DateUtils.getDueDateStatus(customer.dueDate)
    val isOverdue = dueStatus is DueStatus.Overdue

    val avatarBg = when {
        isGet -> Color(0xFFFEE2E2)
        isGive -> Color(0xFFDCFCE7)
        else -> Color(0xFFF1F5F9)
    }
    val avatarTextColor = when {
        isGet -> Color(0xFFDC2626)
        isGive -> Color(0xFF16A34A)
        else -> Color(0xFF64748B)
    }

    val showActive = customer.name.contains("Supratik", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerAvatar(
                name = customer.name,
                photoUrl = customer.photoURL,
                size = 46.dp,
                backgroundColor = avatarBg,
                textColor = avatarTextColor,
                fontSize = 19.sp,
                showActiveBadge = showActive
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateUtils.formatRelativeTime(customer.updatedAt).uppercase(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )

                    if (isOverdue && isGet) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(0.5.dp, Color(0xFFFECACA), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color(0xFFDC2626), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(3.5.dp))
                            Text(
                                text = "${dueStatus.days}D OVERDUE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val amountText = when {
                    isSettled -> "₹0"
                    isGet -> CurrencyFormatter.formatInr(Math.abs(customer.balance)) + " ↘"
                    isGive -> CurrencyFormatter.formatInr(Math.abs(customer.balance)) + " ↗"
                    else -> "₹0"
                }
                val amountColor = when {
                    isGet -> Color(0xFFDC2626)
                    isGive -> Color(0xFF16A34A)
                    else -> Color(0xFF64748B)
                }

                Text(
                    text = amountText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                val badgeBg = if (isGet) Color(0xFFFEE2E2) else if (isGive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                val badgeText = if (isGet) Color(0xFFDC2626) else if (isGive) Color(0xFF16A34A) else Color(0xFF64748B)
                val badgeLabel = if (isGet) "YOU'LL GET" else if (isGive) "YOU'LL GIVE" else "SETTLED"

                if (!isSettled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
    }
}
