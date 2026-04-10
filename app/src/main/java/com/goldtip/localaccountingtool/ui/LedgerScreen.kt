package com.goldtip.localaccountingtool.ui

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goldtip.localaccountingtool.data.TransactionEntity
import com.goldtip.localaccountingtool.data.TransactionType
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA)
private val detailDateFormatter = DateTimeFormatter.ofPattern("yyyy\u5e74 M\u6708 d\u65e5", Locale.CHINA)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.CHINA)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

@Composable
fun LedgerApp(viewModel: LedgerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
            viewModel.updateExportFolder(uri.toString(), uri.toReadableFolderLabel())
        }
    }
    val palette = state.activePalette
    val incomeColor = Color(palette.incomeColor)
    val expenseColor = Color(palette.expenseColor)
    val accentColor = Color(palette.accentColor)
    val lineColor = Color(palette.lineColor)
    val backgroundBrush = Brush.verticalGradient(
        listOf(Color(palette.backgroundTop), Color(palette.backgroundBottom))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            if (state.bottomNavPage == BottomNavPage.CHART) {
                ChartTopBar(
                    state = state,
                    accentColor = accentColor,
                    onScopeChange = viewModel::updateChartScope
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                selected = state.bottomNavPage,
                accentColor = accentColor,
                onSelect = viewModel::updateBottomNavPage
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            when (state.bottomNavPage) {
                BottomNavPage.ENTRY -> EntryPage(
                    state = state,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    accentColor = accentColor,
                    onTypeChange = viewModel::updateType,
                    onAmountChange = viewModel::updateAmount,
                    onNoteChange = viewModel::updateNote,
                    onCategoryChange = viewModel::updateCategory,
                    onDateAdjust = viewModel::adjustDate,
                    onSave = viewModel::saveTransaction
                )

                BottomNavPage.DETAIL -> DetailPage(
                    state = state,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    onDelete = viewModel::deleteTransaction
                )

                BottomNavPage.CHART -> ChartPage(
                    state = state,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    accentColor = accentColor,
                    lineColor = lineColor,
                    onChartSearchInputChange = viewModel::updateChartSearchInput,
                    onExecuteChartSearch = viewModel::executeChartSearch,
                    onOpenBucket = viewModel::openChartBucket,
                    onCloseBucket = viewModel::closeChartBucket
                )

                BottomNavPage.SETTINGS -> SettingsPage(
                    state = state,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor,
                    accentColor = accentColor,
                    lineColor = lineColor,
                    onPickExportFolder = { folderPickerLauncher.launch(null) },
                    onExportFormatChange = viewModel::updateExportFormat,
                    onExport = { viewModel.exportTransactions(context.applicationContext) },
                    onPaletteChange = viewModel::updatePalette,
                    onCustomColorsEnabledChange = viewModel::updateCustomColorsEnabled,
                    onCustomColorChange = viewModel::updateCustomColor
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    selected: BottomNavPage,
    accentColor: Color,
    onSelect: (BottomNavPage) -> Unit
) {
    NavigationBar(containerColor = Color.White.copy(alpha = 0.96f)) {
        val items = listOf(
            Triple(BottomNavPage.ENTRY, Icons.Outlined.EditNote, "\u8bb0\u8d26"),
            Triple(BottomNavPage.DETAIL, Icons.Outlined.Subject, "\u660e\u7ec6"),
            Triple(BottomNavPage.CHART, Icons.Outlined.Analytics, "\u56fe\u8868"),
            Triple(BottomNavPage.SETTINGS, Icons.Outlined.Settings, "\u8bbe\u7f6e")
        )
        items.forEach { (page, icon, label) ->
            NavigationBarItem(
                selected = selected == page,
                onClick = { onSelect(page) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor,
                    indicatorColor = accentColor.copy(alpha = 0.14f)
                )
            )
        }
    }
}

@Composable
private fun ChartTopBar(
    state: LedgerUiState,
    accentColor: Color,
    onScopeChange: (ChartScope) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "\u56fe\u8868\u4e2d\u5fc3", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "\u624b\u673a\u65f6\u95f4 ${state.currentDateTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = state.currentDateTime.toLocalDate().format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChartScope.entries.forEach { scope ->
                    FilterChip(
                        selected = state.chartScope == scope,
                        onClick = { onScopeChange(scope) },
                        label = { Text(scope.label) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.16f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryPage(
    state: LedgerUiState,
    incomeColor: Color,
    expenseColor: Color,
    accentColor: Color,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDateAdjust: (Long) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EntryHeroCard(state, incomeColor, expenseColor, accentColor) }
        item {
            EntryCard(
                state = state,
                accentColor = accentColor,
                expenseColor = expenseColor,
                incomeColor = incomeColor,
                onTypeChange = onTypeChange,
                onAmountChange = onAmountChange,
                onNoteChange = onNoteChange,
                onCategoryChange = onCategoryChange,
                onDateAdjust = onDateAdjust,
                onSave = onSave
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "\u4eca\u65e5\u6536\u5165", state.todayIncome.currency(), incomeColor)
                StatCard(Modifier.weight(1f), "\u4eca\u65e5\u652f\u51fa", state.todayExpense.currency(), expenseColor)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "\u672c\u6708\u65e5\u5747", state.averageExpensePerDay.shortCurrency(), accentColor)
                StatCard(Modifier.weight(1f), "\u652f\u51fa\u91cd\u70b9", state.highestExpenseCategory, expenseColor)
            }
        }
        item {
            Text(text = "\u6700\u8fd1\u8bb0\u5f55", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (state.recentTransactions.isEmpty()) {
            item { EmptyCard("\u8fd8\u6ca1\u6709\u8d26\u5355\uff0c\u5148\u8bb0\u4e0b\u7b2c\u4e00\u7b14\u3002") }
        } else {
            items(state.recentTransactions, key = { it.id }) { transaction ->
                RecentTransactionCard(transaction, incomeColor, expenseColor)
            }
        }
    }
}

@Composable
private fun EntryHeroCard(
    state: LedgerUiState,
    incomeColor: Color,
    expenseColor: Color,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(accentColor, incomeColor.copy(alpha = 0.85f), expenseColor.copy(alpha = 0.78f))
                    )
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "\u8bb0\u8d26\u9996\u9875", color = Color.White.copy(alpha = 0.72f))
                Text(
                    text = "\u7d2f\u8ba1\u7ed3\u4f59",
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = state.balance.currency(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "\u624b\u673a\u65f6\u95f4 ${state.currentDateTime.format(dateTimeFormatter)}",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniHeroPill(Modifier.weight(1f), "\u672c\u6708\u6536\u5165", state.monthIncome.currency())
                    MiniHeroPill(Modifier.weight(1f), "\u672c\u6708\u652f\u51fa", state.monthExpense.currency())
                }
            }
        }
    }
}

@Composable
private fun MiniHeroPill(modifier: Modifier, title: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text = title, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium)
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntryCard(
    state: LedgerUiState,
    accentColor: Color,
    expenseColor: Color,
    incomeColor: Color,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDateAdjust: (Long) -> Unit,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = "\u5feb\u901f\u8bb0\u4e00\u7b14", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = state.selectedType == TransactionType.EXPENSE,
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    label = { Text("\u652f\u51fa") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = expenseColor.copy(alpha = 0.14f))
                )
                FilterChip(
                    selected = state.selectedType == TransactionType.INCOME,
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    label = { Text("\u6536\u5165") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = incomeColor.copy(alpha = 0.14f))
                )
            }
            OutlinedTextField(
                value = state.amountInput,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text("\u91d1\u989d") },
                placeholder = { Text("\u4f8b\u5982 188.00") }
            )
            OutlinedTextField(
                value = state.noteInput,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("\u8bf4\u660e") },
                placeholder = { Text("\u8f93\u5165\u6765\u6e90\u3001\u573a\u666f\u6216\u5907\u6ce8") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = state.selectedDate.format(dateFormatter), fontWeight = FontWeight.Medium)
                Row {
                    IconButton(onClick = { onDateAdjust(-1) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "\u524d\u4e00\u5929")
                    }
                    IconButton(onClick = { onDateAdjust(1) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "\u540e\u4e00\u5929")
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.currentCategories.forEach { category ->
                    AssistChip(
                        onClick = { onCategoryChange(category) },
                        label = { Text(category) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedCategory == category) {
                                accentColor.copy(alpha = 0.16f)
                            } else {
                                Color(0xFFF3F4F6)
                            }
                        )
                    )
                }
            }
            Button(onClick = onSave, enabled = state.canSave, modifier = Modifier.fillMaxWidth()) {
                Text("\u4fdd\u5b58\u8fd9\u7b14\u8d26")
            }
        }
    }
}

