package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AccentTheme
import com.example.model.ThemeMode

@Composable
fun OmniSocialTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentTheme: AccentTheme = AccentTheme.CLASSIC_SOCIAL,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.PURE_AMOLED -> true
        ThemeMode.MIDNIGHT -> true
        ThemeMode.SOFT_DARK -> true
        ThemeMode.LIGHT -> false
    }

    val primaryColor = when (accentTheme) {
        AccentTheme.CLASSIC_SOCIAL -> AccentIndigo
        AccentTheme.EMERALD -> AccentEmerald
        AccentTheme.CYBER_PINK -> AccentRose
        AccentTheme.OCEAN_BLUE -> AccentBlue
        AccentTheme.PURPLE -> AccentPurple
        AccentTheme.AMBER -> AccentAmber
    }

    val colorScheme: ColorScheme = if (isDark) {
        when (themeMode) {
            ThemeMode.PURE_AMOLED -> {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    primaryContainer = primaryColor.copy(alpha = 0.25f),
                    onPrimaryContainer = primaryColor,
                    background = AmoledBlack,
                    onBackground = if (highContrast) Color.White else Color(0xFFF0F0F0),
                    surface = AmoledSurface,
                    onSurface = if (highContrast) Color.White else Color(0xFFEEEEEE),
                    surfaceVariant = AmoledCard,
                    onSurfaceVariant = if (highContrast) Color(0xFFE0E0E0) else Color(0xFFB0B0B8),
                    outline = if (highContrast) Color(0xFF666677) else AmoledBorder
                )
            }
            ThemeMode.MIDNIGHT -> {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    background = MidnightBackground,
                    onBackground = Color(0xFFE2E8F0),
                    surface = MidnightSurface,
                    onSurface = Color(0xFFF1F5F9),
                    surfaceVariant = MidnightCard,
                    onSurfaceVariant = Color(0xFF94A3B8),
                    outline = Color(0xFF334155)
                )
            }
            else -> {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    background = SoftDarkBackground,
                    onBackground = Color(0xFFEDEDF2),
                    surface = SoftDarkSurface,
                    onSurface = Color(0xFFEDEDF2),
                    surfaceVariant = SoftDarkCard,
                    onSurfaceVariant = Color(0xFFA1A1AA),
                    outline = Color(0xFF2E2E38)
                )
            }
        }
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightCard,
            onSurfaceVariant = Color(0xFF475569),
            outline = LightBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
