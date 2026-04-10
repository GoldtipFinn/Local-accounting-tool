package com.goldtip.localaccountingtool.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.goldtip.localaccountingtool.data.LedgerDatabase
import com.goldtip.localaccountingtool.data.LedgerRepository
import com.goldtip.localaccountingtool.data.TransactionEntity
import com.goldtip.localaccountingtool.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

data class LedgerUiState(
    val bottomNavPage: BottomNavPage = BottomNavPage.ENTRY,
    val chartScope: ChartScope = ChartScope.DAY,
    val chartSearchInput: String = "",
    val chartSearchMessage: String? = null,
    val prioritizedChartBucketId: String? = null,
    val selectedChartBucketId: String? = null,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val noteInput: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val currentDateTime: LocalDateTime = LocalDateTime.now(),
    val selectedCategory: String = expenseCategories.first(),
    val transactions: List<TransactionEntity> = emptyList(),
    val analytics: LedgerAnalytics = LedgerAnalytics.EMPTY,
    val paletteIndex: Int = 0,
    val useCustomColors: Boolean = false,
    val customIncomeColor: Long = solidColorChoices[0],
    val customExpenseColor: Long = solidColorChoices[1],
    val customLineColor: Long = solidColorChoices[2],
    val customBackgroundTop: Long = backgroundColorChoices[0],
    val customBackgroundBottom: Long = backgroundColorChoices[1],
    val databaseFolderPath: String = "",
    val exportFolderUri: String = "",
    val exportFolderLabel: String = "未选择",
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val exportMessage: String? = null,
    val exportInProgress: Boolean = false
) {
    val activePalette: VisualizationPalette
        get() {
            val preset = palettePresets[paletteIndex.coerceIn(0, palettePresets.lastIndex)]
            return if (useCustomColors) {
                preset.copy(
                    name = "自定义",
                    incomeColor = customIncomeColor,
                    expenseColor = customExpenseColor,
                    lineColor = customLineColor,
                    backgroundTop = customBackgroundTop,
                    backgroundBottom = customBackgroundBottom
                )
            } else {
                preset
            }
        }

    val currentDate: LocalDate
        get() = currentDateTime.toLocalDate()

    val currentCategories: List<String>
        get() = if (selectedType == TransactionType.INCOME) incomeCategories else expenseCategories

    val amountValue: Double?
        get() = amountInput.toDoubleOrNull()

    val canSave: Boolean
        get() {
            val amount = amountValue ?: return false
            return amount > 0
        }

    val totalIncome: Double
        get() = analytics.totalIncome

    val totalExpense: Double
        get() = analytics.totalExpense

    val balance: Double
        get() = totalIncome - totalExpense

    val todayTransactions: List<TransactionEntity>
        get() = analytics.todayTransactions

    val thisMonthTransactions: List<TransactionEntity>
        get() = analytics.monthTransactions

    val thisYearTransactions: List<TransactionEntity>
        get() = analytics.yearTransactions

    val todayIncome: Double
        get() = todayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val todayExpense: Double
        get() = todayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val monthIncome: Double
        get() = thisMonthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val monthExpense: Double
        get() = thisMonthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val yearIncome: Double
        get() = thisYearTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val yearExpense: Double
        get() = thisYearTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val averageExpensePerDay: Double
        get() = analytics.averageExpensePerDay

    val highestExpenseCategory: String
        get() = analytics.highestExpenseCategory

    val recentTransactions: List<TransactionEntity>
        get() = analytics.recentTransactions

    val detailSections: List<DaySection>
        get() = analytics.detailSections

    val chartBuckets: List<ChartBucket>
        get() {
            val buckets = when (chartScope) {
                ChartScope.DAY -> analytics.dayBuckets
                ChartScope.MONTH -> analytics.monthBuckets
                ChartScope.YEAR -> analytics.yearBuckets
            }
            val prioritized = prioritizedChartBucketId ?: return buckets
            val prioritizedBucket = buckets.firstOrNull { it.id == prioritized } ?: return buckets
            return listOf(prioritizedBucket) + buckets.filterNot { it.id == prioritized }
        }

    val selectedChartBucket: ChartBucket?
        get() {
            val buckets = chartBuckets
            if (buckets.isEmpty()) return null
            return buckets.firstOrNull { it.id == selectedChartBucketId } ?: buckets.first()
        }

    val selectedBucketIncomeCategories: List<CategorySlice>
        get() = LedgerAnalyticsEngine.buildCategoryBreakdown(selectedChartBucket?.transactions.orEmpty(), TransactionType.INCOME)

    val selectedBucketExpenseCategories: List<CategorySlice>
        get() = LedgerAnalyticsEngine.buildCategoryBreakdown(selectedChartBucket?.transactions.orEmpty(), TransactionType.EXPENSE)

    val selectedYearMonthlyTrend: List<MonthlyTrendPoint>
        get() {
            val bucket = selectedChartBucket ?: return emptyList()
            if (chartScope != ChartScope.YEAR) return emptyList()
            bucket.id.toIntOrNull() ?: return emptyList()
            return (1..12).map { month ->
                val monthItems = bucket.transactions.filter { it.date.monthValue == month }
                val income = monthItems.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthItems.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                MonthlyTrendPoint(label = "${month}月", income = income, expense = expense, balance = income - expense)
            }
        }
}

