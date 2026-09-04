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

private val DarkColorScheme =
  darkColorScheme(
    primary = NovaAccent,
    onPrimary = Color.White,
    primaryContainer = NovaDarkSurfaceVariant,
    onPrimaryContainer = NovaDarkTextPrimary,
    secondary = NovaPrimaryLight,
    onSecondary = NovaDarkBackground,
    background = NovaDarkBackground,
    onBackground = NovaDarkTextPrimary,
    surface = NovaDarkSurface,
    onSurface = NovaDarkTextPrimary,
    surfaceVariant = NovaDarkSurfaceVariant,
    onSurfaceVariant = NovaDarkTextSecondary,
    outline = NovaDarkBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NovaPrimary,
    onPrimary = Color.White,
    primaryContainer = NovaPrimaryLight,
    onPrimaryContainer = NovaOnPrimaryContainer,
    secondary = NovaAccent,
    onSecondary = NovaOnPrimaryContainer,
    secondaryContainer = NovaNavPill,
    onSecondaryContainer = NovaTextPrimary,
    background = NovaBackground,
    onBackground = NovaTextPrimary,
    surface = NovaSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceVariant,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaBorder,
    outlineVariant = NovaBorderSubtle,
    error = NovaError,
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature NOVA-SEND palette
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

