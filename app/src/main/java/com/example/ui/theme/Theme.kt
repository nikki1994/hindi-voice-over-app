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
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedOnSecondary,
    background = SophisticatedBackground,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onBackground = SophisticatedOnSurface,
    onSurface = SophisticatedOnSurface,
    onSurfaceVariant = SophisticatedOnSurfaceVariant,
    outline = SophisticatedOutline
  )

private val LightColorScheme = DarkColorScheme // Enforce Sophisticated Dark theme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Dark Theme
  dynamicColor: Boolean = false, // Use our Sophisticated Dark design theme
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
