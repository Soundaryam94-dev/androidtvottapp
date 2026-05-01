package com.example.myottapp.features.profile

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────
//  ProfileScreen.kt
//  Place at: features/profile/ProfileScreen.kt
// ─────────────────────────────────────────────────────────────────────

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF120D0A)
private val BgCardHover  = Color(0xFF1C1410)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF6A6460)
private val AccentOrange = Color(0xFFFF6A00)
private val AccentHover  = Color(0xFFFF8C30)
private val Gold         = Color(0xFFFFCC00)
private val RedDanger    = Color(0xFFFF3B30)

// ═══════════════════════════════════════════════════════════════════════
//  PROFILE SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ProfileScreen(
    onBack:   () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Hardcoded user data — replace with real auth state
    val userName  = "Soundarya N"
    val userEmail = "soundarya@example.com"
    val userPlan  = "Premium · 4K"
    val initials  = userName.split(" ").take(2).joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A0603), BgDeep, Color(0xFF060404)))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            ProfileTopBar(onBack = onBack)

            // ── Content — two column layout ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment     = Alignment.Top
            ) {
                // ── LEFT: Avatar + name + plan ─────────────────────
                Column(
                    modifier            = Modifier.width(280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(24.dp, CircleShape,
                                ambientColor = AccentOrange.copy(0.4f),
                                spotColor    = AccentOrange.copy(0.6f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AccentOrange, Color(0xFFCC4400)))
                            )
                            .border(3.dp, Color.White.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = initials,
                            fontSize   = 42.sp,
                            fontWeight = FontWeight.Black,
                            color      = Color.White
                        )
                    }

                    // Name
                    Text(
                        text       = userName,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPure,
                        textAlign  = TextAlign.Center
                    )

                    // Email
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Email, null,
                            tint = TextDim, modifier = Modifier.size(14.dp))
                        Text(userEmail, fontSize = 13.sp, color = TextMid)
                    }

                    // Plan badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AccentOrange.copy(0.15f))
                            .border(1.dp, AccentOrange.copy(0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Star, null,
                                tint = Gold, modifier = Modifier.size(14.dp))
                            Text(
                                text       = userPlan,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = AccentOrange
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Edit Profile button
                    ProfileActionButton(
                        icon    = Icons.Default.Edit,
                        label   = "Edit Profile",
                        onClick = {}
                    )

                    // Logout button
                    ProfileLogoutButton(onClick = onLogout)
                }

                // ── RIGHT: Settings list ───────────────────────────
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = "Account Settings",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDim,
                        letterSpacing = 2.sp,
                        modifier   = Modifier.padding(bottom = 4.dp)
                    )

                    ProfileMenuItem(
                        icon     = Icons.Default.Person,
                        title    = "Personal Information",
                        subtitle = "Name, email, phone",
                        onClick  = {}
                    )
                    ProfileMenuItem(
                        icon     = Icons.Default.Shield,
                        title    = "Privacy & Security",
                        subtitle = "Password, 2FA settings",
                        onClick  = {}
                    )
                    ProfileMenuItem(
                        icon     = Icons.Default.Notifications,
                        title    = "Notifications",
                        subtitle = "Alerts and preferences",
                        onClick  = {}
                    )
                    ProfileMenuItem(
                        icon     = Icons.Default.Settings,
                        title    = "App Settings",
                        subtitle = "Video quality, language",
                        onClick  = {}
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text      = "Subscription",
                        fontSize  = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color     = TextDim,
                        letterSpacing = 2.sp,
                        modifier  = Modifier.padding(bottom = 4.dp)
                    )

                    // Plan info card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgCard)
                            .border(1.dp, AccentOrange.copy(0.20f), RoundedCornerShape(14.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Premium Plan", fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold, color = TextPure)
                                Text("₹299/month · Renews May 27, 2026",
                                    fontSize = 12.sp, color = TextMid)
                                Text("4K Ultra HD · 4 screens · Downloads",
                                    fontSize = 11.sp, color = TextDim)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentOrange.copy(0.12f))
                                    .border(1.dp, AccentOrange.copy(0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Manage", fontSize = 12.sp,
                                    color = AccentOrange, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E0804), BgDeep)))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isFocused) Color.White.copy(0.15f) else Color.White.copy(0.08f))
                .border(if (isFocused) 1.5.dp else 0.dp,
                    Color.White.copy(0.4f), CircleShape)
                .focusable(interactionSource = interactionSource)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Column {
            Text("Profile", fontSize = 22.sp,
                fontWeight = FontWeight.Black, color = TextPure)
            Text("Manage your account", fontSize = 12.sp, color = TextDim)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PROFILE MENU ITEM
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ProfileMenuItem(
    icon:     ImageVector,
    title:    String,
    subtitle: String,
    onClick:  () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.02f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "menu_$title"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 8.dp else 0.dp, RoundedCornerShape(12.dp),
                ambientColor = AccentOrange.copy(0.2f))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) BgCardHover else BgCard)
            .border(
                width = if (isFocused) 1.5.dp else 0.5.dp,
                color = if (isFocused) AccentOrange.copy(0.6f) else Color.White.copy(0.06f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isFocused) AccentOrange.copy(0.20f)
                    else Color.White.copy(0.06f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null,
                tint     = if (isFocused) AccentOrange else TextMid,
                modifier = Modifier.size(20.dp))
        }

        // Text
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 14.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) TextPure else TextMid)
            Text(subtitle, fontSize = 11.sp, color = TextDim)
        }

        // Arrow
        Text("›", fontSize = 20.sp,
            color = if (isFocused) AccentOrange else TextDim,
            fontWeight = FontWeight.Light)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ACTION BUTTON (Edit Profile)
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ProfileActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.05f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "action_$label"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color.White.copy(0.14f) else Color.White.copy(0.07f))
            .border(if (isFocused) 1.5.dp else 1.dp,
                if (isFocused) Color.White.copy(0.5f) else Color.White.copy(0.12f),
                RoundedCornerShape(12.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  LOGOUT BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ProfileLogoutButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.05f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "logout"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .shadow(if (isFocused) 12.dp else 0.dp, RoundedCornerShape(12.dp),
                ambientColor = RedDanger.copy(0.3f), spotColor = RedDanger.copy(0.4f))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) RedDanger.copy(0.18f) else RedDanger.copy(0.08f)
            )
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) RedDanger.copy(0.7f) else RedDanger.copy(0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null,
                tint     = if (isFocused) RedDanger else RedDanger.copy(0.7f),
                modifier = Modifier.size(18.dp))
            Text(
                text       = "Log Out",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isFocused) RedDanger else RedDanger.copy(0.7f)
            )
        }
    }
}