private const val preferencesName = "ledger_preferences"
private const val palettePreferenceKey = "palette_index"
private const val useCustomColorsPreferenceKey = "use_custom_colors"
private const val customIncomeColorPreferenceKey = "custom_income_color"
private const val customExpenseColorPreferenceKey = "custom_expense_color"
private const val customLineColorPreferenceKey = "custom_line_color"
private const val customBackgroundTopPreferenceKey = "custom_background_top"
private const val customBackgroundBottomPreferenceKey = "custom_background_bottom"
private const val exportFolderUriPreferenceKey = "export_folder_uri"
private const val exportFolderLabelPreferenceKey = "export_folder_label"
private const val exportFormatPreferenceKey = "export_format"

class LedgerViewModel(
    private val repository: LedgerRepository,
    private val preferences: SharedPreferences,
    private val databaseFolderPath: String
) : ViewModel() {
    private val formState = MutableStateFlow(
        LedgerUiState(
            paletteIndex = preferences.getInt(palettePreferenceKey, 0).coerceIn(0, palettePresets.lastIndex),
            useCustomColors = preferences.getBoolean(useCustomColorsPreferenceKey, false),
            customIncomeColor = preferences.getLong(customIncomeColorPreferenceKey, solidColorChoices[0]),
            customExpenseColor = preferences.getLong(customExpenseColorPreferenceKey, solidColorChoices[1]),
            customLineColor = preferences.getLong(customLineColorPreferenceKey, solidColorChoices[2]),
            customBackgroundTop = preferences.getLong(customBackgroundTopPreferenceKey, backgroundColorChoices[0]),
            customBackgroundBottom = preferences.getLong(customBackgroundBottomPreferenceKey, backgroundColorChoices[1]),
            databaseFolderPath = databaseFolderPath,
            exportFolderUri = preferences.getString(exportFolderUriPreferenceKey, "").orEmpty(),
            exportFolderLabel = preferences.getString(exportFolderLabelPreferenceKey, "未选择").orEmpty(),
            exportFormat = preferences.getString(exportFormatPreferenceKey, ExportFormat.CSV.name)
                ?.let { value -> ExportFormat.entries.firstOrNull { it.name == value } }
                ?: ExportFormat.CSV
        )
    )
    private val currentDateState = MutableStateFlow(LocalDate.now())
    private val currentDateTimeState = MutableStateFlow(LocalDateTime.now())
    private val transactionsFlow = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val analyticsFlow = combine(transactionsFlow, currentDateState) { transactions, currentDate ->
        LedgerAnalyticsEngine.build(transactions, currentDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LedgerAnalytics.EMPTY)

    val uiState: StateFlow<LedgerUiState> = combine(
        formState,
        currentDateTimeState,
        transactionsFlow,
        analyticsFlow
    ) { form, currentDateTime, transactions, analytics ->
        val categories = if (form.selectedType == TransactionType.INCOME) incomeCategories else expenseCategories
        form.copy(
            currentDateTime = currentDateTime,
            transactions = transactions,
            analytics = analytics,
            databaseFolderPath = databaseFolderPath,
            selectedCategory = if (form.selectedCategory in categories) form.selectedCategory else categories.first()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState(databaseFolderPath = databaseFolderPath)
    )

    init {
        observeClock()
    }

    private fun observeClock() {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                currentDateTimeState.value = now
                if (currentDateState.value != now.toLocalDate()) {
                    currentDateState.value = now.toLocalDate()
                }
                delay(1_000)
            }
        }
    }

    fun updateBottomNavPage(page: BottomNavPage) {
        formState.value = formState.value.copy(bottomNavPage = page)
    }

    fun updateChartScope(scope: ChartScope) {
        formState.value = formState.value.copy(
            chartScope = scope,
            chartSearchInput = "",
            chartSearchMessage = null,
            prioritizedChartBucketId = null,
            selectedChartBucketId = null
        )
    }

    fun updateChartSearchInput(value: String) {
        formState.value = formState.value.copy(chartSearchInput = value, chartSearchMessage = null)
    }

    fun executeChartSearch() {
        val query = formState.value.chartSearchInput.trim()
        val normalized = when (formState.value.chartScope) {
            ChartScope.DAY -> normalizeDayQuery(query)
            ChartScope.MONTH -> normalizeMonthQuery(query)
            ChartScope.YEAR -> normalizeYearQuery(query)
        }
        if (normalized == null) {
            formState.value = formState.value.copy(chartSearchMessage = "输入格式错误", prioritizedChartBucketId = null)
            return
        }
        val availableIds = uiState.value.chartBuckets.map { it.id }.toSet()
        if (normalized !in availableIds) {
            formState.value = formState.value.copy(chartSearchMessage = "数据不存在", prioritizedChartBucketId = null)
            return
        }
        formState.value = formState.value.copy(chartSearchMessage = null, prioritizedChartBucketId = normalized)
    }

    fun openChartBucket(bucketId: String) {
        formState.value = formState.value.copy(selectedChartBucketId = bucketId)
    }

    fun closeChartBucket() {
        formState.value = formState.value.copy(selectedChartBucketId = null)
    }

    fun updatePalette(index: Int) {
        val safeIndex = index.coerceIn(0, palettePresets.lastIndex)
        preferences.edit().putInt(palettePreferenceKey, safeIndex).apply()
        formState.value = formState.value.copy(paletteIndex = safeIndex)
    }

    fun updateCustomColorsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(useCustomColorsPreferenceKey, enabled).apply()
        formState.value = formState.value.copy(useCustomColors = enabled)
    }

    fun updateCustomColor(target: CustomColorTarget, color: Long) {
        when (target) {
            CustomColorTarget.INCOME -> {
                preferences.edit().putLong(customIncomeColorPreferenceKey, color).apply()
                formState.value = formState.value.copy(customIncomeColor = color)
            }
            CustomColorTarget.EXPENSE -> {
                preferences.edit().putLong(customExpenseColorPreferenceKey, color).apply()
                formState.value = formState.value.copy(customExpenseColor = color)
            }
            CustomColorTarget.LINE -> {
                preferences.edit().putLong(customLineColorPreferenceKey, color).apply()
                formState.value = formState.value.copy(customLineColor = color)
            }
            CustomColorTarget.BACKGROUND_TOP -> {
                preferences.edit().putLong(customBackgroundTopPreferenceKey, color).apply()
                formState.value = formState.value.copy(customBackgroundTop = color)
            }
            CustomColorTarget.BACKGROUND_BOTTOM -> {
                preferences.edit().putLong(customBackgroundBottomPreferenceKey, color).apply()
                formState.value = formState.value.copy(customBackgroundBottom = color)
            }
        }
    }

    fun updateExportFolder(uri: String, label: String) {
        preferences.edit()
            .putString(exportFolderUriPreferenceKey, uri)
            .putString(exportFolderLabelPreferenceKey, label)
            .apply()
        formState.value = formState.value.copy(exportFolderUri = uri, exportFolderLabel = label, exportMessage = "已选择导出文件夹")
    }

    fun updateExportFormat(format: ExportFormat) {
        preferences.edit().putString(exportFormatPreferenceKey, format.name).apply()
        formState.value = formState.value.copy(exportFormat = format, exportMessage = null)
    }

    fun exportTransactions(context: Context) {
        val state = uiState.value
        val folderUri = state.exportFolderUri
        if (folderUri.isBlank()) {
            formState.value = formState.value.copy(exportMessage = "请先选择导出文件夹")
            return
        }
        if (state.transactions.isEmpty()) {
            formState.value = formState.value.copy(exportMessage = "暂无可导出的记账数据")
            return
        }
        val transactions = state.transactions.sortedWith(compareBy<TransactionEntity> { it.date }.thenBy { it.id })
        viewModelScope.launch {
            formState.value = formState.value.copy(exportInProgress = true, exportMessage = "正在导出...")
            val message = withContext(Dispatchers.IO) {
                runCatching {
                    val fileName = LedgerExporter.exportToDocumentTree(
                        context = context.applicationContext,
                        treeUri = Uri.parse(folderUri),
                        format = state.exportFormat,
                        transactions = transactions
                    )
                    "已导出：$fileName"
                }.getOrElse {
                    "导出失败：${it.message ?: "无法写入文件"}"
                }
            }
            formState.value = formState.value.copy(exportInProgress = false, exportMessage = message)
        }
    }

    fun updateType(type: TransactionType) {
        formState.value = formState.value.copy(
            selectedType = type,
            selectedCategory = if (type == TransactionType.INCOME) incomeCategories.first() else expenseCategories.first()
        )
    }

    fun updateAmount(value: String) {
        val filtered = buildString {
            var hasDot = false
            value.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !hasDot -> {
                        hasDot = true
                        append(char)
                    }
                }
            }
        }
        formState.value = formState.value.copy(amountInput = filtered)
    }

    fun updateNote(value: String) {
        formState.value = formState.value.copy(noteInput = value)
    }

    fun updateCategory(category: String) {
        formState.value = formState.value.copy(selectedCategory = category)
    }

    fun adjustDate(days: Long) {
        formState.value = formState.value.copy(selectedDate = formState.value.selectedDate.plusDays(days))
    }

    fun saveTransaction() {
        val state = uiState.value
        val amount = state.amountValue ?: return
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = state.selectedType,
                    category = state.selectedCategory,
                    amount = amount,
                    note = state.noteInput.trim(),
                    date = state.selectedDate
                )
            )
            formState.value = formState.value.copy(amountInput = "", noteInput = "", selectedDate = state.currentDate)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

class LedgerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = LedgerRepository(LedgerDatabase.getInstance(context).ledgerDao())
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val databaseFolder = context.getDatabasePath(LedgerDatabase.DATABASE_FILE_NAME).parent
            ?: context.filesDir.absolutePath
        return LedgerViewModel(repository, preferences, databaseFolder) as T
    }
}
