package com.sumanonline.hisabkhata.feature.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import com.sumanonline.hisabkhata.core.database.entity.CustomerEntity
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import com.sumanonline.hisabkhata.core.security.SecureSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class ReportPeriod(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_7_DAYS("Last 7 Days"),
    CUSTOM("Custom Range")
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val customerDao: CustomerDao,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    val session = sessionManager.sessionState

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.ALL)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                customerDao.observeCustomersByUser(s.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTransactions = sessionManager.sessionState
        .flatMapLatest { s ->
            if (s.userId.isNotBlank()) {
                transactionDao.observeAllTransactionsByUser(s.userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        customers,
        _selectedPeriod,
        _searchQuery,
        _customStartDate,
        _customEndDate
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val txList = args[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val custList = args[1] as List<CustomerEntity>
        val period = args[2] as ReportPeriod
        val query = (args[3] as String).trim()
        val customStart = args[4] as Long?
        val customEnd = args[5] as Long?

        val (startMillis, endMillis) = when (period) {
            ReportPeriod.THIS_MONTH -> {
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                Pair(startCal.timeInMillis, endCal.timeInMillis)
            }
            ReportPeriod.LAST_MONTH -> {
                val startCal = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MILLISECOND, -1)
                }
                Pair(startCal.timeInMillis, endCal.timeInMillis)
            }
            ReportPeriod.LAST_7_DAYS -> {
                val startCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                Pair(startCal.timeInMillis, endCal.timeInMillis)
            }
            ReportPeriod.ALL -> {
                Pair(0L, Long.MAX_VALUE)
            }
            ReportPeriod.CUSTOM -> {
                val s = customStart ?: 0L
                val e = customEnd ?: Long.MAX_VALUE
                Pair(s, e)
            }
        }

        val customerNameMap = custList.associate { it.id to it.name }

        txList.filter { tx ->
            val inDateRange = tx.timestamp in startMillis..endMillis
            val partyName = customerNameMap[tx.customerId].orEmpty()
            val matchesQuery = query.isBlank() ||
                partyName.contains(query, ignoreCase = true) ||
                tx.description.contains(query, ignoreCase = true) ||
                CurrencyFormatter.formatInrClean(Math.abs(tx.amount), false).contains(query)
            inDateRange && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDebit: StateFlow<Double> = filteredTransactions.combine(_selectedPeriod) { list, _ ->
        list.filter { it.amount < 0 || it.type == "GAVE" }.sumOf { Math.abs(it.amount) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCredit: StateFlow<Double> = filteredTransactions.combine(_selectedPeriod) { list, _ ->
        list.filter { it.amount > 0 || it.type == "GOT" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCustomDateRange(start: Long?, end: Long?) {
        _customStartDate.value = start
        _customEndDate.value = end
    }

    fun clearCustomDates() {
        _customStartDate.value = null
        _customEndDate.value = null
    }

    fun sharePdf(context: Context) {
        viewModelScope.launch {
            val customerNameMap = customers.value.associate { it.id to it.name }
            val currentSession = session.value
            val merchantName = currentSession.businessName.ifBlank { currentSession.displayName }
            val merchantPhone = currentSession.phone

            val periodLabel = when (selectedPeriod.value) {
                ReportPeriod.THIS_MONTH -> "This Month"
                ReportPeriod.LAST_MONTH -> "Last Month"
                ReportPeriod.LAST_7_DAYS -> "Last 7 Days"
                ReportPeriod.ALL -> "All Time"
                ReportPeriod.CUSTOM -> {
                    val s = _customStartDate.value
                    val e = _customEndDate.value
                    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val sStr = if (s != null) fmt.format(s) else "Start"
                    val eStr = if (e != null) fmt.format(e) else DateUtils.todayIso()
                    "$sStr to $eStr"
                }
            }

            val pdfFile = PdfGenerator.generateStatementPdf(
                context = context,
                merchantName = merchantName,
                merchantPhone = merchantPhone,
                customerName = null,
                startDate = periodLabel,
                endDate = DateUtils.todayIso(),
                transactions = filteredTransactions.value,
                customerNameMap = customerNameMap
            )

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
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Statement"))
        }
    }

    fun shareCsv(context: Context) {
        viewModelScope.launch {
            val customerNameMap = customers.value.associate { it.id to it.name }
            val sb = java.lang.StringBuilder()
            sb.append("Date,Party Name,Details,Debit (-),Credit (+),Running Balance\n")

            filteredTransactions.value.forEach { tx ->
                val party = customerNameMap[tx.customerId] ?: "Party"
                val debit = if (tx.amount < 0 || tx.type == "GAVE") Math.abs(tx.amount).toString() else ""
                val credit = if (tx.amount > 0 || tx.type == "GOT") tx.amount.toString() else ""
                sb.append("\"${DateUtils.formatDisplayDate(tx.timestamp)}\",\"$party\",\"${tx.description}\",\"$debit\",\"$credit\",\"${Math.abs(tx.balance)}\"\n")
            }

            val file = File(context.cacheDir, "statement_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share CSV Statement"))
        }
    }
}
