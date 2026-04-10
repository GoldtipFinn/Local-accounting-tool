package com.goldtip.vivoledger.ui

import com.goldtip.vivoledger.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class SampleDataGeneratorTest {

    @Test
    fun `sample data covers requested range and monthly balance constraints`() {
        val transactions = SampleDataGenerator.generate()

        assertFalse(transactions.isEmpty())
        assertEquals(LocalDate.of(2020, 1, 1), transactions.minOf { it.date })
        assertEquals(LocalDate.of(2026, 4, 3), transactions.maxOf { it.date })

        val monthlyBalances = transactions
            .groupBy { YearMonth.from(it.date) }
            .mapValues { (_, items) ->
                val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                income - expense
            }

        assertTrue(monthlyBalances.values.all { it in -50_000.0..5_000.0 })
        assertTrue(monthlyBalances.values.any { it > 0 })
        assertTrue(monthlyBalances.values.any { it < 0 })
    }
}
