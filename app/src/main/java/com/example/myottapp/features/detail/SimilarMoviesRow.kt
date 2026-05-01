package com.example.myottapp.features.detail

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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.MovieDto

// ─────────────────────────────────────────────────────────────────────
//  SimilarMoviesRow.kt — Netflix-style "More Like This" section
//  Place at: features/detail/SimilarMoviesRow.kt
//
//  Usage:
//    SimilarMoviesRow(
//        movies      = uiState.similar,
//        leftPadding = 0.dp,
//        onMovieClick = { movieId, title -> }
//    )
// ─────────────────────────────────────────────────────────────────────

private val Bg          = Color(0xFF141414)
private val BgCard      = Color(0xFF1F1F1F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecond  = Color(0xFFB3B3B3)
private val TextDim     = Color(0xFF6A6A6A)
private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)
private val Gold        = Color(0xFFFFCC00)

// ═══════════════════════════════════════════════════════════════════════
//  SIMILAR MOVIES ROW — section header + horizontal card list
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SimilarMoviesRow(
    movies:       List<MovieDto>,
    leftPadding:  Dp                             = 48.dp,
    maxVisible:   Int                            = 12,
    onMovieClick: (movieId: Int, title: String) -> Unit = { _, _ -> }
) {
    // ✅ Limit to 12 max — no clutter, good performance
    val visibleMovies = remember(movies) { movies.take(maxVisible) }
    if (visibleMovies.isEmpty()) return

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Section header ────────────────────────────────────────────
        SimilarSectionHeader(leftPadding = leftPadding)
        Spacer(Modifier.height(12.dp))

        // ── Horizontal cards ──────────────────────────────────────────
        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = leftPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = visibleMovies,
                key   = { it.id }
            ) { movie ->
                SimilarMovieCard(
                    movie   = movie,
                    onClick = { onMovieClick(movie.id, movie.title) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SECTION HEADER — "More Like This" with orange accent bar
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SimilarSectionHeader(leftPadding: Dp = 48.dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = leftPadding),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Orange vertical accent bar
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
            text          = "More Like This",
            fontSize      = 16.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextPrimary,
            letterSpacing = 0.2.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SIMILAR MOVIE CARD — poster style, full TV focus support
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SimilarMovieCard(
    movie:   MovieDto,
    onClick: () -> Unit,
    width:   Dp = 160.dp,
    height:  Dp = 220.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()

    // ✅ Smooth scale animation on D-pad focus
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "sim_${movie.id}"
    )

    val ctx  = LocalContext.current
    val year = movie.release_date.take(4)

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            // ✅ Orange glow shadow on focus
            .shadow(
                elevation    = if (isFocused) 20.dp else 2.dp,
                shape        = RoundedCornerShape(10.dp),
                ambientColor = if (isFocused) Accent.copy(0.55f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.90f) else Color.Transparent
            )
            .clip(RoundedCornerShape(10.dp))
            // ✅ Orange border on focus
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Accent else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .background(BgCard)
            // ✅ TV: focusable first, then clickable
            .focusable(interactionSource = interaction)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onClick
            )
            // ✅ Touch support
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
    ) {
        // ── Poster image ──────────────────────────────────────────────
        if (!movie.poster_path.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(TmdbApiService.posterUrl(movie.poster_path))
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            // ── Fallback — gradient + initials ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1F0F), BgCard)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = movie.title.take(2).uppercase(),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Black,
                    color      = Accent.copy(0.40f)
                )
            }
        }

        // ── Bottom gradient overlay ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f    to Color.Transparent,
                        0.50f to Color.Transparent,
                        1f    to Color.Black.copy(0.94f)
                    )
                )
        )

        // ── Orange top bar on focus ───────────────────────────────────
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(Accent)
            )
        }

        // ── Play hint on focus ────────────────────────────────────────
        if (isFocused) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.Center)
                    .shadow(10.dp, CircleShape, ambientColor = Accent.copy(0.5f))
                    .clip(CircleShape)
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow, "Play",
                    tint     = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Rating badge (top-left) ───────────────────────────────────
        if (movie.vote_average > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(0.78f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    Icons.Default.Star, null,
                    tint     = Gold,
                    modifier = Modifier.size(9.dp)
                )
                Text(
                    text       = String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Gold
                )
            }
        }

        // ── Year badge (top-right) ────────────────────────────────────
        if (year.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Accent.copy(0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(year, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // ── Title (bottom) ────────────────────────────────────────────
        Text(
            text       = movie.title,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 13.sp,
            modifier   = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}