package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SRDarkPrimary,
    onPrimary = SRDarkOnPrimary,
    primaryContainer = SRDarkPrimaryContainer,
    onPrimaryContainer = SRDarkOnPrimaryContainer,
    secondary = SRDarkSecondary,
    onSecondary = SRDarkOnSecondary,
    secondaryContainer = SRDarkSecondaryContainer,
    onSecondaryContainer = SRDarkOnSecondaryContainer,
    tertiary = SRDarkTertiary,
    onTertiary = SRDarkOnTertiary,
    background = SRDarkBackground,
    onBackground = SRDarkOnBackground,
    surface = SRDarkSurface,
    onSurface = SRDarkOnSurface,
    surfaceVariant = SRDarkSurfaceVariant,
    onSurfaceVariant = SRDarkOnSurfaceVariant,
    outline = SRDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SRNavyPrimary,
    onPrimary = SRNavyOnPrimary,
    primaryContainer = SRNavyContainer,
    onPrimaryContainer = SROnNavyContainer,
    secondary = SRGoldSecondary,
    onSecondary = SRGoldOnSecondary,
    secondaryContainer = SRGoldContainer,
    onSecondaryContainer = SROnGoldContainer,
    tertiary = SRAccentTertiary,
    onTertiary = SROnAccentTertiary,
    background = SRLightBackground,
    onBackground = SRLightOnBackground,
    surface = SRLightSurface,
    onSurface = SRLightOnSurface,
    surfaceVariant = SRLightSurfaceVariant,
    onSurfaceVariant = SRLightOnSurfaceVariant,
    outline = SRLightOutline
)

@Composable
fun SRUExamVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
