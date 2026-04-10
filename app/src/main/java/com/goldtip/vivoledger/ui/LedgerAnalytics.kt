package com.goldtip.vivoledger.ui

import com.goldtip.vivoledger.data.TransactionEntity
import com.goldtip.vivoledger.data.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayDetailFormatter = DateTimeFormatter.ofPattern("yyyy年 M月 d日", Locale.CHINA)
private val monthDetailFormatter = DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINA)

object LedgerAnalyticsEngine {
    fun build(transactions: List<TransactionEntity>, currentDate: LocalDate): LedgerAnalytics {
        val sortedTransactions = transactions.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
        val visibleTransactions = sortedTransactions.filter { !it.date.isAfter(currentDate) }
        val visibleByDate = visibleTransactions.groupBy { it.date }
        val visibleByMonth = visibleTransactions.groupBy { YearMonth.from(it.date) }
        val visibleByYear = visibleTransactions.groupBy { it.date.year }

        val todayTransactions = visibleByDate[currentDate].orEmpty()
        val monthTransactions = visibleByMonth[YearMonth.from(currentDate)].orEmpty()
        val yearTransactions = visibleByYear[currentDate.year].orEmpty()
        val expenses = visibleTransactions.filter { it.type == TransactionType.EXPENSE }
        val averageExpensePerDay = if (expenses.isEmpty()) 0.0 else expenses.groupBy { it.date }.values.map { day -> day.sumOf { it.amount } }.average()
        val highestExpenseCategory = buildCategoryBreakdown(monthTransactions, TransactionType.EXPENSE).maxByOrNull { it.amount }?.category ?: "暂无"

        return LedgerAnalytics(
            totalIncome = visibleTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            totalExpense = visibleTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
            todayTransactions = todayTransactions,
            monthTransactions = monthTransactions,
            yearTransactions = yearTransactions,
            averageExpensePerDay = averageExpensePerDay,
            highestExpenseCategory = highestExpenseCategory,
            recentTransactions = visibleTransactions.take(6),
            detailSections = buildDaySections(visibleByDate),
            dayBuckets = buildDayBuckets(visibleTransactions),
            monthBuckets = buildMonthBuckets(visibleTransactions),
            yearBuckets = buildYearBuckets(visibleTransactions)
        )
    }

    fun buildCategoryBreakdown(items: List<TransactionEntity>, type: TransactionType): List<CategorySlice> {
        val typedItems = items.filter { it.type == type }
        val total = typedItems.sumOf { it.amount }
        if (total <= 0) return emptyList()

        return typedItems
            .groupBy { it.category }
            .map { (category, categoryItems) ->
                val amount = categoryItems.sumOf { it.amount }
                CategorySlice(category = category, amount = amount, ratio = amount / total)
            }
            .sortedByDescending { it.amount }
    }

    private fun buildDaySections(itemsByDate: Map<LocalDate, List<TransactionEntity>>): List<DaySection> {
        return itemsByDate
            .toSortedMap(compareByDescending { it })
            .map { (date, items) ->
                DaySection(
                    date = date,
                    income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                    items = items.sortedByDescending { it.id }
                )
            }
    }

    private fun buildDayBuckets(transactions: List<TransactionEntity>): List<ChartBucket> {
        return transactions
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
            .map { (date, items) ->
                ChartBucket(
                    id = date.toString(),
                    label = date.format(dayDetailFormatter),
                    trailingLabel = date.dayOfWeekLabel(),
                    income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                    transactions = items.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
                )
            }
    }

    private fun buildMonthBuckets(transactions: List<TransactionEntity>): List<ChartBucket> {
        return transactions
            .groupBy { YearMonth.from(it.date) }
            .toSortedMap(compareByDescending { it })
            .map { (month, items) ->
                ChartBucket(
                    id = month.toString(),
                    label = month.format(monthDetailFormatter),
                    trailingLabel = "${items.size}笔",
                    income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                    transactions = items.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
                )
            }
    }

    private fun buildYearBuckets(transactions: List<TransactionEntity>): List<ChartBucket> {
        return transactions
            .groupBy { it.date.year }
            .toSortedMap(compareByDescending { it })
            .map { (year, items) ->
                ChartBucket(
                    id = year.toString(),
                    label = "${year}年",
                    trailingLabel = "${items.size}笔",
                    income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                    transactions = items.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
                )
            }
    }
}
