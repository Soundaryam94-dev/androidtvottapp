package com.example.myottapp.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.myottapp.core.ui.theme.FocusHighlight

@Composable
fun FocusableCard(
    modifier: Modifier = Modifier,
    onClick:  () -> Unit = {},           // ✅ added — required for D-pad
    content:  @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // ✅ Spring animation — feels natural on TV
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "focusable_card_scale"
    )

    Box(
        modifier = modifier
            // ✅ 1. Scale wraps everything — border scales with the card
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // ✅ 2. Clip before border so border respects rounded corners
            .clip(RoundedCornerShape(8.dp))
            // ✅ 3. Border only when focused
            .then(
                if (isFocused)
                    Modifier.border(3.dp, FocusHighlight, RoundedCornerShape(8.dp))
                else Modifier
            )
            // ✅ 4. Focus tracking before focusable()
            .onFocusChanged { isFocused = it.isFocused }
            // ✅ 5. focusable() — makes card reachable by D-pad
            .focusable()
            // ✅ 6. D-pad center / Enter key handler for TV remote
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else false
            }
    ) {
        content()
    }
}