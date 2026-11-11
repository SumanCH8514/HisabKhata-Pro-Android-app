package com.sumanonline.hisabkhata.feature.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

object PdfGenerator {

    fun generateStatementPdf(
        context: Context,
        merchantName: String,
        merchantPhone: String,
        customerName: String?,
        startDate: String,
        endDate: String,
        transactions: List<TransactionEntity>,
        customerNameMap: Map<String, String> = emptyMap()
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        paint.isAntiAlias = true

        paint.color = Color.parseColor("#0057BB")
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("HisabKhata PRO", 24f, 40f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subtitle = if (customerName != null) "Customer Statement: $customerName" else "Business Transaction Statement"
        canvas.drawText(subtitle, 24f, 60f, paint)
        canvas.drawText("Period: $startDate to $endDate", 24f, 76f, paint)

        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(merchantName.uppercase(), 571f, 40f, paint)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        if (merchantPhone.isNotBlank()) {
            canvas.drawText(merchantPhone, 571f, 58f, paint)
        }
        paint.textAlign = Paint.Align.LEFT

        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(24f, 105f, 571f, 155f, 12f, 12f, paint)
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(24f, 105f, 571f, 155f, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        var totalDebit = 0.0
        var totalCredit = 0.0
        transactions.forEach {
            if (it.amount < 0 || it.type == "GAVE") {
                totalDebit += Math.abs(it.amount)
            } else {
                totalCredit += it.amount
            }
        }
        val net = totalCredit - totalDebit

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL DEBIT (-)", 36f, 122f, paint)
        canvas.drawText("TOTAL CREDIT (+)", 220f, 122f, paint)
        canvas.drawText("NET BALANCE", 410f, 122f, paint)

        paint.textSize = 14f
        paint.color = Color.parseColor("#DC2626")
        canvas.drawText(CurrencyFormatter.formatInr(totalDebit), 36f, 142f, paint)

        paint.color = Color.parseColor("#16A34A")
        canvas.drawText(CurrencyFormatter.formatInr(totalCredit), 220f, 142f, paint)

        paint.color = if (net < 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        val netLabel = if (net < 0) "Dr" else "Cr"
        canvas.drawText("${CurrencyFormatter.formatInr(Math.abs(net))} $netLabel", 410f, 142f, paint)

        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(24f, 175f, 571f, 195f, paint)
        paint.color = Color.parseColor("#475569")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE", 32f, 188f, paint)
        canvas.drawText("PARTY / DETAILS", 120f, 188f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DEBIT (-)", 380f, 188f, paint)
        canvas.drawText("CREDIT (+)", 470f, 188f, paint)
        canvas.drawText("BALANCE", 560f, 188f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = 215f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f

        val maxRows = 26
        transactions.take(maxRows).forEach { tx ->
            val isDebit = tx.amount < 0 || tx.type == "GAVE"
            val partyName = customerName ?: customerNameMap[tx.customerId] ?: "Party"

            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(DateUtils.formatDisplayDate(tx.timestamp), 32f, y, paint)

            val detail = if (tx.description.isNotBlank()) "$partyName (${tx.description})" else partyName
            canvas.drawText(detail.take(28), 120f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            if (isDebit) {
                paint.color = Color.parseColor("#DC2626")
                canvas.drawText(CurrencyFormatter.formatInr(Math.abs(tx.amount)), 380f, y, paint)
                paint.color = Color.parseColor("#94A3B8")
                canvas.drawText("-", 470f, y, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                canvas.drawText("-", 380f, y, paint)
                paint.color = Color.parseColor("#16A34A")
                canvas.drawText(CurrencyFormatter.formatInr(tx.amount), 470f, y, paint)
            }

            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(CurrencyFormatter.formatInr(Math.abs(tx.balance)), 560f, y, paint)
            paint.textAlign = Paint.Align.LEFT

            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawLine(24f, y + 6f, 571f, y + 6f, paint)
            y += 20f
        }

        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "reports")
        if (!outputDir.exists()) outputDir.mkdirs()
        val file = File(outputDir, "statement_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDoc.writeTo(outputStream)
        outputStream.close()
        pdfDoc.close()

        return file
    }
}
