package com.example.myottapp.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myottapp.ui.theme.OttColors
import com.example.myottapp.ui.theme.OttRadius
import com.example.myottapp.ui.theme.OttSpacing
import com.example.myottapp.ui.theme.OttTypography

// ═══════════════════════════════════════════════════════════════════════
//  PRIMARY BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun OttPrimaryButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.07f else 1f,
        animationSpec = tween(160), label = "primaryBtn"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(OttRadius.MD))
            .background(if (isFocused) OttColors.AccentBright else OttColors.Accent)
            .then(if (isFocused) Modifier.border(2.dp, Color.White.copy(0.6f),
                RoundedCornerShape(OttRadius.MD)) else Modifier)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 28.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, null, tint = OttColors.TextOnAccent,
                modifier = Modifier.size(18.dp))
            Text(label, style = OttTypography.ButtonLabel, color = OttColors.TextOnAccent)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SECONDARY BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun OttSecondaryButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.07f else 1f,
        animationSpec = tween(160), label = "secondaryBtn"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(OttRadius.MD))
            .background(if (isFocused) OttColors.White20 else OttColors.White10)
            .border(if (isFocused) 2.dp else 1.dp,
                if (isFocused) OttColors.TextPrimary.copy(0.8f) else OttColors.White40,
                RoundedCornerShape(OttRadius.MD))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, null, tint = OttColors.TextPrimary,
                modifier = Modifier.size(18.dp))
            Text(label, style = OttTypography.ButtonLabel, color = OttColors.TextPrimary)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ICON BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun OttIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1f,
        animationSpec = tween(150), label = "iconBtn"
    )
    Box(
        modifier = modifier
            .scale(scale).size(42.dp)
            .clip(RoundedCornerShape(OttRadius.Full))
            .background(if (isFocused) OttColors.Accent else OttColors.White10)
            .then(if (isFocused) Modifier.border(2.dp, OttColors.AccentBright,
                RoundedCornerShape(OttRadius.Full)) else Modifier)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description,
            tint = if (isFocused) Color.White else OttColors.TextSecondary,
            modifier = Modifier.size(18.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  META BADGE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MetaBadge(label: String, style: BadgeStyle = BadgeStyle.Default) {
    val (bg, border, textColor) = when (style) {
        BadgeStyle.Accent  -> Triple(OttColors.Accent, OttColors.Accent, Color.White)
        BadgeStyle.Outline -> Triple(Color.Transparent, OttColors.White40, OttColors.TextSecondary)
        BadgeStyle.Default -> Triple(OttColors.White10, OttColors.White20, OttColors.TextSecondary)
    }
    Box(modifier = Modifier
        .clip(RoundedCornerShape(OttRadius.SM))
        .background(bg).border(1.dp, border, RoundedCornerShape(OttRadius.SM))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, style = OttTypography.BadgeText, color = textColor)
    }
}

enum class BadgeStyle { Default, Accent, Outline }

// ═══════════════════════════════════════════════════════════════════════
//  PROGRESS BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun WatchProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(3.dp)
        .clip(RoundedCornerShape(OttRadius.Full))
        .background(OttColors.White20)) {
        Box(modifier = Modifier.fillMaxHeight()
            .fillMaxWidth(progress.coerceIn(0f, 1f))
            .clip(RoundedCornerShape(OttRadius.Full))
            .background(OttColors.Accent))
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SECTION HEADER  — title + TV-focusable "See All" button
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(
    title:    String,
    badge:    String           = "",
    onSeeAll: (() -> Unit)?    = null,
    modifier: Modifier         = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OttSpacing.ScreenEdge)
            .padding(bottom = OttSpacing.SM),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Orange left accent bar
        Box(modifier = Modifier
            .width(3.dp).height(20.dp)
            .clip(RoundedCornerShape(OttRadius.Full))
            .background(Brush.verticalGradient(
                listOf(OttColors.Accent, OttColors.Accent.copy(0.4f)))))

        // Title
        Text(title, style = OttTypography.SectionTitle, color = OttColors.TextPrimary)

        // Optional badge
        if (badge.isNotEmpty()) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(OttRadius.Full))
                .background(OttColors.AccentDim)
                .border(1.dp, OttColors.AccentBorder, RoundedCornerShape(OttRadius.Full))
                .padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(badge, style = OttTypography.BadgeText, color = OttColors.Accent)
            }
        }

        Spacer(Modifier.weight(1f))

        // ✅ TV-focusable See All button
        if (onSeeAll != null) {
            SeeAllButton(onClick = onSeeAll)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SEE ALL BUTTON — TV D-pad focusable with orange focus glow
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SeeAllButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "seeall"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 8.dp else 0.dp, RoundedCornerShape(20.dp),
                ambientColor = OttColors.Accent.copy(0.3f),
                spotColor    = OttColors.Accent.copy(0.4f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isFocused) OttColors.Accent.copy(0.18f)
                else Color.White.copy(0.05f)
            )
            .border(
                width = if (isFocused) 1.5.dp else 0.5.dp,
                color = if (isFocused) OttColors.Accent.copy(0.80f)
                else Color.White.copy(0.12f),
                shape = RoundedCornerShape(20.dp)
            )
            // ✅ TV: focusable FIRST so D-pad can select it
            .focusable(interactionSource = interactionSource)
            // ✅ TV: clickable so OK/Enter fires it
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            // ✅ Touch support
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text       = "See all",
                fontSize   = 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color      = if (isFocused) OttColors.AccentBright
                else OttColors.TextSecondary
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint               = if (isFocused) OttColors.AccentBright
                else OttColors.TextSecondary,
                modifier           = Modifier.size(14.dp)
            )
        }
    }
}