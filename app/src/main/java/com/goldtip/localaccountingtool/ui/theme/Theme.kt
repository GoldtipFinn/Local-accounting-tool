package com.goldtip.localaccountingtool.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFFE76F51),
    tertiary = Color(0xFF264653),
    background = Color(0xFFF8FAFC),
    surface = Color.White
)

@Composable
fun LocalAccountingToolTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightScheme,
        typography = LedgerTypography,
        content = content
    )
}
