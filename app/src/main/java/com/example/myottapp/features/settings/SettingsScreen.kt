package com.example.myottapp.features.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────
//  SettingsScreen.kt
//  Place at: features/settings/SettingsScreen.kt
// ─────────────────────────────────────────────────────────────────────

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF120D0A)
private val BgCardFocus  = Color(0xFF1E1510)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF5A5450)
private val AccentOrange = Color(0xFFFF6A00)
private val AccentHover  = Color(0xFFFF8C30)
private val RedDanger    = Color(0xFFFF3B30)
private val GreenOk      = Color(0xFF22C55E)

// ─── Data models ──────────────────────────────────────────────────────
data class SettingsOption(val label: String, val value: String)

data class SettingsItem(
    val id:       String,
    val icon:     ImageVector,
    val title:    String,
    val subtitle: String,
    val options:  List<SettingsOption> = emptyList(),  // empty = no sub-options
    val isAction: Boolean = false,                      // true = just fires onClick
    val isDanger: Boolean = false
)

// ─── Settings data ────────────────────────────────────────────────────
private val settingsSections = listOf(
    "Playback" to listOf(
        SettingsItem(
            id       = "quality",
            icon     = Icons.Default.Tune,
            title    = "Playback Quality",
            subtitle = "Choose video resolution",
            options  = listOf(
                SettingsOption("Auto (Recommended)", "auto"),
                SettingsOption("4K Ultra HD",        "4k"),
                SettingsOption("1080p Full HD",      "1080p"),
                SettingsOption("720p HD",            "720p"),
                SettingsOption("480p",               "480p"),
            )
        ),
        SettingsItem(
            id       = "subtitles",
            icon     = Icons.Default.ClosedCaption,
            title    = "Subtitles",
            subtitle = "Choose subtitle language",
            options  = listOf(
                SettingsOption("Off",     "off"),
                SettingsOption("English", "en"),
                SettingsOption("Hindi",   "hi"),
                SettingsOption("Tamil",   "ta"),
                SettingsOption("Telugu",  "te"),
            )
        ),
        SettingsItem(
            id       = "audio",
            icon     = Icons.AutoMirrored.Filled.VolumeUp,
            title    = "Audio Language",
            subtitle = "Choose audio track",
            options  = listOf(
                SettingsOption("Original",      "original"),
                SettingsOption("English",        "en"),
                SettingsOption("Hindi",          "hi"),
                SettingsOption("Tamil",          "ta"),
            )
        ),
        SettingsItem(
            id       = "autoplay",
            icon     = Icons.Default.PlayArrow,
            title    = "Autoplay Next Episode",
            subtitle = "Automatically play next episode",
            options  = listOf(
                SettingsOption("On",  "on"),
                SettingsOption("Off", "off"),
            )
        ),
    ),
    "App" to listOf(
        SettingsItem(
            id       = "language",
            icon     = Icons.Default.Language,
            title    = "App Language",
            subtitle = "Interface language",
            options  = listOf(
                SettingsOption("English", "en"),
                SettingsOption("Hindi",   "hi"),
                SettingsOption("Tamil",   "ta"),
            )
        ),
        SettingsItem(
            id       = "privacy",
            icon     = Icons.Default.Shield,
            title    = "Privacy & Security",
            subtitle = "Manage your data and privacy",
            isAction = true
        ),
        SettingsItem(
            id       = "subscription",
            icon     = Icons.Default.Star,
            title    = "Subscription",
            subtitle = "Premium Plan · ₹299/month",
            isAction = true
        ),
        SettingsItem(
            id       = "about",
            icon     = Icons.Default.Info,
            title    = "About",
            subtitle = "MyOTT v1.0.0 · Build 100",
            isAction = true
        ),
    ),
    "Account" to listOf(
        SettingsItem(
            id       = "logout",
            icon     = Icons.AutoMirrored.Filled.ExitToApp,
            title    = "Log Out",
            subtitle = "Sign out of your account",
            isAction = true,
            isDanger = true
        ),
    )
)

