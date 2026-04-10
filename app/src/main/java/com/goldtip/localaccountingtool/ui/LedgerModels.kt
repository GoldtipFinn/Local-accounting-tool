package com.goldtip.localaccountingtool.ui

import com.goldtip.localaccountingtool.data.TransactionEntity
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.abs

enum class BottomNavPage(val label: String) {
    ENTRY("记账"),
    DETAIL("明细"),
    CHART("图表"),
    SETTINGS("设置")
}

enum class ChartScope(val label: String) {
    DAY("日"),
    MONTH("月"),
    YEAR("年")
}

enum class ExportFormat(
    val label: String,
    val extension: String,
    val mimeType: String,
    val description: String
) {
    CSV("CSV", "csv", "text/csv", "Excel 可直接打开，适合导入表格"),
    EXCEL_XML("Excel XML", "xml", "application/vnd.ms-excel", "保留列结构，适合 Excel"),
    JSON("JSON", "json", "application/json", "更适合备份和后续导入")
}

data class VisualizationPalette(
    val name: String,
    val incomeColor: Long,
    val expenseColor: Long,
    val accentColor: Long,
    val lineColor: Long,
    val backgroundTop: Long,
    val backgroundBottom: Long
)

enum class CustomColorTarget(val label: String) {
    INCOME("收入色"),
    EXPENSE("支出色"),
    LINE("折线色"),
    BACKGROUND_TOP("背景上段"),
    BACKGROUND_BOTTOM("背景下段")
}

data class DaySection(
    val date: LocalDate,
    val income: Double,
    val expense: Double,
    val items: List<TransactionEntity>
)

data class CategorySlice(
    val category: String,
    val amount: Double,
    val ratio: Double
)

data class ChartBucket(
    val id: String,
    val label: String,
    val trailingLabel: String,
    val income: Double,
    val expense: Double,
    val transactions: List<TransactionEntity>
)

data class MonthlyTrendPoint(
    val label: String,
    val income: Double,
    val expense: Double,
    val balance: Double
)

data class LedgerAnalytics(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val todayTransactions: List<TransactionEntity> = emptyList(),
    val monthTransactions: List<TransactionEntity> = emptyList(),
    val yearTransactions: List<TransactionEntity> = emptyList(),
    val averageExpensePerDay: Double = 0.0,
    val highestExpenseCategory: String = "暂无",
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val detailSections: List<DaySection> = emptyList(),
    val dayBuckets: List<ChartBucket> = emptyList(),
    val monthBuckets: List<ChartBucket> = emptyList(),
    val yearBuckets: List<ChartBucket> = emptyList()
) {
    companion object {
        val EMPTY = LedgerAnalytics()
    }
}

val palettePresets = listOf(
    VisualizationPalette("枫叶", 0xFFBA6F1EL, 0xFF1F7A8CL, 0xFF7A3E00L, 0xFF7A3E00L, 0xFFFFF5E8L, 0xFFF6FBFFL),
    VisualizationPalette("森林", 0xFF2D8F6FL, 0xFF315C9BL, 0xFF1F4A3DL, 0xFF1F4A3DL, 0xFFF2FBF6L, 0xFFF5F8FCL),
    VisualizationPalette("珊瑚", 0xFFE07A5FL, 0xFF3D405BL, 0xFF81B29AL, 0xFF81B29AL, 0xFFFFF3F0L, 0xFFF4F7FBL),
    VisualizationPalette("冰川", 0xFF4D96FFL, 0xFF183A5DL, 0xFF6BCB77L, 0xFF6BCB77L, 0xFFF1F7FFL, 0xFFF6FBF9L),
    VisualizationPalette("落日", 0xFFF4A261L, 0xFF264653L, 0xFFE76F51L, 0xFFE76F51L, 0xFFFFF0E6L, 0xFFFFF7F2L),
    VisualizationPalette("霓虹", 0xFF7B2CBFL, 0xFF3C096CL, 0xFFFF8500L, 0xFFFF8500L, 0xFFF8F0FFL, 0xFFFFF8EFL)
)

val solidColorChoices = listOf(
    0xFF2D8F6FL, 0xFFBA6F1EL, 0xFF1F7A8CL, 0xFFE07A5FL, 0xFF7B2CBFL,
    0xFF4D96FFL, 0xFFE76F51L, 0xFF264653L, 0xFF81B29AL, 0xFFFF8500L
)

val backgroundColorChoices = listOf(
    0xFFFFF5E8L, 0xFFF6FBFFL, 0xFFF2FBF6L, 0xFFF5F8FCL,
    0xFFFFF3F0L, 0xFFF4F7FBL, 0xFFF8F0FFL, 0xFFFFF8EFL
)

val expenseCategories = listOf("餐饮", "交通", "购物", "住房", "娱乐", "医疗", "学习", "人情", "其他")
val incomeCategories = listOf("工资", "奖金", "兼职", "理财", "退款", "红包", "其他")

fun Double.currency(): String = String.format(Locale.CHINA, "¥%,.2f", this)

fun Double.percent(): String = String.format(Locale.CHINA, "%.0f%%", this * 100)

fun Double.shortCurrency(): String {
    val absolute = abs(this)
    return if (absolute >= 10_000) String.format(Locale.CHINA, "¥%.1f万", this / 10_000) else currency()
}

fun Double.noSymbolShort(): String {
    val absolute = abs(this)
    return if (absolute >= 10_000) String.format(Locale.CHINA, "%.1f万", this / 10_000)
    else String.format(Locale.CHINA, "%,.0f", this)
}

fun LocalDate.dayOfWeekLabel(): String = when (dayOfWeek.value) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    else -> "周日"
}

fun normalizeDayQuery(query: String): String? {
    val trimmed = query.trim()
    return when {
        Regex("""\d{4}-\d{2}-\d{2}""").matches(trimmed) -> trimmed
        Regex("""\d{8}""").matches(trimmed) -> "${trimmed.substring(0, 4)}-${trimmed.substring(4, 6)}-${trimmed.substring(6, 8)}"
        else -> null
    }
}

fun normalizeMonthQuery(query: String): String? {
    val trimmed = query.trim()
    return if (Regex("""\d{6}""").matches(trimmed)) "${trimmed.substring(0, 4)}-${trimmed.substring(4, 6)}" else null
}

fun normalizeYearQuery(query: String): String? {
    val trimmed = query.trim()
    return if (Regex("""\d{4}""").matches(trimmed)) trimmed else null
}

fun YearMonth.label(): String = "${year}年 ${monthValue}月"
