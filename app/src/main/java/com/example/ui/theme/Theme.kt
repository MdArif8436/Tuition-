package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AcademicPrimaryDark, // #77B7D0
    onPrimary = Color(0xFF003544),
    primaryContainer = AcademicPrimaryContainerDark,
    onPrimaryContainer = BrandSoftSky,
    secondary = AcademicSecondaryDark,
    onSecondary = Color(0xFF003544),
    secondaryContainer = AcademicSecondaryContainerDark,
    onSecondaryContainer = BrandSoftSky,
    tertiary = AcademicTertiaryDark, // #EED3CE
    onTertiary = Color(0xFF4B2823),
    tertiaryContainer = AcademicTertiaryContainerDark,
    onTertiaryContainer = BrandWarmRose,
    background = AcademicBackgroundDark,
    onBackground = Color(0xFFE2E8F0),
    surface = AcademicSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = AcademicSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = AcademicPrimary, // #579FB6
    onPrimary = Color.White,
    primaryContainer = AcademicPrimaryContainer, // #B3E0F4
    onPrimaryContainer = Color(0xFF0C3340),
    secondary = AcademicSecondary, // #77B7D0
    onSecondary = Color.White,
    secondaryContainer = BrandWarmRose, // #EED3CE
    onSecondaryContainer = Color(0xFF4A2A25),
    tertiary = AcademicTertiary, // #E2BAB1
    onTertiary = Color(0xFF3E1D18),
    tertiaryContainer = AcademicTertiaryContainer, // #EED3CE
    onTertiaryContainer = Color(0xFF43231F),
    background = AcademicBackgroundLight, // #D9E9F0 balanced soothing mid-tone (not stark white, not dark)
    onBackground = BrandTextPrimary,
    surface = AcademicSurfaceLight, // #E8F2F6 soft tinted container
    onSurface = BrandTextPrimary,
    surfaceVariant = AcademicSurfaceVariantLight, // #CFE3EC
    onSurfaceVariant = BrandTextSecondary,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Always enforce our custom #579FB6 palette
    content: @Composable () -> Unit
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
