package com.example.myottapp.features.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────
//  NotificationScreen.kt
//  Place at: features/notifications/NotificationScreen.kt
// ─────────────────────────────────────────────────────────────────────

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF120D0A)
private val BgCardFocus  = Color(0xFF1E1510)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF5A5450)
private val AccentOrange = Color(0xFFFF6A00)
private val RedBadge     = Color(0xFFFF3B30)
private val GreenOk      = Color(0xFF22C55E)
private val Gold         = Color(0xFFFFCC00)

// ─── Notification data model ──────────────────────────────────────────
data class NotificationItem(
    val id:        Int,
    val icon:      ImageVector,
    val iconColor: Color,
    val title:     String,
    val message:   String,
    val time:      String,
    val isRead:    Boolean = false,
    val tag:       String  = ""   // "NEW", "LIVE", etc.
)

// ─── Sample notifications ─────────────────────────────────────────────
private val sampleNotifications = listOf(
    NotificationItem(
        id         = 1,
        icon       = Icons.Default.NewReleases,
        iconColor  = AccentOrange,
        title      = "New Release: Dune Part Two",
        message    = "Now available to stream in 4K HDR. Don't miss the epic continuation!",
        time       = "2 min ago",
        isRead     = false,
        tag        = "NEW"
    ),
    NotificationItem(
        id         = 2,
        icon       = Icons.Default.PlayArrow,
        iconColor  = GreenOk,
        title      = "Continue Watching",
        message    = "You left off at 1h 23m in Oppenheimer. Resume where you stopped.",
        time       = "1 hour ago",
        isRead     = false,
        tag        = ""
    ),
    NotificationItem(
        id         = 3,
        icon       = Icons.Default.Star,
        iconColor  = Gold,
        title      = "Top Rated This Week",
        message    = "Interstellar is trending #1. Add it to your list now!",
        time       = "3 hours ago",
        isRead     = false,
        tag        = "TRENDING"
    ),
    NotificationItem(
        id         = 4,
        icon       = Icons.Default.Movie,
        iconColor  = Color(0xFF7C3AED),
        title      = "Your List Updated",
        message    = "Inception has been added to My List successfully.",
        time       = "Yesterday",
        isRead     = true,
        tag        = ""
    ),
    NotificationItem(
        id         = 5,
        icon       = Icons.Default.Info,
        iconColor  = Color(0xFF0EA5E9),
        title      = "App Update Available",
        message    = "MyOTT v1.1.0 is available with new features and bug fixes.",
        time       = "2 days ago",
        isRead     = true,
        tag        = ""
    ),
    NotificationItem(
        id         = 6,
        icon       = Icons.Default.Notifications,
        iconColor  = AccentOrange,
        title      = "Subscription Renewal",
        message    = "Your Premium Plan renews on May 27, 2026. All good!",
        time       = "3 days ago",
        isRead     = true,
        tag        = ""
    ),
)

