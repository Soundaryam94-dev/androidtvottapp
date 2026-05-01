package com.example.myottapp.features.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow

private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)

/**
 * Shared Play Button used in both Home and Detail screens.
 * Consolidates NfPlayBtn and DetailPlayBtn.
 */
@Composable
fun HeroPlayBtn(
    onClick:        () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier:       Modifier       = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused  by interaction.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "play_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation    = if (isFocused) 15.dp else 4.dp,
                shape        = RoundedCornerShape(10.dp),
                ambientColor = Accent.copy(0.4f),
                spotColor    = Accent.copy(0.6f)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFocused) Brush.horizontalGradient(listOf(AccentHover, Accent))
                else Brush.horizontalGradient(listOf(Accent, Color(0xFFDD5500)))
            )
            .then(
                if (isFocused) Modifier.border(2.dp, Color.White.copy(0.7f), RoundedCornerShape(10.dp))
                else Modifier
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusable(interactionSource = interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.PlayArrow,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(22.dp)
            )
            Text(
                text       = "Play",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

/**
 * Shared Outline Button used for "More Info" or "Trailer".
 * Consolidates NfOutlineBtn and DetailOutlineBtn.
 */
@Composable
fun HeroOutlineBtn(
    label:   String,
    icon:    ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused  by interaction.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "outline_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White.copy(0.20f) else Color.White.copy(0.08f))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White.copy(0.9f) else Color.White.copy(0.25f),
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(interactionSource = interaction)
            .clickable(interaction, null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text       = label,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
        }
    }
}
