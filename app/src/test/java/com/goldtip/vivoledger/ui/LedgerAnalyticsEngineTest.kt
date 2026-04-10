package com.goldtip.vivoledger.ui

import com.goldtip.vivoledger.data.TransactionEntity
import com.goldtip.vivoledger.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LedgerAnalyticsEngineTest {

    @Test
    fun `build computes visible totals and grouped buckets`() {
        val transactions = listOf(
            TransactionEntity(id = 1, type = TransactionType.INCOME, category = "工资", amount = 1000.0, note = "", date = LocalDate.of(2026, 4, 3)),
            TransactionEntity(id = 2, type = TransactionType.EXPENSE, category = "学习", amount = 200.0, note = "", date = LocalDate.of(2026, 4, 3)),
            TransactionEntity(id = 3, type = TransactionType.EXPENSE, category = "餐饮", amount = 50.0, note = "", date = LocalDate.of(2026, 4, 2)),
            TransactionEntity(id = 4, type = TransactionType.INCOME, category = "红包", amount = 500.0, note = "", date = LocalDate.of(2026, 4, 4))
        )

        val analytics = LedgerAnalyticsEngine.build(transactions, LocalDate.of(2026, 4, 3))

        assertEquals(1000.0, analytics.totalIncome, 0.001)
        assertEquals(250.0, analytics.totalExpense, 0.001)
        assertEquals(2, analytics.todayTransactions.size)
        assertEquals(3, analytics.monthTransactions.size)
        assertEquals("学习", analytics.highestExpenseCategory)
        assertTrue(analytics.dayBuckets.any { it.id == "2026-04-03" && it.expense == 200.0 })
        assertTrue(analytics.monthBuckets.any { it.id == "2026-04" && it.income == 1000.0 })
        assertTrue(analytics.yearBuckets.any { it.id == "2026" && it.expense == 250.0 })
    }
}
