@file:OptIn(ExperimentalComposeUiApi::class)

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
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
import com.example.myottapp.data.local.WatchHistoryEntity
import com.example.myottapp.data.remote.dto.MovieDto

private const val MAX_CARDS = 8

private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecond  = Color(0xFFB3B3B3)
private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)
private val BgCard      = Color(0xFF1F1F1F)

// ═══════════════════════════════════════════════════════════════════════
//  MOVIE ROW
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MovieRow(
    title:          String,
    movies:         List<MovieDto>,
    onMovieClick:   (movieId: Int, title: String) -> Unit,
    onViewAll:      (() -> Unit)?   = null,
    leftPadding:    Dp              = 48.dp,
    firstItemFocus: FocusRequester? = null
) {
    if (movies.isEmpty()) return

    val displayList = remember(movies) { movies.take(MAX_CARDS) }
    val showViewAll = movies.size > MAX_CARDS && onViewAll != null
    val listState   = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = leftPadding),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(
                    listOf(Accent, Accent.copy(0.3f))
                )))
            Text(
                text          = title,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(0.08f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text     = "${movies.size}",
                    fontSize = 10.sp,
                    color    = TextSecond
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = leftPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier              = Modifier
                .fillMaxWidth()
                .focusProperties {
                    exit = { direction ->
                        when (direction) {
                            FocusDirection.Left,
                            FocusDirection.Right -> FocusRequester.Cancel
                            else                 -> FocusRequester.Default
                        }
                    }
                }
        ) {
            // ✅ Max 8 cards
            items(
                items = displayList,
                key   = { movie -> "movie_${movie.id}" }
            ) { movie ->
                MovieCard(
                    movie          = movie,
                    focusRequester = if (movie.id == displayList.firstOrNull()?.id)
                        firstItemFocus else null,
                    onClick        = { onMovieClick(movie.id, movie.title) }
                )
            }

            // ✅ View All at position 9
            if (showViewAll) {
                item(key = "viewall_$title") {
                    ViewAllCard(
                        onClick = {
                            android.util.Log.d("CLICK", "ViewAll: $title")
                            onViewAll?.invoke()
                        }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  CONTINUE WATCHING ROW
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ContinueWatchingRowNF(
    history:        List<WatchHistoryEntity>,
    onItemClick:    (movieId: Int, title: String) -> Unit,
    onViewAll:      (() -> Unit)?   = null,
    leftPadding:    Dp              = 48.dp,
    firstItemFocus: FocusRequester? = null
) {
    if (history.isEmpty()) return

    val displayList = remember(history) { history.take(MAX_CARDS) }
    val showViewAll = history.size > MAX_CARDS && onViewAll != null
    val listState   = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = leftPadding),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(
                    listOf(Accent, Accent.copy(0.3f))
                )))
            Text(
                text          = "Continue Watching",
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = 0.2.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = leftPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier              = Modifier
                .fillMaxWidth()
                .focusProperties {
                    exit = { direction ->
                        when (direction) {
                            FocusDirection.Left,
                            FocusDirection.Right -> FocusRequester.Cancel
                            else                 -> FocusRequester.Default
                        }
                    }
                }
        ) {
            items(
                items = displayList,
                key   = { item -> "cw_${item.movieId}" }
            ) { item ->
                ContinueWatchingCardNF(
                    item           = item,
                    focusRequester = if (item.movieId == displayList.firstOrNull()?.movieId)
                        firstItemFocus else null,
                    onClick        = { onItemClick(item.movieId, item.title) }
                )
            }

            if (showViewAll) {
                item(key = "viewall_continue_watching") {
                    ViewAllCard(
                        width   = 200.dp,
                        height  = 130.dp,
                        onClick = {
                            android.util.Log.d("CLICK", "ViewAll ContinueWatching")
                            onViewAll?.invoke()
                        }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  VIEW ALL CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ViewAllCard(
    onClick: () -> Unit,
    width:   Dp = 150.dp,
    height:  Dp = 215.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "view_all_scale"
    )

    Box(
        modifier = Modifier
            .width(width).height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(
                elevation    = if (isFocused) 18.dp else 2.dp,
                shape        = RoundedCornerShape(10.dp),
                ambientColor = if (isFocused) Accent.copy(0.6f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.9f) else Color.Transparent
            )
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Accent else Color.White.copy(0.15f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                if (isFocused)
                    Brush.verticalGradient(listOf(Color(0xFF3A1800), Color(0xFF1A0E00)))
                else
                    Brush.verticalGradient(listOf(Color(0xFF252525), Color(0xFF181818)))
            )
            .focusable(interactionSource = interaction)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onClick
            )
            .onKeyEvent { e ->
                when {
                    e.type == KeyEventType.KeyDown &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> true
                    e.type == KeyEventType.KeyUp &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFocused)
                            Brush.radialGradient(listOf(AccentHover, Accent))
                        else
                            Brush.radialGradient(listOf(
                                Color.White.copy(0.12f),
                                Color.White.copy(0.05f)
                            ))
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) Color.White.copy(0.5f)
                        else Color.White.copy(0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.ArrowForward,
                    contentDescription = "View All",
                    tint               = if (isFocused) Color.White
                    else Color.White.copy(0.5f),
                    modifier           = Modifier.size(24.dp)
                )
            }

            Text(
                text       = "View All",
                fontSize   = 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                color      = if (isFocused) Color.White else Color.White.copy(0.5f)
            )

            if (isFocused) {
                Text(
                    text     = "Press OK",
                    fontSize = 10.sp,
                    color    = Accent.copy(0.8f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  CONTINUE WATCHING CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun ContinueWatchingCardNF(
    item:           WatchHistoryEntity,
    focusRequester: FocusRequester? = null,
    onClick:        () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "cw_${item.movieId}"
    )
    val ctx      = LocalContext.current
    val progress = item.progress.coerceIn(0f, 1f)
    val remainMs = (item.totalDuration - item.watchedDuration).coerceAtLeast(0L)
    val remMin   = remainMs / 60_000L
    val remStr   = when {
        remMin <= 0  -> "Finished"
        remMin >= 60 -> "${remMin / 60}h ${remMin % 60}m left"
        else         -> "${remMin}m left"
    }

    Box(
        modifier = Modifier
            .width(200.dp).height(130.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(
                elevation    = if (isFocused) 18.dp else 2.dp,
                shape        = RoundedCornerShape(8.dp),
                ambientColor = if (isFocused) Accent.copy(0.5f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.8f) else Color.Transparent
            )
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Accent else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .background(BgCard)
            .then(
                if (focusRequester != null)
                    Modifier.focusRequester(focusRequester)
                else Modifier
            )
            .focusable(interactionSource = interaction)
            .clickable(
                interactionSource = interaction,
                indication        = null,
                onClick           = onClick
            )
            .onKeyEvent { e ->
                when {
                    e.type == KeyEventType.KeyDown &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> true
                    e.type == KeyEventType.KeyUp &&
                            (e.key == Key.DirectionCenter || e.key == Key.Enter) -> {
                        onClick(); true
                    }
                    else -> false
                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(TmdbApiService.posterUrl(item.posterPath))
                .crossfade(true).build(),
            contentDescription = item.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
                .focusProperties { canFocus = false }
        )

        Box(Modifier.fillMaxSize()
            .focusProperties { canFocus = false }
            .background(Brush.verticalGradient(
                0f    to Color.Transparent,
                0.45f to Color.Transparent,
                1f    to Color.Black.copy(0.95f)
            )))

        if (isFocused) {
            Box(Modifier.fillMaxWidth().height(3.dp)
                .align(Alignment.TopCenter).background(Accent))
            Box(
                modifier = Modifier.size(38.dp).align(Alignment.Center)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape).background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Resume",
                    tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart).fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(item.title, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (remStr.isNotEmpty()) {
                Text(remStr, fontSize = 9.sp, color = TextSecond)
            }
            Box(Modifier.fillMaxWidth().height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(0.22f))
            ) {
                Box(Modifier.fillMaxWidth(progress).fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(
                        listOf(Accent, AccentHover)
                    )))
            }
        }
    }
}