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

    fun formatInrClean(amount: Double, withSpace: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(indianLocale)
        val absAmount = Math.abs(amount)
        val hasFractions = absAmount % 1.0 != 0.0
        if (hasFractions) {
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
        } else {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
        }
        val symbol = if (withSpace) "₹ " else "₹"
        return "$symbol${formatter.format(absAmount)}"
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