// ═══════════════════════════════════════════════════════════════════════
//  SETTINGS SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsScreen(
    onBack:   () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Track selected value per setting id
    val selectedValues = remember {
        mutableStateOf(mapOf(
            "quality"   to "auto",
            "subtitles" to "off",
            "audio"     to "original",
            "autoplay"  to "on",
            "language"  to "en",
        ))
    }

    // Track which item is expanded
    var expandedId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0603), BgDeep)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────
            SettingsTopBar(onBack = onBack)

            // ── Two-column layout ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // ── LEFT: Settings list ───────────────────────────────
                LazyColumn(
                    modifier            = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding      = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    settingsSections.forEach { (sectionTitle, items) ->
                        item {
                            Text(
                                text          = sectionTitle.uppercase(),
                                fontSize      = 11.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = TextDim,
                                letterSpacing = 2.sp,
                                modifier      = Modifier.padding(
                                    top = 20.dp, bottom = 8.dp, start = 4.dp)
                            )
                        }
                        itemsIndexed(items) { _, item ->
                            SettingsRow(
                                item          = item,
                                selectedValue = selectedValues.value[item.id] ?: "",
                                isExpanded    = expandedId == item.id,
                                onToggleExpand = {
                                    expandedId = if (expandedId == item.id) null else item.id
                                },
                                onOptionSelected = { optionValue ->
                                    selectedValues.value = selectedValues.value
                                        .toMutableMap()
                                        .apply { put(item.id, optionValue) }
                                    expandedId = null
                                },
                                onActionClick = {
                                    if (item.isDanger) onLogout()
                                }
                            )
                        }
                    }
                }

                // ── RIGHT: Info panel ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .padding(top = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsInfoPanel()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E0804), BgDeep)))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        Box(
            modifier = Modifier
                .size(44.dp).clip(CircleShape)
                .background(if (isFocused) Color.White.copy(0.15f) else Color.White.copy(0.08f))
                .border(if (isFocused) 1.5.dp else 0.dp, Color.White.copy(0.4f), CircleShape)
                .focusable(interactionSource = interactionSource)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Column {
            Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPure)
            Text("Manage app preferences", fontSize = 12.sp, color = TextDim)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SETTINGS ROW — expandable item
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsRow(
    item:             SettingsItem,
    selectedValue:    String,
    isExpanded:       Boolean,
    onToggleExpand:   () -> Unit,
    onOptionSelected: (String) -> Unit,
    onActionClick:    () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.01f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "settings_${item.id}"
    )

    // Find display label for current selection
    val selectedLabel = item.options.find { it.value == selectedValue }?.label ?: ""
    val accentColor   = if (item.isDanger) RedDanger else AccentOrange

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 8.dp else 0.dp, RoundedCornerShape(14.dp),
                ambientColor = accentColor.copy(0.20f),
                spotColor    = accentColor.copy(0.25f))
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isExpanded -> Brush.verticalGradient(listOf(BgCardFocus, BgCard))
                    isFocused  -> Brush.horizontalGradient(listOf(BgCardFocus, BgCard))
                    else       -> Brush.horizontalGradient(listOf(BgCard, BgCard))
                }
            )
            .border(
                width = if (isFocused || isExpanded) 1.5.dp else 0.5.dp,
                color = when {
                    isExpanded -> accentColor.copy(0.50f)
                    isFocused  -> accentColor.copy(0.65f)
                    else       -> Color.White.copy(0.05f)
                },
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        // ── Main row ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusable(interactionSource = interactionSource)
                .clickable(interactionSource, null) {
                    if (item.isAction) onActionClick()
                    else if (item.options.isNotEmpty()) onToggleExpand()
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (item.isAction) onActionClick()
                        else if (item.options.isNotEmpty()) onToggleExpand()
                    })
                }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            item.isDanger -> RedDanger.copy(0.12f)
                            isFocused     -> AccentOrange.copy(0.18f)
                            else          -> Color.White.copy(0.06f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null,
                    tint = when {
                        item.isDanger -> RedDanger.copy(if (isFocused) 1f else 0.7f)
                        isFocused     -> AccentOrange
                        else          -> TextMid
                    },
                    modifier = Modifier.size(20.dp))
            }

            // Title + subtitle
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = item.title,
                    fontSize   = 14.sp,
                    fontWeight = if (isFocused || isExpanded) FontWeight.Bold else FontWeight.Medium,
                    color      = if (item.isDanger)
                        RedDanger.copy(if (isFocused) 1f else 0.8f)
                    else if (isFocused || isExpanded) TextPure
                    else TextMid
                )
                Text(item.subtitle, fontSize = 11.sp, color = TextDim)
            }

            // Right side — current value or arrow
            if (item.options.isNotEmpty()) {
                if (selectedLabel.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentOrange.copy(0.12f))
                            .border(1.dp, AccentOrange.copy(0.30f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(selectedLabel, fontSize = 11.sp,
                            color = AccentOrange, fontWeight = FontWeight.SemiBold,
                            maxLines = 1)
                    }
                }
                Text(
                    text       = if (isExpanded) "▲" else "▼",
                    fontSize   = 12.sp,
                    color      = if (isFocused) AccentOrange else TextDim
                )
            } else {
                Text("›", fontSize = 20.sp,
                    color = if (item.isDanger) RedDanger.copy(0.6f)
                    else if (isFocused) AccentOrange else TextDim)
            }
        }

        // ── Expandable options ────────────────────────────────────────
        AnimatedVisibility(
            visible = isExpanded && item.options.isNotEmpty(),
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 78.dp, end = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Divider
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp)
                    .background(AccentOrange.copy(0.15f)))
                Spacer(Modifier.height(4.dp))

                item.options.forEach { option ->
                    SettingsOptionRow(
                        option      = option,
                        isSelected  = option.value == selectedValue,
                        onSelect    = { onOptionSelected(option.value) }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SETTINGS OPTION ROW  (inside expanded item)
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsOptionRow(
    option:     SettingsOption,
    isSelected: Boolean,
    onSelect:   () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected && isFocused -> AccentOrange.copy(0.22f)
                    isSelected              -> AccentOrange.copy(0.12f)
                    isFocused               -> Color.White.copy(0.07f)
                    else                    -> Color.Transparent
                }
            )
            .border(
                width = if (isSelected || isFocused) 1.dp else 0.dp,
                color = when {
                    isSelected -> AccentOrange.copy(0.50f)
                    isFocused  -> Color.White.copy(0.20f)
                    else       -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onSelect)
            .pointerInput(Unit) { detectTapGestures(onTap = { onSelect() }) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = option.label,
            fontSize   = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = when {
                isSelected -> AccentOrange
                isFocused  -> TextPure
                else       -> TextMid
            }
        )
        if (isSelected) {
            Icon(Icons.Default.Check, null,
                tint     = AccentOrange,
                modifier = Modifier.size(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  RIGHT INFO PANEL
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SettingsInfoPanel() {
    // App info card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(AccentOrange, Color(0xFFBB4000)))),
                    contentAlignment = Alignment.Center) {
                    Text("▶", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("MyOTT", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPure)
                    Text("TV Edition", fontSize = 10.sp, color = TextDim)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp)
                .background(Color.White.copy(0.07f)))

            // Info rows
            listOf(
                "Version"     to "1.0.0",
                "Build"       to "100",
                "Platform"    to "Android TV",
                "API"         to "TMDB v3",
            ).forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 12.sp, color = TextDim)
                    Text(value, fontSize = 12.sp, color = TextMid, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Plan card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(
                listOf(AccentOrange.copy(0.15f), AccentOrange.copy(0.05f))))
            .border(1.dp, AccentOrange.copy(0.25f), RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Star, null, tint = AccentOrange, modifier = Modifier.size(18.dp))
                Text("Premium Plan", fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = TextPure)
            }
            Text("₹299/month", fontSize = 13.sp, color = AccentOrange, fontWeight = FontWeight.SemiBold)
            Text("4K Ultra HD · 4 screens\nRenews May 27, 2026",
                fontSize = 11.sp, color = TextMid, lineHeight = 16.sp)
        }
    }

    // Tip card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("TV Tips", fontSize = 12.sp,
                fontWeight = FontWeight.Bold, color = TextDim, letterSpacing = 1.sp)
            listOf(
                "D-pad to navigate",
                "OK/Enter to select",
                "Back to go previous",
                "Hold Back to go Home"
            ).forEach { tip ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentOrange.copy(0.6f)))
                    Text(tip, fontSize = 11.sp, color = TextDim)
                }
            }
        }
    }
}