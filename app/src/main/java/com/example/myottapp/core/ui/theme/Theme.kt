package com.example.myottapp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun OttAppTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary      = OttBrand,      // ✅ FIXED: was OttRed, now orange
        onPrimary    = OttWhite,
        secondary    = OttBrandDark,  // ✅ FIXED: was OttRedDark
        background   = OttBlack,
        surface      = OttDark,
        onBackground = OttWhite,
        onSurface    = OttOffWhite
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content     = content
    )
}