@Composable
private fun DetailPage(
    state: LedgerUiState,
    incomeColor: Color,
    expenseColor: Color,
    onDelete: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "\u6bcf\u65e5\u660e\u7ec6", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (state.detailSections.isEmpty()) {
            item { EmptyCard("\u8fd8\u6ca1\u6709\u8bb0\u5f55\uff0c\u4f60\u7684\u6bcf\u5929\u8d26\u5355\u4f1a\u6309\u65e5\u5206\u533a\u5c55\u793a\u5728\u8fd9\u91cc\u3002") }
        } else {
            items(state.detailSections, key = { it.date.toString() }) { section ->
                DaySectionCard(section, incomeColor, expenseColor, onDelete)
            }
        }
    }
}

@Composable
private fun DaySectionCard(
    section: DaySection,
    incomeColor: Color,
    expenseColor: Color,
    onDelete: (TransactionEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = section.date.format(detailDateFormatter),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4B5563)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "\u6536 ${section.income.currency()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = incomeColor
                    )
                    Text(
                        text = "\u652f ${section.expense.currency()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = expenseColor
                    )
                }
            }
            section.items.forEach { transaction ->
                DetailRow(transaction, incomeColor, expenseColor, onDelete)
            }
        }
    }
}

@Composable
private fun DetailRow(
    transaction: TransactionEntity,
    incomeColor: Color,
    expenseColor: Color,
    onDelete: (TransactionEntity) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = transaction.category, fontWeight = FontWeight.Bold)
            Text(
                text = if (transaction.note.isBlank()) "\u65e0\u5907\u6ce8" else transaction.note,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = (if (transaction.type == TransactionType.INCOME) "+" else "-") + transaction.amount.currency(),
            color = if (transaction.type == TransactionType.INCOME) incomeColor else expenseColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "\u5220\u9664",
            color = Color(0xFF9CA3AF),
            modifier = Modifier.clickable { onDelete(transaction) }
        )
    }
}

