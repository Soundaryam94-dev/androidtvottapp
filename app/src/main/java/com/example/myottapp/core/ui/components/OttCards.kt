package com.example.myottapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myottapp.ui.theme.OttColors
import com.example.myottapp.ui.theme.OttRadius
import com.example.myottapp.ui.theme.OttSpacing
import com.example.myottapp.ui.theme.OttTypography

// ═══════════════════════════════════════════════════════════════════════
//  DATA MODEL
// ═══════════════════════════════════════════════════════════════════════
data class ContentItem(
    val id:            String,
    val title:         String,
    val subtitle:      String = "",
    val year:          String = "",
    val genre:         String = "",
    val rating:        String = "",
    val cardColor:     Color,
    val accentColor:   Color  = OttColors.Accent,
    val progress:      Float  = 0f,
    val remainingTime: String = "",
    val rank:          Int    = 0,
    val isNew:         Boolean = false,
    val badge:         String = ""
)

// ═══════════════════════════════════════════════════════════════════════
//  STANDARD CONTENT CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ContentCard(
    item:     ContentItem,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ✅ FIXED: spring instead of tween — natural TV feel
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "card_${item.id}"
    )

    Box(
        modifier = modifier
            // ✅ graphicsLayer FIRST — scale wraps border correctly
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .width(OttSpacing.CardWidth)
            .height(OttSpacing.CardHeight)
            .clip(RoundedCornerShape(OttRadius.MD))
            .background(item.cardColor)
            .then(
                if (isFocused) Modifier.border(2.dp, OttColors.Accent,
                    RoundedCornerShape(OttRadius.MD))
                else Modifier.border(1.dp, OttColors.White10,
                    RoundedCornerShape(OttRadius.MD))
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource,
                indication = null, onClick = onClick)
            // ✅ FIXED: D-pad key handler — was completely missing
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) { onClick(); true } else false
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f to Color.Transparent,
                0.4f to Color.Transparent,
                1f to OttColors.Black80
            )))

        if (item.rank > 0) {
            Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                .clip(RoundedCornerShape(OttRadius.SM))
                .background(OttColors.Accent)
                .padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text("#${item.rank}", style = OttTypography.BadgeText, color = Color.White)
            }
        }

        if (item.badge.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                .clip(RoundedCornerShape(OttRadius.SM))
                .background(if (item.badge == "NEW") OttColors.Success else OttColors.Accent)
                .padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(item.badge, style = OttTypography.BadgeText, color = Color.White)
            }
        }

        if (isFocused) {
            Box(modifier = Modifier.fillMaxSize()
                .border(1.dp, Brush.verticalGradient(
                    listOf(OttColors.AccentBright.copy(alpha = 0.5f), Color.Transparent)
                ), RoundedCornerShape(OttRadius.MD)))
        }

        Column(modifier = Modifier.align(Alignment.BottomStart)
            .padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(item.title, style = OttTypography.CardTitle,
                color = OttColors.TextPrimary, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotEmpty() || item.year.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        if (item.year.isNotEmpty()) append(item.year)
                        if (item.year.isNotEmpty() && item.genre.isNotEmpty()) append(" · ")
                        if (item.genre.isNotEmpty()) append(item.genre)
                        if (item.subtitle.isNotEmpty() &&
                            (item.year.isNotEmpty() || item.genre.isNotEmpty())) append(" · ")
                        if (item.subtitle.isNotEmpty()) append(item.subtitle)
                    },
                    style    = OttTypography.CardMeta,
                    color    = OttColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  CONTINUE WATCHING CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ContinueWatchingCard(
    item:     ContentItem,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ✅ FIXED: spring animation
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.07f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cw_${item.id}"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }  // ✅ first
            .width(OttSpacing.CardWidthLarge)
            .height(OttSpacing.CardHeightLarge)
            .clip(RoundedCornerShape(OttRadius.MD))
            .background(item.cardColor)
            .then(
                if (isFocused) Modifier.border(2.dp, OttColors.Accent,
                    RoundedCornerShape(OttRadius.MD))
                else Modifier.border(1.dp, OttColors.White10,
                    RoundedCornerShape(OttRadius.MD))
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource,
                indication = null, onClick = onClick)
            // ✅ FIXED: D-pad key handler — was missing
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) { onClick(); true } else false
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f to Color.Transparent,
                0.5f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.92f)
            )))

        // ✅ Play icon on focus — using real Icon, not Text("▶")
        if (isFocused) {
            Box(modifier = Modifier.align(Alignment.Center).size(36.dp)
                .clip(RoundedCornerShape(OttRadius.Full))
                .background(OttColors.Accent.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, "Play",
                    tint     = Color.White,
                    modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
            .padding(horizontal = 10.dp).padding(bottom = 8.dp)) {
            Text(item.title, style = OttTypography.CardTitle,
                color = OttColors.TextPrimary, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotEmpty()) {
                Text(item.subtitle, style = OttTypography.CardMeta,
                    color = OttColors.TextSecondary, maxLines = 1)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchProgressBar(progress = item.progress,
                    modifier = Modifier.weight(1f))
                if (item.remainingTime.isNotEmpty()) {
                    Text(item.remainingTime, style = OttTypography.CardMeta,
                        color = OttColors.Accent)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  FEATURED CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun FeaturedCard(
    item:     ContentItem,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ✅ FIXED: spring animation
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "feat_${item.id}"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }  // ✅ first
            .width(240.dp)
            .height(144.dp)
            .clip(RoundedCornerShape(OttRadius.LG))
            .background(Brush.linearGradient(
                listOf(item.cardColor, item.cardColor.copy(alpha = 0.6f))
            ))
            .then(
                if (isFocused) Modifier.border(2.dp, OttColors.Accent,
                    RoundedCornerShape(OttRadius.LG))
                else Modifier.border(1.dp, OttColors.White10,
                    RoundedCornerShape(OttRadius.LG))
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource,
                indication = null, onClick = onClick)
            // ✅ FIXED: D-pad key handler — was missing
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) { onClick(); true } else false
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f to Color.Transparent,
                0.3f to Color.Transparent,
                1f to OttColors.BgBase.copy(alpha = 0.95f)
            )))

        if (item.rating.isNotEmpty()) {
            Row(modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("★", style = OttTypography.BadgeText, color = OttColors.Gold)
                Text(item.rating, style = OttTypography.BadgeText, color = OttColors.Gold)
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(item.title, style = OttTypography.CardTitle,
                color = OttColors.TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.year.isNotEmpty())  MetaBadge(item.year)
                if (item.genre.isNotEmpty()) MetaBadge(item.genre)
            }
        }
    }
}