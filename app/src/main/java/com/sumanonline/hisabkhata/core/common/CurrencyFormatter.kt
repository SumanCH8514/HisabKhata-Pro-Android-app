package com.sumanonline.hisabkhata.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val indianLocale = Locale("en", "IN")
    private val usLocale = Locale("en", "US")

    @Volatile
    var activeCurrencyFormat: String = "INR (₹)"

    fun getCurrencySymbol(): String {
        return when {
            activeCurrencyFormat.contains("$") -> "$"
            activeCurrencyFormat.contains("€") -> "€"
            activeCurrencyFormat.contains("£") -> "£"
            else -> "₹"
        }
    }

    private fun getLocale(): Locale {
        return if (activeCurrencyFormat.contains("$") || activeCurrencyFormat.contains("€") || activeCurrencyFormat.contains("£")) {
            usLocale
        } else {
            indianLocale
        }
    }

    fun formatInr(amount: BigDecimal, includeSymbol: Boolean = true): String {
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)
        val formatter = NumberFormat.getNumberInstance(getLocale())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        val formattedNumber = formatter.format(scaled)
        val sym = getCurrencySymbol()
        return if (includeSymbol) "$sym$formattedNumber" else formattedNumber
    }

    fun formatInr(amount: Double, includeSymbol: Boolean = true): String {
        return formatInr(BigDecimal.valueOf(amount), includeSymbol)
    }

    fun formatInrClean(amount: Double, withSpace: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(getLocale())
        val absAmount = Math.abs(amount)
        val hasFractions = absAmount % 1.0 != 0.0
        if (hasFractions) {
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
        } else {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
        }
        val symbol = getCurrencySymbol()
        val space = if (withSpace) " " else ""
        return "$symbol$space${formatter.format(absAmount)}"
    }

    fun formatInrCompact(amount: BigDecimal): String {
        val absVal = amount.abs()
        val sign = if (amount < BigDecimal.ZERO) "-" else ""
        val sym = getCurrencySymbol()
        return if (activeCurrencyFormat.contains("$") || activeCurrencyFormat.contains("€") || activeCurrencyFormat.contains("£")) {
            when {
                absVal >= BigDecimal("1000000000") -> {
                    val b = absVal.divide(BigDecimal("1000000000"), 2, RoundingMode.HALF_UP)
                    "${sign}$sym$b B"
                }
                absVal >= BigDecimal("1000000") -> {
                    val m = absVal.divide(BigDecimal("1000000"), 2, RoundingMode.HALF_UP)
                    "${sign}$sym$m M"
                }
                absVal >= BigDecimal("1000") -> {
                    val k = absVal.divide(BigDecimal("1000"), 1, RoundingMode.HALF_UP)
                    "${sign}$sym$k k"
                }
                else -> formatInr(amount)
            }
        } else {
            when {
                absVal >= BigDecimal("10000000") -> {
                    val cr = absVal.divide(BigDecimal("10000000"), 2, RoundingMode.HALF_UP)
                    "${sign}$sym$cr Cr"
                }
                absVal >= BigDecimal("100000") -> {
                    val lakh = absVal.divide(BigDecimal("100000"), 2, RoundingMode.HALF_UP)
                    "${sign}$sym$lakh L"
                }
                absVal >= BigDecimal("1000") -> {
                    val k = absVal.divide(BigDecimal("1000"), 1, RoundingMode.HALF_UP)
                    "${sign}$sym$k k"
                }
                else -> formatInr(amount)
            }
        }
    }
}
