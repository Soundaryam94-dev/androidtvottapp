package com.example.myottapp.features.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TOP_BAR_HEIGHT_DP = 64.dp

private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)

data class NavItemData(val label: String, val route: String)

val TOP_NAV_ITEMS = listOf(
    NavItemData("Home",    "home"),
    NavItemData("Movies",  "movies"),
    NavItemData("My List", "library")
)

@Composable
fun TopBar(
    currentRoute:    String           = "home",
    onNavigate:      (String) -> Unit = {},
    onSearch:        () -> Unit       = {},
    onProfile:       () -> Unit       = {},
    // ✅ onNotifications now a real callback — not hardcoded {}
    onNotifications: () -> Unit       = {},
    modifier:        Modifier         = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TOP_BAR_HEIGHT_DP)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 48.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TopBarLogo()

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TOP_NAV_ITEMS.forEach { item ->
                NavItem(
                    label      = item.label,
                    isSelected = currentRoute == item.route ||
                            currentRoute.startsWith(item.route),
                    onClick    = { onNavigate(item.route) }
                )
            }
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TopBarIcon(
                icon    = Icons.Default.Search,
                label   = "Search",
                // ✅ search properly wired
                onClick = {
                    android.util.Log.d("CLICK", "Search clicked")
                    onSearch()
                }
            )
            TopBarIcon(
                icon    = Icons.Default.Notifications,
                label   = "Notifications",
                // ✅ notifications properly wired — was {} before
                onClick = {
                    android.util.Log.d("CLICK", "Notifications clicked")
                    onNotifications()
                }
            )
            TopBarProfileDot(onClick = onProfile)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  LOGO
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TopBarLogo() {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(Accent, Color(0xFFBB4000)))),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        Text(
            text          = "MyOTT",
            fontSize      = 17.sp,
            fontWeight    = FontWeight.Black,
            color         = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  NAV ITEM
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NavItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused         by interactionSource.collectIsFocusedAsState()

    // ✅ Minimal scale (1.05 max as requested)
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.05f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "nav_$label"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            // ✅ Fixed height ensures UI stability and no layout shifts
            .height(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(
                // ✅ Background highlight ONLY on focus
                if (isFocused) Color.White.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = {
                    android.util.Log.d("CLICK", "NavItem $label clicked")
                    onClick()
                }
            )
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> true
                    event.type == KeyEventType.KeyUp &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            }
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text          = label,
            fontSize      = 13.sp,
            // ✅ Distinct weights: Bold for focus, SemiBold for selected
            fontWeight    = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
            color         = when {
                isFocused  -> Color.White
                isSelected -> Color.White.copy(alpha = 0.90f)
                else       -> Color.White.copy(alpha = 0.50f)
            },
            letterSpacing = 0.2.sp
        )

        // ✅ Permanent indicator container: Always exists, only visible when selected.
        // This prevents the button height from changing when selection changes.
        Box(modifier = Modifier
            .padding(top = 2.dp)
            .width(20.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(if (isSelected) Accent else Color.Transparent)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ICON BUTTON — used for Search and Notifications
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TopBarIcon(
    icon:    ImageVector,
    label:   String   = "",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused         by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isFocused) Color.White.copy(0.14f) else Color.Transparent)
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) Color.White.copy(0.50f) else Color.Transparent,
                shape = CircleShape
            )
            // ✅ focusable BEFORE clickable
            .focusable(interactionSource = interactionSource)
            // ✅ clickable with real onClick
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            // ✅ D-pad support
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> true
                    event.type == KeyEventType.KeyUp &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = Color.White.copy(if (isFocused) 1f else 0.80f),
            modifier           = Modifier.size(18.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PROFILE DOT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TopBarProfileDot(
    initials: String  = "RS",
    onClick:  () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused         by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Accent, Color(0xFFCC3300))))
            .then(
                if (isFocused) Modifier.border(2.dp, Color.White, CircleShape)
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = {
                    android.util.Log.d("CLICK", "Profile clicked")
                    onClick()
                }
            )
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> true
                    event.type == KeyEventType.KeyUp &&
                            (event.key == Key.DirectionCenter || event.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    }
}