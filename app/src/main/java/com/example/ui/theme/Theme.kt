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

private val DarkColorScheme =
  darkColorScheme(
    primary = SaffronDarkTheme,
    secondary = GoldDarkTheme,
    tertiary = GoldLight,
    background = SpiritualDarkBg,
    surface = SpiritualDarkSurface,
    onPrimary = HolyIvory,
    onSecondary = SpiritualDarkBg,
    onBackground = HolyIvory,
    onSurface = HolyIvory
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Saffron,
    secondary = Gold,
    tertiary = GoldDark,
    background = HolyIvory,
    surface = WarmCream,
    onPrimary = HolyIvory,
    onSecondary = DeepText,
    onBackground = DeepText,
    onSurface = DeepText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set false to prioritize Saffron & Gold traditional branding
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
