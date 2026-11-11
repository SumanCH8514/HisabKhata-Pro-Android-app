package com.sumanonline.hisabkhata

import com.sumanonline.hisabkhata.core.common.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FinancialCalculationsTest {

    @Test
    fun testCurrencyFormattingInr() {
        val amount = BigDecimal("12500.50")
        val formatted = CurrencyFormatter.formatInr(amount, includeSymbol = true)
        assertEquals("₹12,500.50", formatted)
    }

    @Test
    fun testCustomerBalanceMathForGave() {
        val initialBalance = -500.0
        val gaveAmount = 250.0
        val signedAmount = -Math.abs(gaveAmount)
        val newBalance = initialBalance + signedAmount

        assertEquals(-750.0, newBalance, 0.001)
    }

    @Test
    fun testCustomerBalanceMathForGot() {
        val initialBalance = -750.0
        val gotAmount = 500.0
        val signedAmount = Math.abs(gotAmount)
        val newBalance = initialBalance + signedAmount

        assertEquals(-250.0, newBalance, 0.001)
    }

    @Test
    fun testCustomerSettledBalance() {
        val initialBalance = -250.0
        val gotAmount = 250.0
        val signedAmount = Math.abs(gotAmount)
        val newBalance = initialBalance + signedAmount

        assertEquals(0.0, newBalance, 0.001)
    }

    @Test
    fun testCustomerAdvanceBalance() {
        val initialBalance = 0.0
        val advanceGot = 1000.0
        val signedAmount = Math.abs(advanceGot)
        val newBalance = initialBalance + signedAmount

        assertEquals(1000.0, newBalance, 0.001)
    }
}
