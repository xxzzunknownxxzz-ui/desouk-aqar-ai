package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DesoukNavyDark,
    onPrimary = Color.White,
    primaryContainer = DesoukNavyLight,
    onPrimaryContainer = Color.White,
    secondary = DesoukGold,
    onSecondary = DesoukNavyDark,
    secondaryContainer = DesoukGoldSoft,
    onSecondaryContainer = DesoukGoldDark,
    tertiary = DesoukGoldDark,
    onTertiary = Color.White,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = Color(0xFFE2E8F0),
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorBg,
    onErrorContainer = StatusError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // نلتزم بهوية التطبيق العقاري الموحدة بدقة
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
