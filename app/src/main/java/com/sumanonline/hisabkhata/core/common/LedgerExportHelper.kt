package com.sumanonline.hisabkhata.core.common

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sumanonline.hisabkhata.core.database.dao.CustomerDao
import com.sumanonline.hisabkhata.core.database.dao.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LedgerExportHelper {

    suspend fun exportAllLedgerCsv(
        context: Context,
        userId: String,
        merchantName: String,
        merchantPhone: String,
        customerDao: CustomerDao,
        transactionDao: TransactionDao
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val customers = customerDao.getCustomersByUser(userId)
            val transactions = transactionDao.getAllTransactionsByUser(userId)
            val customerMap = customers.associateBy { it.id }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val readableDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            val sb = StringBuilder()
            // Header Info
            sb.appendLine("\"HISABKHATA PRO - COMPLETE LEDGER DATA EXPORT\"")
            sb.appendLine("\"Export Generated On:\",\"$readableDate\"")
            sb.appendLine("\"Business / User:\",\"$merchantName\",\"$merchantPhone\"")
            sb.appendLine("\"Total Parties:\",\"${customers.size}\"")
            sb.appendLine("\"Total Transactions:\",\"${transactions.size}\"")
            sb.appendLine()

            // 1. Transactions Sheet
            sb.appendLine("\"=== ALL TRANSACTIONS LOG ===\"")
            sb.appendLine("\"Date\",\"Party / Customer Name\",\"Phone\",\"Transaction Type\",\"Debit (You Gave)\",\"Credit (You Got)\",\"Net Balance\",\"Description / Note\",\"Bill No / Ref\"")

            transactions.forEach { tx ->
                val customer = customerMap[tx.customerId]
                val partyName = customer?.name ?: "Unknown Party"
                val partyPhone = customer?.phone ?: ""
                val isGave = tx.type == Constants.TX_TYPE_GAVE || tx.amount < 0
                val debitStr = if (isGave) String.format(Locale.US, "%.2f", Math.abs(tx.amount)) else ""
                val creditStr = if (!isGave) String.format(Locale.US, "%.2f", Math.abs(tx.amount)) else ""
                val txTypeLabel = if (isGave) "YOU GAVE" else "YOU GOT"
                val balanceStr = String.format(Locale.US, "%.2f", tx.balance)
                val cleanDesc = tx.description.replace("\"", "\"\"")
                val clientRef = tx.clientTxId.replace("\"", "\"\"")
                val displayDate = if (tx.timestamp > 0) DateUtils.formatDisplayDate(tx.timestamp) else tx.date

                sb.appendLine("\"$displayDate\",\"$partyName\",\"$partyPhone\",\"$txTypeLabel\",\"$debitStr\",\"$creditStr\",\"$balanceStr\",\"$cleanDesc\",\"$clientRef\"")
            }

            sb.appendLine()
            // 2. Customers Summary Sheet
            sb.appendLine("\"=== ALL CUSTOMERS / PARTIES SUMMARY ===\"")
            sb.appendLine("\"Party Name\",\"Phone Number\",\"Email\",\"Party Type\",\"Current Balance\",\"Due Date\",\"Last Updated\"")

            customers.forEach { c ->
                val due = c.dueDate ?: "No Due Date"
                val updatedStr = if (c.updatedAt > 0) DateUtils.formatDisplayDate(c.updatedAt) else ""
                val balStr = String.format(Locale.US, "%.2f", c.balance)
                val cType = if (c.type == Constants.PARTY_TYPE_SUPPLIER) "SUPPLIER" else "CUSTOMER"
                sb.appendLine("\"${c.name}\",\"${c.phone}\",\"${c.email}\",\"$cType\",\"$balStr\",\"$due\",\"$updatedStr\"")
            }

            // Write to reports directory in cache
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val csvFile = File(reportsDir, "HisabKhata_All_Ledger_$timestamp.csv")
            FileOutputStream(csvFile).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }

            Result.success(csvFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareExportedFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "HisabKhata Pro - All Ledger Data Export")
                putExtra(Intent.EXTRA_TEXT, "Attached is the complete HisabKhata Pro ledger CSV data export.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Download / Share Ledger CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening export: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
