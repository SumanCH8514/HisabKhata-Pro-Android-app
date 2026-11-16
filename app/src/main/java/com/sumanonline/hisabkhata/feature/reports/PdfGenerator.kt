package com.sumanonline.hisabkhata.feature.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.graphics.PathParser
import com.sumanonline.hisabkhata.R
import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import com.sumanonline.hisabkhata.core.common.DateUtils
import com.sumanonline.hisabkhata.core.database.entity.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

object PdfGenerator {

    private val phonePath: Path by lazy {
        PathParser.createPathFromPathData("M6.62,10.79c1.44,2.83 3.76,5.14 6.59,6.59l2.2,-2.2c0.27,-0.27 0.67,-0.36 1.02,-0.24 1.12,0.37 2.33,0.57 3.57,0.57 0.55,0 1,0.45 1,1V20c0,0.55 -0.45,1 -1,1 -9.39,0 -17,-7.61 -17,-17 0,-0.55 0.45,-1 1,-1h3.5c0.55,0 1,0.45 1,1 0,1.25 0.2,2.45 0.57,3.57 0.11,0.35 0.03,0.74 -0.25,1.02l-2.2,2.2z")
    }

    private val emailPath: Path by lazy {
        PathParser.createPathFromPathData("M20,4H4C2.9,4 2.01,4.9 2.01,6L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V6C22,4.9 21.1,4 20,4zM20,8l-8,5 -8,-5V6l8,5 8,-5V8z")
    }

    @Suppress("DEPRECATION")
    private fun drawIcon(canvas: Canvas, path: Path, x: Float, y: Float, size: Float, color: Int) {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val maxDim = maxOf(bounds.width(), bounds.height()).coerceAtLeast(1f)
        val scale = size / maxDim
        val matrix = Matrix().apply {
            postTranslate(-bounds.left, -bounds.top)
            postScale(scale, scale)
            postTranslate(x, y)
        }
        val transformed = Path()
        path.transform(matrix, transformed)
        val iconPaint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawPath(transformed, iconPaint)
    }

