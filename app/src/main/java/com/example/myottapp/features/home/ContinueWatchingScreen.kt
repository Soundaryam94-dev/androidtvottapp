package com.example.myottapp.features.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.local.WatchHistoryEntity
import com.example.myottapp.ui.theme.OttSpacing

// ─────────────────────────────────────────────────────────────────────
//  ContinueWatchingScreen.kt — Netflix-style Continue Watching row
//  Place at: features/home/ContinueWatchingScreen.kt
// ─────────────────────────────────────────────────────────────────────

private val BgCard       = Color(0xFF1A1208)
private val BgCardFocus  = Color(0xFF251A0A)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF5A5450)
private val AccentOrange = Color(0xFFFF6A00)
private val AccentHover  = Color(0xFFFF8C30)

// ═══════════════════════════════════════════════════════════════════════
//  CONTINUE WATCHING ROW
//  ✅ Hidden when watchHistory is empty — no empty section shown
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ContinueWatchingRow(
    watchHistory:  List<WatchHistoryEntity>,
    onResumeMovie: (movieId: Int, title: String, startPosition: Long) -> Unit,
    onRemove:      (movieId: Int) -> Unit = {}
) {
    // ✅ Conditional visibility — hide if empty
    if (watchHistory.isEmpty()) return

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Section Header ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OttSpacing.ScreenEdge, vertical = 0.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Orange left accent bar
            Box(modifier = Modifier
                .size(width = 3.dp, height = 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(
                    listOf(AccentOrange, AccentOrange.copy(0.4f)))))

            Text(
                text          = "Continue Watching",
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPure,
                letterSpacing = 0.3.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── Cards Row ─────────────────────────────────────────────────
        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = OttSpacing.ScreenEdge),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = watchHistory,
                key   = { it.movieId }
            ) { item ->
                ContinueWatchingCard(
                    item    = item,
                    onClick = {
                        onResumeMovie(item.movieId, item.title, item.watchedDuration)
                    },
                    onRemove = { onRemove(item.movieId) }
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
    item:     WatchHistoryEntity,
    onClick:  () -> Unit,
    onRemove: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "cw_${item.movieId}"
    )

    val ctx          = LocalContext.current
    val posterUrl    = TmdbApiService.posterUrl(item.posterPath)
    val progress     = item.progress.coerceIn(0f, 1f)
    val remainingStr = formatRemainingTime(item.watchedDuration, item.totalDuration)

    Box(
        modifier = Modifier
            .width(190.dp)
            .height(130.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation    = if (isFocused) 18.dp else 2.dp,
                shape        = RoundedCornerShape(12.dp),
                ambientColor = if (isFocused) AccentOrange.copy(0.5f) else Color.Transparent,
                spotColor    = if (isFocused) AccentOrange.copy(0.8f) else Color.Transparent
            )
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) AccentOrange else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(BgCard)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
    ) {
        // ── Poster image ──────────────────────────────────────────────
        if (posterUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(posterUrl).crossfade(true).build(),
                contentDescription = item.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            // Fallback gradient when no poster
            Box(modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(
                    listOf(Color(0xFF1E1208), Color(0xFF2A1A0A)))))
            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                Text(item.title.take(2).uppercase(),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Black,
                    color      = AccentOrange.copy(0.4f))
            }
        }

        // ── Dark gradient overlay (bottom) ────────────────────────────
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f    to Color.Black.copy(0.05f),
                0.40f to Color.Black.copy(0.20f),
                0.70f to Color.Black.copy(0.75f),
                1f    to Color.Black.copy(0.97f)
            )))

        // ── Orange top accent bar on focus ────────────────────────────
        if (isFocused) {
            Box(modifier = Modifier
                .fillMaxWidth().height(3.dp)
                .align(Alignment.TopCenter)
                .background(AccentOrange))
        }

        // ── Play icon on focus ────────────────────────────────────────
        if (isFocused) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
                    .shadow(12.dp, CircleShape, ambientColor = AccentOrange.copy(0.6f))
                    .clip(CircleShape)
                    .background(AccentOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Resume",
                    tint     = Color.White,
                    modifier = Modifier.size(22.dp))
            }
        }

        // ── Remove (X) button top-right ───────────────────────────────
        CwRemoveButton(
            modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
            onClick  = onRemove
        )

        // ── Bottom: title + remaining time + progress bar ─────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Title
            Text(
                text       = item.title,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPure,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            // Remaining time
            if (remainingStr.isNotEmpty()) {
                Text(
                    text     = remainingStr,
                    fontSize = 9.sp,
                    color    = TextMid
                )
            }

            // ✅ Progress bar
            CwProgressBar(progress = progress)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PROGRESS BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun CwProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White.copy(0.22f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(AccentOrange, AccentHover)
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  REMOVE BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun CwRemoveButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(
                if (isFocused) Color.Red.copy(0.90f)
                else Color.Black.copy(0.65f)
            )
            .border(0.5.dp, Color.White.copy(0.18f), CircleShape)
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Close, "Remove",
            tint     = Color.White,
            modifier = Modifier.size(10.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  HELPER — format remaining time
// ═══════════════════════════════════════════════════════════════════════
private fun formatRemainingTime(watchedMs: Long, totalMs: Long): String {
    if (totalMs <= 0L) return ""
    val remainingMs  = (totalMs - watchedMs).coerceAtLeast(0L)
    val remainingMin = remainingMs / 60_000L
    return when {
        remainingMin <= 0  -> "Finished"
        remainingMin >= 60 -> "${remainingMin / 60}h ${remainingMin % 60}m left"
        else               -> "${remainingMin}m left"
    }
}