@Composable
private fun ChartPage(
    state: LedgerUiState,
    incomeColor: Color,
    expenseColor: Color,
    accentColor: Color,
    lineColor: Color,
    onChartSearchInputChange: (String) -> Unit,
    onExecuteChartSearch: () -> Unit,
    onOpenBucket: (String) -> Unit,
    onCloseBucket: () -> Unit
) {
    if (state.selectedChartBucket == null && state.chartBuckets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            EmptyCard("\u6682\u65f6\u8fd8\u6ca1\u6709\u56fe\u8868\u6570\u636e\u3002")
        }
        return
    }

    if (state.selectedChartBucketId == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ChartSearchCard(
                    scope = state.chartScope,
                    value = state.chartSearchInput,
                    message = state.chartSearchMessage,
                    accentColor = accentColor,
                    onValueChange = onChartSearchInputChange,
                    onSearch = onExecuteChartSearch
                )
            }
            items(state.chartBuckets, key = { it.id }) { bucket ->
                ChartBucketCard(bucket, incomeColor, expenseColor, onOpenBucket)
            }
        }
    } else {
        val bucket = state.selectedChartBucket ?: return
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCloseBucket) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "\u8fd4\u56de")
                    }
                    Column {
                        Text(text = bucket.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "\u53ef\u89c6\u5316\u8be6\u60c5", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "\u6536\u5165", bucket.income.currency(), incomeColor, compact = true)
                    StatCard(Modifier.weight(1f), "\u652f\u51fa", bucket.expense.currency(), expenseColor, compact = true)
                    StatCard(Modifier.weight(1f), "\u5dee\u503c", (bucket.income - bucket.expense).currency(), accentColor, compact = true)
                }
            }
            item {
                CategoryDualSection(
                    incomeSlices = state.selectedBucketIncomeCategories,
                    expenseSlices = state.selectedBucketExpenseCategories,
                    incomeColor = incomeColor,
                    expenseColor = expenseColor
                )
            }
            if (state.chartScope == ChartScope.YEAR) {
                item {
                    YearHybridChartCard(
                        title = "\u672c\u5e74 12 \u4e2a\u6708\u6536\u652f\u5dee\u503c",
                        points = state.selectedYearMonthlyTrend,
                        incomeColor = incomeColor,
                        expenseColor = expenseColor,
                        lineColor = lineColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartBucketCard(
    bucket: ChartBucket,
    incomeColor: Color,
    expenseColor: Color,
    onOpenBucket: (String) -> Unit
) {
    val total = (bucket.income + bucket.expense).coerceAtLeast(1.0)
    val incomeRatio = (bucket.income / total).toFloat()
    val expenseRatio = (bucket.expense / total).toFloat()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        modifier = Modifier.clickable { onOpenBucket(bucket.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(118.dp)) {
                Text(text = bucket.label, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, fontSize = 12.sp)
                Text(text = bucket.trailingLabel, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, softWrap = false, fontSize = 11.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .clip(RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(incomeRatio.coerceAtLeast(0.06f))
                            .fillMaxSize()
                            .background(incomeColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(expenseRatio.coerceAtLeast(0.06f))
                            .fillMaxSize()
                            .background(expenseColor)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "\u6536 ${bucket.income.currency()}", style = MaterialTheme.typography.bodySmall, color = incomeColor, maxLines = 1, softWrap = false, fontSize = 10.sp)
                    Text(text = "\u652f ${bucket.expense.currency()}", style = MaterialTheme.typography.bodySmall, color = expenseColor, maxLines = 1, softWrap = false, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ChartSearchCard(
    scope: ChartScope,
    value: String,
    message: String?,
    accentColor: Color,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val placeholder = when (scope) {
        ChartScope.DAY -> "20230213"
        ChartScope.MONTH -> "202412"
        ChartScope.YEAR -> "2023"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("\u8f93\u5165${scope.label}\u671f\u4fe1\u606f") },
                    placeholder = { Text(placeholder) }
                )
                Button(onClick = onSearch) {
                    Text(text = "\u67e5\u8be2", color = Color.White, maxLines = 1, softWrap = false)
                }
            }
            if (message != null) {
                Text(text = message, color = Color(0xFFB42318), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsPage(
    state: LedgerUiState,
    incomeColor: Color,
    expenseColor: Color,
    accentColor: Color,
    lineColor: Color,
    onPickExportFolder: () -> Unit,
    onExportFormatChange: (ExportFormat) -> Unit,
    onExport: () -> Unit,
    onPaletteChange: (Int) -> Unit,
    onCustomColorsEnabledChange: (Boolean) -> Unit,
    onCustomColorChange: (CustomColorTarget, Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "\u8bbe\u7f6e", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "\u672c\u5730\u6570\u636e\u5bfc\u51fa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "\u5e94\u7528\u6570\u636e\u5e93\u4f4d\u7f6e", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text(text = state.databaseFolderPath, color = accentColor)
                    Text(
                        text = "\u8fd0\u884c\u4e2d\u7684 Room \u6570\u636e\u5e93\u4ecd\u4fdd\u5b58\u5728\u7cfb\u7edf\u79c1\u6709\u76ee\u5f55\u3002\u4e0b\u65b9\u53ef\u9009\u62e9\u4f60\u81ea\u5df1\u7684\u6587\u4ef6\u5939\uff0c\u5c06\u5168\u90e8\u8bb0\u8d26\u6570\u636e\u5bfc\u51fa\u5230\u672c\u5730\u3002",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(text = "\u5bfc\u51fa\u6587\u4ef6\u5939", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text(text = state.exportFolderLabel, color = accentColor)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "\u5bfc\u51fa\u683c\u5f0f", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        ExportFormat.entries.forEach { format ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (state.exportFormat == format) accentColor.copy(alpha = 0.10f) else Color(0xFFF8FAFC))
                                    .clickable { onExportFormatChange(format) }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = format.label, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = format.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (state.exportFormat == format) {
                                    Text(text = "\u5f53\u524d", color = accentColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onPickExportFolder,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("\u9009\u62e9\u6587\u4ef6\u5939")
                        }
                        Button(
                            onClick = onExport,
                            enabled = !state.exportInProgress,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (state.exportInProgress) "\u5bfc\u51fa\u4e2d..." else "\u7acb\u5373\u5bfc\u51fa")
                        }
                    }
                    state.exportMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.startsWith("\u5df2\u5bfc\u51fa") || message.startsWith("\u5df2\u9009\u62e9")) incomeColor else expenseColor
                        )
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "\u9884\u8bbe\u914d\u8272", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    palettePresets.forEachIndexed { index, preset ->
                        val selected = index == state.paletteIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) accentColor.copy(alpha = 0.10f) else Color(0xFFF8FAFC))
                                .clickable { onPaletteChange(index) }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = preset.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (selected) "\u5f53\u524d\u4f7f\u7528\u4e2d" else "\u70b9\u51fb\u5207\u6362",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ColorDot(Color(preset.incomeColor))
                                ColorDot(Color(preset.expenseColor))
                                ColorDot(Color(preset.lineColor))
                                ColorDot(Color(preset.accentColor))
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "\u5f53\u524d\u6536\u5165\u8272", "\u793a\u4f8b", incomeColor)
                        StatCard(Modifier.weight(1f), "\u5f53\u524d\u652f\u51fa\u8272", "\u793a\u4f8b", expenseColor)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(text = "\u72ec\u7acb\u7ec6\u8c03\u914d\u8272", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !state.useCustomColors,
                            onClick = { onCustomColorsEnabledChange(false) },
                            label = { Text("\u4f7f\u7528\u9884\u8bbe") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = state.useCustomColors,
                            onClick = { onCustomColorsEnabledChange(true) },
                            label = { Text("\u542f\u7528\u7ec6\u8c03") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.16f)
                            )
                        )
                    }
                    Text(
                        text = if (state.useCustomColors) "\u5df2\u542f\u7528\u72ec\u7acb\u989c\u8272\u8bbe\u5b9a" else "\u5f53\u524d\u4ecd\u4f7f\u7528\u9884\u8bbe\u914d\u8272",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    CustomColorChooser(
                        title = "\u6536\u5165\u8272",
                        selectedColor = state.customIncomeColor,
                        choices = solidColorChoices,
                        onSelect = { onCustomColorChange(CustomColorTarget.INCOME, it) }
                    )
                    CustomColorChooser(
                        title = "\u652f\u51fa\u8272",
                        selectedColor = state.customExpenseColor,
                        choices = solidColorChoices,
                        onSelect = { onCustomColorChange(CustomColorTarget.EXPENSE, it) }
                    )
                    CustomColorChooser(
                        title = "\u6298\u7ebf\u8272",
                        selectedColor = state.customLineColor,
                        choices = solidColorChoices,
                        onSelect = { onCustomColorChange(CustomColorTarget.LINE, it) }
                    )
                    CustomColorChooser(
                        title = "\u80cc\u666f\u4e0a\u6bb5",
                        selectedColor = state.customBackgroundTop,
                        choices = backgroundColorChoices,
                        onSelect = { onCustomColorChange(CustomColorTarget.BACKGROUND_TOP, it) }
                    )
                    CustomColorChooser(
                        title = "\u80cc\u666f\u4e0b\u6bb5",
                        selectedColor = state.customBackgroundBottom,
                        choices = backgroundColorChoices,
                        onSelect = { onCustomColorChange(CustomColorTarget.BACKGROUND_BOTTOM, it) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "\u5f53\u524d\u6298\u7ebf\u8272", "\u793a\u4f8b", lineColor)
                        StatCard(Modifier.weight(1f), "\u4e3b\u9898\u8272", "\u793a\u4f8b", accentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun CustomColorChooser(
    title: String,
    selectedColor: Long,
    choices: List<Long>,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            choices.forEach { colorValue ->
                val color = Color(colorValue)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == colorValue) 3.dp else 1.dp,
                            color = if (selectedColor == colorValue) Color.Black.copy(alpha = 0.75f) else Color.White,
                            shape = CircleShape
                        )
                        .clickable { onSelect(colorValue) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    color: Color,
    compact: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 10.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 1,
                softWrap = false,
                fontSize = if (compact) 10.sp else 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                fontSize = if (compact) 12.sp else 15.sp
            )
        }
    }
}

@Composable
private fun RecentTransactionCard(transaction: TransactionEntity, incomeColor: Color, expenseColor: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = transaction.category, fontWeight = FontWeight.Bold)
                Text(text = transaction.date.format(dateFormatter), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = (if (transaction.type == TransactionType.INCOME) "+" else "-") + transaction.amount.currency(),
                color = if (transaction.type == TransactionType.INCOME) incomeColor else expenseColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f))) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategoryDualSection(
    incomeSlices: List<CategorySlice>,
    expenseSlices: List<CategorySlice>,
    incomeColor: Color,
    expenseColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CategorySplitPieCard(
            incomeSlices = incomeSlices,
            expenseSlices = expenseSlices,
            incomeColor = incomeColor,
            expenseColor = expenseColor
        )
        CategoryBarsCard("\u6536\u5165\u5206\u7c7b", incomeSlices, incomeColor)
        CategoryBarsCard("\u652f\u51fa\u5206\u7c7b", expenseSlices, expenseColor)
    }
}

@Composable
private fun CategorySplitPieCard(
    incomeSlices: List<CategorySlice>,
    expenseSlices: List<CategorySlice>,
    incomeColor: Color,
    expenseColor: Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "\u7efc\u5408\u6536\u652f\u5206\u7c7b", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SinglePiePane(
                    modifier = Modifier.weight(1f),
                    title = "\u6536\u5165",
                    slices = incomeSlices,
                    colors = warmPalette(incomeColor),
                    emptyText = "\u6682\u65e0\u6536\u5165"
                )
                SinglePiePane(
                    modifier = Modifier.weight(1f),
                    title = "\u652f\u51fa",
                    slices = expenseSlices,
                    colors = coolPalette(expenseColor),
                    emptyText = "\u6682\u65e0\u652f\u51fa"
                )
            }
        }
    }
}

@Composable
private fun SinglePiePane(
    modifier: Modifier,
    title: String,
    slices: List<CategorySlice>,
    colors: List<Color>,
    emptyText: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (slices.isEmpty()) {
            Text(text = emptyText, color = Color.Gray)
        } else {
            FlatPieChart(slices = slices, colors = colors)
        }
    }
}

@Composable
private fun CategoryBarsCard(title: String, slices: List<CategorySlice>, color: Color) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (slices.isEmpty()) {
                Text(text = "\u6682\u65e0\u5bf9\u5e94\u6570\u636e", color = Color.Gray)
            } else {
                slices.forEach { slice ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = slice.category, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${slice.amount.currency()}  ${slice.ratio.percent()}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF3F4F6))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(slice.ratio.toFloat().coerceAtLeast(0.08f))
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatPieChart(
    slices: List<CategorySlice>,
    colors: List<Color>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(136.dp)) {
                var startAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweep = (slice.ratio * 360f).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }
        }
        slices.forEachIndexed { index, slice ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slice.category.removePrefix("\u6536 ").removePrefix("\u652f "),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(text = slice.ratio.percent(), color = Color.Gray, maxLines = 1)
            }
        }
    }
}

