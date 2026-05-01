package com.example.myottapp.features.detail

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.CastDto

// ─────────────────────────────────────────────────────────────────────
//  CastRow.kt — Netflix-style TV cast section
//  Place at: features/detail/CastRow.kt
//
//  Usage:
//    CastRow(
//        cast        = uiState.cast,
//        leftPadding = 48.dp,
//        onCastClick = { member -> }
//    )
// ─────────────────────────────────────────────────────────────────────

private val Bg          = Color(0xFF141414)
private val BgCard      = Color(0xFF1F1F1F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecond  = Color(0xFFB3B3B3)
private val TextDim     = Color(0xFF6A6A6A)
private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)

// ═══════════════════════════════════════════════════════════════════════
//  CAST ROW — section header + horizontal scrolling cast items
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun CastRow(
    cast:         List<CastDto>,
    leftPadding:  Dp             = 48.dp,
    maxVisible:   Int            = 8,          // ✅ limit to top 8
    onCastClick:  (CastDto) -> Unit = {}
) {
    // ✅ Limit cast to top 8 — no clutter
    val visibleCast = remember(cast) { cast.take(maxVisible) }
    if (visibleCast.isEmpty()) return

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Section header ────────────────────────────────────────────
        CastSectionHeader(leftPadding = leftPadding)
        Spacer(Modifier.height(14.dp))

        // ── Horizontal cast cards ─────────────────────────────────────
        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = leftPadding),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = visibleCast,
                key   = { it.id }
            ) { member ->
                CastItem(
                    member  = member,
                    onClick = { onCastClick(member) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SECTION HEADER
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun CastSectionHeader(leftPadding: Dp = 48.dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = leftPadding),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Orange accent bar
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Accent, Accent.copy(alpha = 0.3f))
                    )
                )
        )
        Text(
            text          = "Cast",
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextPrimary,
            letterSpacing = 0.2.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  CAST ITEM — circular image + name + character
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun CastItem(
    member:  CastDto,
    onClick: () -> Unit = {}
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()

    // ✅ Scale animation on D-pad focus
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.10f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cast_scale_${member.id}"
    )

    val ctx        = LocalContext.current
    val profileUrl = TmdbApiService.profileUrl(member.profile_path)

    Column(
        modifier = Modifier
            .width(80.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // ✅ TV focusable — D-pad can select this item
            .focusable(interactionSource = interaction)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Circular photo ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(64.dp)
                // ✅ Glow shadow on focus
                .shadow(
                    elevation    = if (isFocused) 16.dp else 2.dp,
                    shape        = CircleShape,
                    ambientColor = if (isFocused) Accent.copy(0.55f) else Color.Transparent,
                    spotColor    = if (isFocused) Accent.copy(0.85f) else Color.Transparent
                )
                .clip(CircleShape)
                .background(BgCard)
                // ✅ Orange border on focus
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Accent else Color.White.copy(0.12f),
                    shape = CircleShape
                )
        ) {
            if (!profileUrl.isNullOrEmpty()) {
                // ── Real actor photo ──────────────────────────────────
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(profileUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    contentScale       = ContentScale.Crop,
                    alignment          = Alignment.TopCenter,
                    modifier           = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                // ── Placeholder — initials avatar ─────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Accent.copy(0.25f),
                                    Color(0xFF1A1208)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = member.name
                            .split(" ")
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() },
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isFocused) Accent else Accent.copy(0.65f)
                    )
                }
            }

            // Focus overlay shimmer (subtle inner glow)
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentHover.copy(0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // ── Actor name ────────────────────────────────────────────────
        Text(
            text       = member.name.split(" ").first(),  // first name only
            fontSize   = 11.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            color      = if (isFocused) TextPrimary else TextSecond,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )

        // ── Character name (optional, smaller) ────────────────────────
        if (member.character.isNotEmpty()) {
            Text(
                text      = member.character,
                fontSize  = 9.sp,
                color     = if (isFocused) Accent.copy(0.85f) else TextDim,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}
