package com.goldtip.localaccountingtool.ui

import com.goldtip.localaccountingtool.data.TransactionEntity
import com.goldtip.localaccountingtool.data.TransactionType
import java.time.LocalDate
import java.time.YearMonth

object SampleDataGenerator {
    fun generate(): List<TransactionEntity> {
        val startDate = LocalDate.of(2020, 1, 1)
        val endDate = LocalDate.of(2026, 4, 3)
        val incomeNotes = listOf("工资入账", "奖励入账", "副业结算", "退款或收款", "理财收益")
        val expenseNotes = listOf("日常消费", "必要开支", "临时购物", "社交与出行", "家庭与生活")
        val results = mutableListOf<TransactionEntity>()
        val balancePattern = listOf(4200.0, -3600.0, -12800.0, 2600.0, -22100.0, -46800.0, 3800.0, -7400.0, -18500.0, 1400.0, -27400.0, -45200.0)

        var month = YearMonth.from(startDate)
        val lastMonth = YearMonth.from(endDate)
        while (!month.isAfter(lastMonth)) {
            val firstDate = maxOf(startDate, month.atDay(1))
            val lastDate = minOf(endDate, month.atEndOfMonth())
            val monthDays = generateSequence(firstDate) { current ->
                current.plusDays(1).takeIf { !it.isAfter(lastDate) }
            }.toList()
            val monthIndex = (month.year - startDate.year) * 12 + (month.monthValue - startDate.monthValue)
            val targetBalance = balancePattern[monthIndex % balancePattern.size]
            val monthlyIncome = (23000 + (month.year - 2020) * 850 + month.monthValue * 180 + (monthIndex % 4) * 420).toDouble()
            val monthlyExpense = monthlyIncome - targetBalance

            val salaryDate = monthDays.first()
            val midDate = monthDays[minOf(monthDays.lastIndex, monthDays.size / 2)]
            val incomeDates = buildList {
                add(salaryDate)
                if (midDate != salaryDate) add(midDate)
                monthDays.filter { it.dayOfMonth % 7 == 0 && it != salaryDate && it != midDate }.take(2).forEach { add(it) }
            }.distinct()
            val incomeWeights = incomeDates.mapIndexed { index, date ->
                when (index) {
                    0 -> 5.4
                    1 -> 2.3
                    else -> 1.2 + (date.dayOfMonth % 3) * 0.2
                }
            }
            allocateByWeights(monthlyIncome, incomeWeights).forEachIndexed { index, amount ->
                val date = incomeDates[index]
                results += TransactionEntity(
                    type = TransactionType.INCOME,
                    category = incomeCategories[(monthIndex + index * 2 + date.dayOfMonth) % incomeCategories.size],
                    amount = amount,
                    note = incomeNotes[(monthIndex + index + date.dayOfMonth) % incomeNotes.size],
                    date = date
                )
            }

            val expenseWeights = monthDays.map { date ->
                1.0 + (date.dayOfMonth % 5) * 0.14 + if (date.dayOfWeek.value >= 6) 0.35 else 0.0 + if (date.dayOfMonth in listOf(1, 15, 28)) 0.55 else 0.0
            }
            val dailyExpenseTotals = allocateByWeights(monthlyExpense, expenseWeights)
            monthDays.forEachIndexed { dayIndex, date ->
                val transactionCount = if (date.dayOfWeek.value >= 6 || date.dayOfMonth % 6 == 0) 2 else 1
                val splitWeights = if (transactionCount == 2) listOf(0.62, 0.38) else listOf(1.0)
                allocateByWeights(dailyExpenseTotals[dayIndex], splitWeights).forEachIndexed { index, amount ->
                    results += TransactionEntity(
                        type = TransactionType.EXPENSE,
                        category = expenseCategories[(monthIndex + dayIndex + index * 3) % expenseCategories.size],
                        amount = amount,
                        note = expenseNotes[(dayIndex + index + month.monthValue) % expenseNotes.size],
                        date = date
                    )
                }
            }
            month = month.plusMonths(1)
        }
        return results
    }
}

private fun allocateByWeights(totalAmount: Double, weights: List<Double>): List<Double> {
    if (weights.isEmpty()) return emptyList()
    val totalCents = (totalAmount * 100).toLong()
    val safeWeights = weights.map { it.coerceAtLeast(0.01) }
    val weightSum = safeWeights.sum()
    val rawCents = safeWeights.map { totalCents * (it / weightSum) }
    val baseCents = rawCents.map { it.toLong() }.toMutableList()
    var remainder = totalCents - baseCents.sum()
    rawCents
        .mapIndexed { index, value -> index to (value - value.toLong()) }
        .sortedByDescending { it.second }
        .forEach { (index, _) ->
            if (remainder <= 0) return@forEach
            baseCents[index] = baseCents[index] + 1
            remainder -= 1
        }
    return baseCents.map { it / 100.0 }
}
