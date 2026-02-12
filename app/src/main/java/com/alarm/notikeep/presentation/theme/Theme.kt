package com.alarm.notikeep.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = White,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = SkyBlueDark,

    secondary = SkyBlueDark,
    onSecondary = White,

    background = WhiteSmoke,
    onBackground = Gray800,

    surface = White,
    onSurface = Gray800,
    onSurfaceVariant = Gray600,

    outline = Gray400
)

@Composable
fun NotiKeepTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
