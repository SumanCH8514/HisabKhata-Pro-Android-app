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
import java.util.Calendar
import javax.inject.Inject

enum class ReportPeriod {
    THIS_MONTH,
    LAST_MONTH,
    LAST_7_DAYS,
    ALL
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionDao: TransactionDao,
    private val customerDao: CustomerDao,
    private val sessionManager: SecureSessionManager
) : ViewModel() {

    val session = sessionManager.sessionState

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
        _selectedPeriod,
        _searchQuery
    ) { txList, period, query ->
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance()

        when (period) {
            ReportPeriod.THIS_MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
            }
            ReportPeriod.LAST_MONTH -> {
                startCal.add(Calendar.MONTH, -1)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0)
                startCal.set(Calendar.MINUTE, 0)
                startCal.set(Calendar.SECOND, 0)
            }
            ReportPeriod.LAST_7_DAYS -> {
                startCal.add(Calendar.DAY_OF_YEAR, -7)
            }
            ReportPeriod.ALL -> {
                startCal.timeInMillis = 0L
            }
        }

        txList.filter { tx ->
            val inDateRange = tx.timestamp >= startCal.timeInMillis
            val matchesQuery = query.isBlank() || tx.description.contains(query, ignoreCase = true)
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

    fun sharePdf(context: Context) {
        viewModelScope.launch {
            val customerNameMap = customers.value.associate { it.id to it.name }
            val currentSession = session.value
            val merchantName = currentSession.businessName.ifBlank { currentSession.displayName }
            val merchantPhone = currentSession.phone

            val pdfFile = PdfGenerator.generateStatementPdf(
                context = context,
                merchantName = merchantName,
                merchantPhone = merchantPhone,
                customerName = null,
                startDate = "Recent",
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
