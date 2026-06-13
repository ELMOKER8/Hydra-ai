package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Cyan80,
    tertiary = IceBlue80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Cyan40,
    tertiary = DeepBlue40,
    background = Color(0xFFF5F9FC),
    surface = Color.White,
    onBackground = Color(0xFF121212),
    onSurface = Color(0xFF121212)
)

// Presets Scheme mappings
private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = IceBlue80,
    background = OceanBackgroundDark,
    surface = OceanSurfaceDark,
    onBackground = Color(0xFFE0F7FA),
    onSurface = Color(0xFFE0F7FA)
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanSecondary,
    secondary = OceanPrimary,
    tertiary = DeepBlue40,
    background = OceanBackgroundLight,
    surface = Color.White,
    onBackground = Color(0xFF00363A),
    onSurface = Color(0xFF00363A)
)

private val IceDarkColorScheme = darkColorScheme(
    primary = IcePrimary,
    secondary = IceSecondary,
    tertiary = Color(0xFFB2EBF2),
    background = IceBackgroundDark,
    surface = IceSurfaceDark,
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1)
)

private val IceLightColorScheme = lightColorScheme(
    primary = IceSecondary,
    secondary = IcePrimary,
    tertiary = IceBackgroundDark,
    background = IceBackgroundLight,
    surface = Color.White,
    onBackground = Color(0xFF002528),
    onSurface = Color(0xFF002528)
)

private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    secondary = MidnightSecondary,
    tertiary = Color(0xFFEA80FC),
    background = MidnightBackground,
    surface = MidnightSurface,
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color(0xFFEDE7F6)
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    secondary = Cyan80,
    tertiary = Color(0xFF80DEEA),
    background = AmoledBackground,
    surface = AmoledSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF161616)
)

@Composable
fun MyApplicationTheme(
    chosenTheme: String = "Ocean Theme",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when (chosenTheme) {
        "Light Mode" -> DefaultLightColorScheme
        "Dark Mode" -> DefaultDarkColorScheme
        "AMOLED Mode" -> AmoledColorScheme
        "Ocean Theme" -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        "Ice Theme" -> if (darkTheme) IceDarkColorScheme else IceLightColorScheme
        "Midnight Theme" -> MidnightColorScheme
        "Dynamic Material You" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
            }
        }
        else -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
