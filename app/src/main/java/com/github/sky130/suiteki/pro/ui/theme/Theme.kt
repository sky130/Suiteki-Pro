package com.github.sky130.suiteki.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun SuitekiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    DynamicMaterialTheme(
        seedColor = SuitekiColor,
        useDarkTheme = darkTheme,
        content = content,
        contrastLevel = 0.05
    )
}