// ═══════════════════════════════════════════════════════════════════════
//  NOTIFICATION SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NotificationScreen(
    onBack: () -> Unit = {}
) {
    var notifications by remember {
        mutableStateOf(sampleNotifications)
    }

    val unreadCount = notifications.count { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0603), BgDeep)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            NotificationTopBar(
                unreadCount  = unreadCount,
                onBack       = onBack,
                onMarkAllRead = {
                    notifications = notifications.map { it.copy(isRead = true) }
                }
            )

            // ── Content ───────────────────────────────────────────────
            if (notifications.isEmpty()) {
                NotificationEmptyState()
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    // Unread section
                    val unread = notifications.filter { !it.isRead }
                    val read   = notifications.filter { it.isRead }

                    if (unread.isNotEmpty()) {
                        item {
                            SectionLabel(
                                text  = "NEW  ·  ${unread.size} unread",
                                color = AccentOrange
                            )
                        }
                        itemsIndexed(unread) { index, notif ->
                            AnimatedVisibility(
                                visible = true,
                                enter   = fadeIn() + slideInVertically { it / 2 }
                            ) {
                                NotificationCard(
                                    item    = notif,
                                    onRead  = {
                                        notifications = notifications.map {
                                            if (it.id == notif.id) it.copy(isRead = true) else it
                                        }
                                    }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    if (read.isNotEmpty()) {
                        item { SectionLabel(text = "EARLIER", color = TextDim) }
                        itemsIndexed(read) { _, notif ->
                            NotificationCard(item = notif, onRead = {})
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NotificationTopBar(
    unreadCount:   Int,
    onBack:        () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E0804), BgDeep)))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        val backInteraction = remember { MutableInteractionSource() }
        val backFocused by backInteraction.collectIsFocusedAsState()
        Box(
            modifier = Modifier
                .size(44.dp).clip(CircleShape)
                .background(if (backFocused) Color.White.copy(0.15f) else Color.White.copy(0.08f))
                .border(if (backFocused) 1.5.dp else 0.dp, Color.White.copy(0.4f), CircleShape)
                .focusable(interactionSource = backInteraction)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // Title
        Column(modifier = Modifier.weight(1f)) {
            Text("Notifications", fontSize = 22.sp,
                fontWeight = FontWeight.Black, color = TextPure)
            if (unreadCount > 0) {
                Text("$unreadCount unread", fontSize = 12.sp, color = AccentOrange)
            } else {
                Text("All caught up", fontSize = 12.sp, color = TextDim)
            }
        }

        // Mark all read button
        if (unreadCount > 0) {
            val markInteraction = remember { MutableInteractionSource() }
            val markFocused by markInteraction.collectIsFocusedAsState()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (markFocused) AccentOrange.copy(0.20f) else AccentOrange.copy(0.10f))
                    .border(if (markFocused) 1.5.dp else 1.dp,
                        AccentOrange.copy(if (markFocused) 0.80f else 0.30f),
                        RoundedCornerShape(10.dp))
                    .focusable(interactionSource = markInteraction)
                    .clickable { onMarkAllRead() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Check, null,
                        tint = AccentOrange, modifier = Modifier.size(14.dp))
                    Text("Mark all read", fontSize = 12.sp,
                        color = AccentOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  NOTIFICATION CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NotificationCard(item: NotificationItem, onRead: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.01f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "notif_${item.id}"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 8.dp else 0.dp, RoundedCornerShape(14.dp),
                ambientColor = if (!item.isRead) AccentOrange.copy(0.15f) else Color.Transparent)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isFocused    -> Brush.horizontalGradient(listOf(BgCardFocus, BgCardFocus))
                    !item.isRead -> Brush.horizontalGradient(listOf(Color(0xFF1A1008), BgCard))
                    else         -> Brush.horizontalGradient(listOf(BgCard, BgCard))
                }
            )
            .border(
                width = if (isFocused) 1.5.dp else if (!item.isRead) 1.dp else 0.5.dp,
                color = when {
                    isFocused   -> AccentOrange.copy(0.60f)
                    !item.isRead -> AccentOrange.copy(0.25f)
                    else        -> Color.White.copy(0.05f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null) { if (!item.isRead) onRead() }
            .pointerInput(Unit) { detectTapGestures(onTap = { if (!item.isRead) onRead() }) }
            .padding(16.dp),
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Unread dot + icon ─────────────────────────────────────────
        Box {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconColor.copy(0.15f))
                    .border(1.dp, item.iconColor.copy(0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null,
                    tint     = item.iconColor,
                    modifier = Modifier.size(22.dp))
            }
            // Red unread dot
            if (!item.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(RedBadge)
                        .border(1.5.dp, BgDeep, CircleShape)
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = item.title,
                    fontSize   = 14.sp,
                    fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium,
                    color      = if (!item.isRead) TextPure else TextMid,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.tag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentOrange.copy(0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(item.tag, fontSize = 9.sp,
                                color = AccentOrange, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp)
                        }
                    }
                    Text(item.time, fontSize = 10.sp, color = TextDim)
                }
            }
            Text(
                text       = item.message,
                fontSize   = 12.sp,
                color      = if (!item.isRead) TextMid else TextDim,
                lineHeight = 17.sp,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SECTION LABEL
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SectionLabel(text: String, color: Color) {
    Text(
        text          = text,
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = color,
        letterSpacing = 2.sp,
        modifier      = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

// ═══════════════════════════════════════════════════════════════════════
//  EMPTY STATE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NotificationEmptyState() {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.NotificationsNone, null,
                tint = TextDim, modifier = Modifier.size(64.dp))
            Text("No notifications", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = TextPure)
            Text("You're all caught up!", fontSize = 13.sp,
                color = TextDim, textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  NOTIFICATION BADGE BUTTON — used in top bar of HomeScreen
//  ✅ Replace existing OttIconButton for Notifications
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NotificationIconButton(
    unreadCount: Int = 0,
    onClick:     () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.12f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "notif_icon"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        // Icon button
        Box(
            modifier = Modifier
                .size(38.dp).clip(CircleShape)
                .background(if (isFocused) Color.White.copy(0.14f) else Color.Transparent)
                .border(if (isFocused) 1.5.dp else 0.dp,
                    if (isFocused) Color.White.copy(0.5f) else Color.Transparent,
                    CircleShape)
                .focusable(interactionSource = interactionSource)
                .clickable(interactionSource, null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint               = if (isFocused) Color.White else Color.White.copy(0.75f),
                modifier           = Modifier.size(20.dp)
            )
        }

        // ✅ Red badge — shows unread count
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(if (unreadCount > 9) 18.dp else 14.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(RedBadge)
                    .border(1.5.dp, BgDeep, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    fontSize   = 7.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color.White
                )
            }
        }
    }
}