private fun warmPalette(baseColor: Color): List<Color> = listOf(
    baseColor,
    lerp(baseColor, Color(0xFFFFD166), 0.28f),
    lerp(baseColor, Color(0xFFFFA94D), 0.18f),
    lerp(baseColor, Color.White, 0.22f),
    Color(0xFFF4A261),
    Color(0xFFE9C46A),
    Color(0xFFFFB703)
)

private fun coolPalette(baseColor: Color): List<Color> = listOf(
    baseColor,
    lerp(baseColor, Color(0xFF4D96FF), 0.30f),
    lerp(baseColor, Color(0xFF90CAF9), 0.20f),
    lerp(baseColor, Color.White, 0.18f),
    Color(0xFF457B9D),
    Color(0xFF5C7AEA),
    Color(0xFF6C91C2)
)

@Composable
private fun YearHybridChartCard(
    title: String,
    points: List<MonthlyTrendPoint>,
    incomeColor: Color,
    expenseColor: Color,
    lineColor: Color
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (points.isEmpty()) {
                Text(text = "\u8fd8\u6ca1\u6709\u5e74\u5ea6\u8d8b\u52bf\u6570\u636e", color = Color.Gray)
            } else {
                YearHybridChart(points, incomeColor, expenseColor, lineColor)
            }
        }
    }
}

