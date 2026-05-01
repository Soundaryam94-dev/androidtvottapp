package com.example.myottapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object OttColors {
    // Backgrounds
    val BgBase        = Color(0xFF0A0A0C)
    val BgSurface     = Color(0xFF141416)
    val BgElevated    = Color(0xFF1C1C20)
    val BgOverlay     = Color(0x99000000)

    // Accent — orange brand color
    val Accent        = Color(0xFFFF6A00)
    val AccentBright  = Color(0xFFFF8C3A)
    val AccentDim     = Color(0x33FF6A00)
    val AccentBorder  = Color(0x80FF6A00)

    // Text
    val TextPrimary   = Color(0xFFF5F3EF)
    val TextSecondary = Color(0xFF9C9890)
    val TextTertiary  = Color(0xFF5C5A56)
    val TextOnAccent  = Color(0xFFFFFFFF)

    // Semantic
    val Gold          = Color(0xFFFFBB00)
    val Success       = Color(0xFF2ECC71)
    val White10       = Color(0x1AFFFFFF)
    val White20       = Color(0x33FFFFFF)
    val White40       = Color(0x66FFFFFF)
    val Black80       = Color(0xCC000000)

    // ✅ Added — used by Bg references in HomeScreen
    val Bg            = Color(0xFF141414)
}

object OttSpacing {
    val XS  = 4.dp
    val SM  = 8.dp
    val MD  = 16.dp
    val LG  = 24.dp
    val XL  = 32.dp
    val XXL = 48.dp

    val ScreenEdge = 48.dp
    val SidebarWidth = 80.dp
    val HeroHeight = 500.dp     // ✅ FIXED: was 420, now matches HERO_HEIGHT

    // ✅ FIXED: portrait card dimensions — matches MovieCard.kt actual usage
    val CardWidth       = 150.dp   // was 180dp landscape — wrong
    val CardHeight      = 215.dp   // was 108dp landscape — wrong
    val CardWidthLarge  = 200.dp   // continue watching card
    val CardHeightLarge = 130.dp   // continue watching card
}

object OttTypography {
    val HeroTitle = TextStyle(
        fontSize     = 52.sp,
        fontWeight   = FontWeight.Black,
        letterSpacing = (-0.5).sp,
        lineHeight   = 56.sp
    )
    val HeroTagline = TextStyle(
        fontSize     = 15.sp,
        fontWeight   = FontWeight.Light,
        letterSpacing = 0.5.sp
    )
    val SectionTitle = TextStyle(
        fontSize     = 18.sp,
        fontWeight   = FontWeight.Bold,
        letterSpacing = 0.sp
    )
    val CardTitle = TextStyle(
        fontSize     = 12.sp,
        fontWeight   = FontWeight.SemiBold,
        letterSpacing = 0.sp
    )
    val CardMeta = TextStyle(
        fontSize     = 10.sp,
        fontWeight   = FontWeight.Normal,
        letterSpacing = 0.sp
    )
    val BadgeText = TextStyle(
        fontSize     = 9.sp,
        fontWeight   = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
    val ButtonLabel = TextStyle(
        fontSize     = 13.sp,
        fontWeight   = FontWeight.SemiBold,
        letterSpacing = 0.3.sp
    )
    val NavLabel = TextStyle(
        fontSize     = 11.sp,
        fontWeight   = FontWeight.Medium,
        letterSpacing = 0.3.sp
    )
    val BodySmall = TextStyle(
        fontSize     = 11.sp,
        fontWeight   = FontWeight.Normal,
        lineHeight   = 16.sp
    )
}

object OttRadius {
    val SM   = 4.dp
    val MD   = 8.dp
    val LG   = 12.dp
    val XL   = 20.dp
    val Full = 50.dp    // ✅ FIXED: was 100.dp — causes glitch on small elements
}