package com.sumanonline.hisabkhata

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CustomerBalanceTest {

    @Test
    fun testAggregateYoullGetCalculation() {
        val balances = listOf(-500.0, -1200.0, 300.0, 0.0, -80.0)

        var totalGet = BigDecimal.ZERO
        balances.forEach { b ->
            if (b < 0.0) {
                totalGet = totalGet.add(BigDecimal.valueOf(Math.abs(b)))
            }
        }

        assertEquals(BigDecimal.valueOf(1780.0), totalGet)
    }

    @Test
    fun testAggregateYoullGiveCalculation() {
        val balances = listOf(-500.0, -1200.0, 300.0, 450.0, 0.0)

        var totalGive = BigDecimal.ZERO
        balances.forEach { b ->
            if (b > 0.0) {
                totalGive = totalGive.add(BigDecimal.valueOf(b))
            }
        }

        assertEquals(BigDecimal.valueOf(750.0), totalGive)
    }

    @Test
    fun testRunningBalanceSequence() {
        val transactions = listOf(
            Pair("GAVE", -500.0),
            Pair("GAVE", -300.0),
            Pair("GOT", 200.0)
        )

        var runningBalance = 0.0
        val history = mutableListOf<Double>()
        transactions.forEach { (_, amt) ->
            runningBalance += amt
            history.add(runningBalance)
        }

        assertEquals(listOf(-500.0, -800.0, -600.0), history)
    }
}