@Composable
private fun YearHybridChart(
    points: List<MonthlyTrendPoint>,
    incomeColor: Color,
    expenseColor: Color,
    lineColor: Color
) {
    val maxAbs = points.maxOfOrNull { maxOf(it.income, it.expense, it.balance.absoluteValue) }?.coerceAtLeast(1.0) ?: 1.0
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            val horizontalPadding = 24.dp.toPx()
            val chartWidth = size.width - horizontalPadding * 2
            val topPadding = 42.dp.toPx()
            val bottomPadding = 44.dp.toPx()
            val centerY = (size.height - bottomPadding + topPadding) / 2
            val halfHeight = (size.height - topPadding - bottomPadding) / 2
            val stepX = chartWidth / points.size
            val barWidth = stepX * 0.52f

            drawLine(
                color = Color(0xFFD1D5DB),
                start = Offset(horizontalPadding, centerY),
                end = Offset(size.width - horizontalPadding, centerY),
                strokeWidth = 2f
            )

            val balancePath = Path()
            points.forEachIndexed { index, point ->
                val xCenter = horizontalPadding + stepX * index + stepX / 2
                val incomeHeight = (point.income / maxAbs).toFloat() * halfHeight
                val expenseHeight = (point.expense / maxAbs).toFloat() * halfHeight
                val balanceY = centerY - (point.balance / maxAbs).toFloat() * halfHeight
                val labelYBottom = size.height - 10.dp.toPx()

                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(xCenter, topPadding),
                    end = Offset(xCenter, size.height - bottomPadding),
                    strokeWidth = 1f
                )

                drawRoundRect(
                    color = incomeColor.copy(alpha = 0.72f),
                    topLeft = Offset(xCenter - barWidth / 2, centerY - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
                drawRoundRect(
                    color = expenseColor.copy(alpha = 0.72f),
                    topLeft = Offset(xCenter - barWidth / 2, centerY),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                if (index == 0) balancePath.moveTo(xCenter, balanceY) else balancePath.lineTo(xCenter, balanceY)
                drawCircle(color = lineColor, radius = 6f, center = Offset(xCenter, balanceY))
                drawCircle(color = Color.White, radius = 3f, center = Offset(xCenter, balanceY))

                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    val monthPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val incomePaint = android.graphics.Paint().apply {
                        color = incomeColor.toArgb()
                        textSize = 18f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    val expensePaint = android.graphics.Paint().apply {
                        color = expenseColor.toArgb()
                        textSize = 18f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    nativeCanvas.drawText(point.label, xCenter, labelYBottom, monthPaint)
                    nativeCanvas.drawText(point.income.noSymbolShort(), xCenter, centerY - incomeHeight - 8.dp.toPx(), incomePaint)
                    nativeCanvas.drawText(point.expense.noSymbolShort(), xCenter, centerY + expenseHeight + 22.dp.toPx(), expensePaint)
                }
            }
            drawPath(balancePath, color = lineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Legend("\u6536\u5165\u6b63\u8f74", incomeColor)
            Legend("\u652f\u51fa\u8d1f\u8f74", expenseColor)
            Legend("\u5dee\u503c\u6298\u7ebf", lineColor)
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun android.net.Uri.toReadableFolderLabel(): String {
    val treeId = runCatching { DocumentsContract.getTreeDocumentId(this) }.getOrNull().orEmpty()
    if (treeId.isBlank()) return toString()
    return treeId.replace("primary:", "\u5185\u90e8\u5b58\u50a8/").replace(':', '/')
}
