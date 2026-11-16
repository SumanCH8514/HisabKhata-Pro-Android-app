package com.sumanonline.hisabkhata.feature.payments.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.util.Locale

object PaymentQrPosterGenerator {

    fun generatePoster(
        qrBitmap: Bitmap,
        amount: Double,
        customerName: String,
        businessName: String = "SumanOnline"
    ): Bitmap {
        val width = 1080
        val height = 1920
        val poster = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(poster)

        // 1. Overall soft background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Top Blue Diagonal Section (#0057D9)
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0057D9")
            style = Paint.Style.FILL
        }
        val bluePath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width.toFloat(), 0f)
            lineTo(width.toFloat(), 360f)
            lineTo(0f, 470f)
            close()
        }
        canvas.drawPath(bluePath, bluePaint)

        // 3. Top Blue Header Texts
        // "PAYMENT QR"
        val topLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("PAYMENT QR", width / 2f, 105f, topLabelPaint)

        // "HisabKhata PRO"
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 62f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("HisabKhata PRO", width / 2f, 185f, titlePaint)

        // Subtitle: e.g. "a SumanOnline Website"
        val cleanBiz = if (businessName.isNotBlank()) businessName.trim() else "SumanOnline"
        val subtitleText = when {
            cleanBiz.contains("Website", ignoreCase = true) -> cleanBiz
            cleanBiz.startsWith("a ", ignoreCase = true) -> cleanBiz
            else -> "a $cleanBiz Website"
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitleText, width / 2f, 235f, subtitlePaint)

        // 4. White Center Card
        val cardLeft = 100f
        val cardRight = width - 100f
        val cardTop = 310f
        val cardBottom = 1290f
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        val cornerRadius = 52f

        // Soft Drop Shadow for card
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 15, 23, 42)
            style = Paint.Style.FILL
            setShadowLayer(45f, 0f, 16f, Color.argb(45, 15, 23, 42))
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, shadowPaint)

        // Card Fill
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint)

        // 5. Card Content
        // "BHIM | UPI"
        val bhimUpiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.04f
        }
        canvas.drawText("BHIM | UPI", width / 2f, cardTop + 100f, bhimUpiPaint)

        // QR Code Bitmap
        val qrSize = 640f
        val qrLeft = (width - qrSize) / 2f
        val qrTop = cardTop + 155f
        val qrRect = RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
        val qrPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val srcRect = Rect(0, 0, qrBitmap.width, qrBitmap.height)
        canvas.drawBitmap(qrBitmap, srcRect, qrRect, qrPaint)

        // "SCAN & PAY WITH ANY UPI APP"
        val scanTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("SCAN & PAY WITH ANY UPI APP", width / 2f, cardBottom - 55f, scanTextPaint)

        // 6. Bottom Information Section
        // Amount: e.g. "₹40" or "₹635"
        val amountStr = if (amount % 1.0 == 0.0) {
            "₹${amount.toLong()}"
        } else {
            "₹${String.format(Locale.US, "%.2f", amount)}"
        }
        val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0057D9")
            textSize = 88f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(amountStr, width / 2f, 1420f, amountPaint)

        // Customer / Target Name
        val cleanName = customerName.ifBlank { "Customer" }.trim()
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cleanName, width / 2f, 1500f, namePaint)

        // Note:
        // "Secure payment for <Name>. Verified"
        // "HisabKhata Digital Statement."
        val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val line1 = "Secure payment for $cleanName. Verified"
        val line2 = "HisabKhata Digital Statement."
        canvas.drawText(line1, width / 2f, 1565f, notePaint)
        canvas.drawText(line2, width / 2f, 1605f, notePaint)

        // 7. Security Badge at Bottom
        // Green dot + "100% SECURE DIGITAL PAYMENTS"
        val secureText = "100% SECURE DIGITAL PAYMENTS"
        val securePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val dotRadius = 9f
        val dotSpacing = 16f
        val textWidth = securePaint.measureText(secureText)
        val totalBadgeWidth = (dotRadius * 2) + dotSpacing + textWidth
        val badgeStartX = (width - totalBadgeWidth) / 2f
        val badgeCenterY = 1720f

        val greenDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#10B981") // Emerald Green
            style = Paint.Style.FILL
        }
        canvas.drawCircle(badgeStartX + dotRadius, badgeCenterY - 7f, dotRadius, greenDotPaint)
        canvas.drawText(secureText, badgeStartX + (dotRadius * 2) + dotSpacing, badgeCenterY, securePaint)

        return poster
    }
}