    fun generateStatementPdf(
        context: Context,
        merchantName: String,
        merchantPhone: String,
        merchantEmail: String? = null,
        merchantOwnerName: String? = null,
        customerName: String?,
        customerPhone: String? = null,
        customerEmail: String? = null,
        startDate: String,
        endDate: String,
        transactions: List<TransactionEntity>,
        customerNameMap: Map<String, String> = emptyMap()
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var pageNumber = 1

        val paint = Paint()
        paint.isAntiAlias = true

        val logoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo_round)
        } catch (_: Exception) {
            null
        }

        var leftLines = 3
        if (!customerPhone.isNullOrBlank()) leftLines++
        if (!customerEmail.isNullOrBlank()) leftLines++

        var rightLines = 1
        val showMerchantOwner = !merchantOwnerName.isNullOrBlank() && !merchantOwnerName.equals(merchantName, ignoreCase = true)
        if (showMerchantOwner) rightLines++
        if (merchantPhone.isNotBlank()) rightLines++
        if (!merchantEmail.isNullOrBlank()) rightLines++

        val maxLines = maxOf(leftLines, rightLines)
        val (headerHeight, lineSpacing, startY) = when {
            maxLines >= 5 -> Triple(108f, 13.5f, 30f)
            maxLines == 4 -> Triple(98f, 14.5f, 32f)
            else -> Triple(88f, 15f, 34f)
        }

        paint.color = Color.parseColor("#0057BB")
        canvas.drawRect(0f, 0f, 595f, headerHeight, paint)

        val textStartX: Float
        if (logoBitmap != null) {
            val logoSize = 46f
            val logoLeft = 24f
            val logoTop = (headerHeight - logoSize) / 2f
            paint.color = Color.WHITE
            canvas.drawCircle(logoLeft + logoSize / 2f, logoTop + logoSize / 2f, logoSize / 2f, paint)
            canvas.drawBitmap(logoBitmap, null, RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize), paint)
            textStartX = logoLeft + logoSize + 12f
        } else {
            textStartX = 24f
        }

        val iconSize = 8f
        val iconTextGap = 3.5f
        val subColor = Color.parseColor("#E0E7FF")

        var leftY = startY

        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("HisabKhata PRO", textStartX, leftY, paint)
        leftY += lineSpacing

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val subtitle = if (customerName != null) "Customer Statement: $customerName" else "Business Transaction Statement"
        canvas.drawText(subtitle, textStartX, leftY, paint)
        leftY += lineSpacing

        if (!customerPhone.isNullOrBlank()) {
            val phoneStr = customerPhone.trim()
            drawIcon(canvas, phonePath, textStartX, leftY - 6.5f, iconSize, subColor)
            paint.color = subColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(phoneStr, textStartX + iconSize + iconTextGap, leftY, paint)
            leftY += lineSpacing
        }

        if (!customerEmail.isNullOrBlank()) {
            var emailStr = customerEmail.trim()
            paint.color = subColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (paint.measureText(emailStr) > 230f) {
                while (emailStr.length > 3 && paint.measureText("$emailStr…") > 230f) {
                    emailStr = emailStr.dropLast(1)
                }
                emailStr = "$emailStr…"
            }
            drawIcon(canvas, emailPath, textStartX, leftY - 6.5f, iconSize, subColor)
            canvas.drawText(emailStr, textStartX + iconSize + iconTextGap, leftY, paint)
            leftY += lineSpacing
        }

        paint.color = subColor
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Period: $startDate to $endDate", textStartX, leftY, paint)

        var rightY = startY
        paint.textAlign = Paint.Align.RIGHT

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(merchantName.uppercase(), 571f, rightY, paint)
        rightY += lineSpacing

        if (showMerchantOwner) {
            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(merchantOwnerName.trim(), 571f, rightY, paint)
            rightY += lineSpacing
        }

        if (merchantPhone.isNotBlank()) {
            val phoneStr = merchantPhone.trim()
            paint.color = subColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val textW = paint.measureText(phoneStr)
            val iconX = 571f - textW - iconSize - iconTextGap
            drawIcon(canvas, phonePath, iconX, rightY - 6.5f, iconSize, subColor)
            canvas.drawText(phoneStr, 571f, rightY, paint)
            rightY += lineSpacing
        }

        if (!merchantEmail.isNullOrBlank()) {
            var emailStr = merchantEmail.trim()
            paint.color = subColor
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (paint.measureText(emailStr) > 230f) {
                while (emailStr.length > 3 && paint.measureText("$emailStr…") > 230f) {
                    emailStr = emailStr.dropLast(1)
                }
                emailStr = "$emailStr…"
            }
            val textW = paint.measureText(emailStr)
            val iconX = 571f - textW - iconSize - iconTextGap
            drawIcon(canvas, emailPath, iconX, rightY - 6.5f, iconSize, subColor)
            canvas.drawText(emailStr, 571f, rightY, paint)
        }
        paint.textAlign = Paint.Align.LEFT

        val summaryTop = headerHeight + 12f
        val summaryBottom = summaryTop + 50f
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(24f, summaryTop, 571f, summaryBottom, 10f, 10f, paint)
        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(24f, summaryTop, 571f, summaryBottom, 10f, 10f, paint)
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

        val summaryLabelY = summaryTop + 17f
        val summaryValueY = summaryTop + 37f

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL DEBIT (-)", 36f, summaryLabelY, paint)
        canvas.drawText("TOTAL CREDIT (+)", 220f, summaryLabelY, paint)
        canvas.drawText("NET BALANCE", 410f, summaryLabelY, paint)

        paint.textSize = 13.5f
        paint.color = Color.parseColor("#DC2626")
        canvas.drawText(CurrencyFormatter.formatInr(totalDebit), 36f, summaryValueY, paint)

        paint.color = Color.parseColor("#16A34A")
        canvas.drawText(CurrencyFormatter.formatInr(totalCredit), 220f, summaryValueY, paint)

        paint.color = if (net < 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        val netLabel = if (net < 0) "Dr" else "Cr"
        canvas.drawText("${CurrencyFormatter.formatInr(Math.abs(net))} $netLabel", 410f, summaryValueY, paint)

        val tableHeaderTop = summaryBottom + 14f
        val tableHeaderBottom = tableHeaderTop + 20f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(24f, tableHeaderTop, 571f, tableHeaderBottom, paint)
        paint.color = Color.parseColor("#475569")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val tableHeaderBaseline = tableHeaderTop + 13.5f
        canvas.drawText("DATE", 32f, tableHeaderBaseline, paint)
        canvas.drawText("PARTY / DETAILS", 120f, tableHeaderBaseline, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DEBIT (-)", 380f, tableHeaderBaseline, paint)
        canvas.drawText("CREDIT (+)", 470f, tableHeaderBaseline, paint)
        canvas.drawText("BALANCE", 560f, tableHeaderBaseline, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = tableHeaderBottom + 16f

        transactions.forEach { tx ->
            val isDebit = tx.amount < 0 || tx.type == "GAVE"
            val partyName = customerName ?: customerNameMap[tx.customerId] ?: "Party"
            val description = tx.description.trim()

            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val descLines = if (description.isNotBlank()) {
                wrapText(description, paint, maxWidth = 180f)
            } else {
                emptyList()
            }

            val lastLineY = if (descLines.isEmpty()) y else (y + 11.5f * descLines.size)
            val rowDividerY = lastLineY + 9f

            if (rowDividerY > 795f) {
                drawFooter(canvas, paint)
                pdfDoc.finishPage(page)
                pageNumber++
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas

                paint.color = Color.parseColor("#0057BB")
                canvas.drawRect(0f, 0f, 595f, 38f, paint)
                paint.color = Color.WHITE
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("HisabKhata PRO - Statement (Contd.)", 24f, 24f, paint)
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Page $pageNumber", 571f, 24f, paint)
                paint.textAlign = Paint.Align.LEFT

                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawRect(24f, 48f, 571f, 68f, paint)
                paint.color = Color.parseColor("#475569")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("DATE", 32f, 61f, paint)
                canvas.drawText("PARTY / DETAILS", 120f, 61f, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("DEBIT (-)", 380f, 61f, paint)
                canvas.drawText("CREDIT (+)", 470f, 61f, paint)
                canvas.drawText("BALANCE", 560f, 61f, paint)
                paint.textAlign = Paint.Align.LEFT

                y = 68f + 16f
            }

            val actualLastLineY = if (descLines.isEmpty()) y else (y + 11.5f * descLines.size)
            val actualDividerY = actualLastLineY + 9f

            paint.color = Color.parseColor("#334155")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val resolvedTs = DateUtils.getResolvedTimestamp(tx.date, tx.timestamp)
            canvas.drawText(DateUtils.formatDisplayDate(resolvedTs), 32f, y, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val displayParty = if (paint.measureText(partyName) > 180f) {
                var p = partyName
                while (p.length > 3 && paint.measureText("$p…") > 180f) {
                    p = p.dropLast(1)
                }
                "$p…"
            } else {
                partyName
            }
            canvas.drawText(displayParty, 120f, y, paint)

            if (descLines.isNotEmpty()) {
                paint.color = Color.parseColor("#64748B")
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                descLines.forEachIndexed { index, line ->
                    canvas.drawText(line, 120f, y + 11.5f * (index + 1), paint)
                }
            }

            paint.textSize = 9f
            paint.textAlign = Paint.Align.RIGHT
            if (isDebit) {
                paint.color = Color.parseColor("#DC2626")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(CurrencyFormatter.formatInr(Math.abs(tx.amount)), 380f, y, paint)
                paint.color = Color.parseColor("#94A3B8")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("-", 470f, y, paint)
            } else {
                paint.color = Color.parseColor("#94A3B8")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("-", 380f, y, paint)
                paint.color = Color.parseColor("#16A34A")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(CurrencyFormatter.formatInr(tx.amount), 470f, y, paint)
            }

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(CurrencyFormatter.formatInr(Math.abs(tx.balance)), 560f, y, paint)
            paint.textAlign = Paint.Align.LEFT

            paint.color = Color.parseColor("#E2E8F0")
            paint.strokeWidth = 0.5f
            canvas.drawLine(24f, actualDividerY, 571f, actualDividerY, paint)

            y = actualDividerY + 16f
        }

        drawFooter(canvas, paint)
        pdfDoc.finishPage(page)

        val outputDir = File(context.cacheDir, "reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val titles = setOf("mr", "mrs", "ms", "dr", "prof", "shri", "smt")
        val words = customerName?.trim()?.split("\\s+".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
        val nameWord = words.firstOrNull { word ->
            val clean = word.replace("[^A-Za-z0-9]".toRegex(), "").lowercase()
            clean !in titles
        } ?: words.firstOrNull()
        val partyFirstName = nameWord?.replace("[^A-Za-z0-9]".toRegex(), "")?.ifBlank { null } ?: "All"

        val timeStamp = System.currentTimeMillis()
        val fileName = "HisabkhataPro_statement_${partyFirstName}_$timeStamp.pdf"
        val file = File(outputDir, fileName)
        val outputStream = FileOutputStream(file)
        pdfDoc.writeTo(outputStream)
        outputStream.close()
        pdfDoc.close()

        return file
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                if (paint.measureText(word) > maxWidth) {
                    var chunk = StringBuilder()
                    for (ch in word) {
                        if (paint.measureText(chunk.toString() + ch) <= maxWidth) {
                            chunk.append(ch)
                        } else {
                            lines.add(chunk.toString())
                            chunk = StringBuilder(ch.toString())
                        }
                    }
                    currentLine = chunk
                } else {
                    currentLine = StringBuilder(word)
                }
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun drawFooter(canvas: Canvas, paint: Paint) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val prefix = "© $currentYear "
        val brand = "HisabKhata Pro"
        val suffix = "| SumanOnline Web Services| All Rights Reserved  "

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val prefixWidth = paint.measureText(prefix)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val brandWidth = paint.measureText(brand)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val suffixWidth = paint.measureText(suffix)

        val totalWidth = prefixWidth + brandWidth + suffixWidth
        val startX = (595f - totalWidth) / 2f
        val footerY = 824f

        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 0.8f
        canvas.drawLine(24f, 808f, 571f, 808f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(prefix, startX, footerY, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(brand, startX + prefixWidth, footerY, paint)

        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(suffix, startX + prefixWidth + brandWidth, footerY, paint)
    }
}
