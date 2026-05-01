package com.example.myottapp.features.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService

// ✅ Single source of truth — both Home and Detail use this
val HERO_HEIGHT = 520.dp

private val Bg          = Color(0xFF141414)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecond  = Color(0xFFB3B3B3)
private val TextDim     = Color(0xFF6A6A6A)
private val Accent      = Color(0xFFFF6A00)
private val Gold        = Color(0xFFFFCC00)

/**
 * Shared Hero Data — passed from both Home and Detail screens
 */
data class HeroData(
    val movieId:      Int,
    val title:        String,
    val overview:     String,
    val tagline:      String      = "",
    val backdropPath: String?,
    val posterPath:   String?,
    val rating:       Double,
    val year:         String,
    val runtime:      String      = "",
    val genres:       List<String> = emptyList()
)

/**
 * Shared Hero Section — used by BOTH Home and Detail screens.
 * Optimized to prevent visual duplication of buttons/text during transitions.
 */
@Composable
fun HeroSection(
    heroData:      HeroData,
    onBack:        (() -> Unit)?             = null,
    actionButtons: @Composable RowScope.(HeroData) -> Unit,
    slideIndex:    Int?                      = null,
    slideCount:    Int                       = 1,
    onDotClick:    ((Int) -> Unit)?          = null
) {
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
    ) {
        // ── Layer 1: Placeholder ─────────────────────────────────────
        Box(Modifier.fillMaxSize().background(Color(0xFF1A0E04)))

        // ── Layer 2: Animated Backdrop ───────────────────────────────
        AnimatedContent(
            targetState = heroData,
            transitionSpec = {
                if (slideIndex != null) {
                    fadeIn(tween(700)) togetherWith fadeOut(tween(700))
                } else {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                }
            },
            label = "hero_backdrop"
        ) { targetData ->
            HeroBackdrop(
                backdropPath = targetData.backdropPath,
                ctx          = ctx
            )
        }

        // ── Layer 3: Overlays & Gradients ────────────────────────────
        Box(Modifier.fillMaxSize().background(Color.Black.copy(0.40f)).focusProperties { canFocus = false })

        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
            0.00f to Color.Black.copy(0.97f),
            0.45f to Color.Black.copy(0.70f),
            0.75f to Color.Black.copy(0.20f),
            1.00f to Color.Transparent
        )).focusProperties { canFocus = false })

        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(100.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(0.8f), Color.Transparent)))
            .focusProperties { canFocus = false })

        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(180.dp)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Bg)))
            .focusProperties { canFocus = false })

        // ── Layer 4: Back Button (If provided) ────────────────────────
        if (onBack != null) {
            HeroBackBtn(
                modifier = Modifier.padding(start = 48.dp, top = 32.dp),
                onClick  = onBack
            )
        }

        // ── Layer 5: Hero Content (Stable) ───────────────────────────
        // We move the text and buttons OUTSIDE the AnimatedContent
        // so they update instantly and don't "double up" during transitions.
        Column(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 64.dp, end = 16.dp)
        ) {
            // Title
            Text(
                text       = heroData.title,
                fontSize   = 40.sp,
                fontWeight = FontWeight.Black,
                color      = TextPrimary,
                lineHeight = 46.sp,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            // Tagline
            if (heroData.tagline.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = heroData.tagline,
                    fontSize  = 14.sp,
                    color     = Accent,
                    fontStyle = FontStyle.Italic,
                    maxLines  = 1
                )
            }

            Spacer(Modifier.height(12.dp))

            // Metadata row
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (heroData.rating > 0) {
                    Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(14.dp))
                    Text(
                        String.format(java.util.Locale.US, "%.1f", heroData.rating),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gold
                    )
                    HeroDot()
                }
                if (heroData.year.isNotEmpty()) {
                    Text(heroData.year, fontSize = 13.sp, color = TextSecond)
                }
                if (heroData.runtime.isNotEmpty()) {
                    HeroDot()
                    Text(heroData.runtime, fontSize = 13.sp, color = TextSecond)
                }
                Spacer(Modifier.width(4.dp))
                HeroQualityBadge("4K")
                HeroQualityBadge("HDR")

                if (heroData.genres.isNotEmpty()) {
                    HeroDot()
                    Text(
                        text     = heroData.genres.take(2).joinToString(" · "),
                        fontSize = 13.sp,
                        color    = TextSecond
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Overview
            if (heroData.overview.isNotEmpty()) {
                Text(
                    text       = heroData.overview,
                    fontSize   = 13.sp,
                    color      = TextSecond,
                    lineHeight = 20.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(28.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                actionButtons(heroData)
            }
        }

        // ── Layer 6: Carousel dots (Home only) ────────────────────────
        if (slideCount > 1 && slideIndex != null && onDotClick != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(slideCount) { i ->
                    val dotWidth by animateFloatAsState(
                        targetValue   = if (i == slideIndex) 24f else 6f,
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                        label = "dot_$i"
                    )
                    Box(modifier = Modifier
                        .size(dotWidth.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i == slideIndex) Accent
                            else Color.White.copy(alpha = 0.30f)
                        )
                        .clickable { onDotClick(i) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBackdrop(backdropPath: String?, ctx: android.content.Context) {
    if (!backdropPath.isNullOrEmpty()) {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(TmdbApiService.backdropUrl(backdropPath))
                .crossfade(true)
                .allowHardware(false)
                .build(),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier.fillMaxSize().focusProperties { canFocus = false }
        )
    } else {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(
                    listOf(Color(0xFF2A1A0A), Color(0xFF1A0E04))
                ))
        )
    }
}

@Composable
fun HeroBackBtn(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isFocused) Color.White.copy(0.2f) else Color.Black.copy(0.5f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White else Color.White.copy(0.3f),
                shape = CircleShape
            )
            .focusable(interactionSource = interaction)
            .clickable(interaction, null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
            tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun HeroQualityBadge(label: String) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(3.dp))
        .background(Color.White.copy(0.12f))
        .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(3.dp))
        .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeroDot() {
    Box(Modifier.size(3.dp).clip(CircleShape).background(TextDim))
}