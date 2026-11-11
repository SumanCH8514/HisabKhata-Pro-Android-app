package com.sumanonline.hisabkhata.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val indianLocale = Locale("en", "IN")

    fun formatInr(amount: BigDecimal, includeSymbol: Boolean = true): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getNumberInstance(indianLocale)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        val formattedNumber = formatter.format(scaled)
        return if (includeSymbol) "₹$formattedNumber" else formattedNumber
    }

    fun formatInr(amount: Double, includeSymbol: Boolean = true): String {
        return formatInr(BigDecimal.valueOf(amount), includeSymbol)
    }

    fun formatInrCompact(amount: BigDecimal): String {
        val absVal = amount.abs()
        val sign = if (amount < BigDecimal.ZERO) "-" else ""
        return when {
            absVal >= BigDecimal("10000000") -> {
                val cr = absVal.divide(BigDecimal("10000000"), 2, RoundingMode.HALF_UP)
                "${sign}₹$cr Cr"
            }
            absVal >= BigDecimal("100000") -> {
                val lakh = absVal.divide(BigDecimal("100000"), 2, RoundingMode.HALF_UP)
                "${sign}₹$lakh L"
            }
            absVal >= BigDecimal("1000") -> {
                val k = absVal.divide(BigDecimal("1000"), 1, RoundingMode.HALF_UP)
                "${sign}₹$k k"
            }
            else -> formatInr(amount)
        }
    }
}
