package com.example.myottapp.features.mylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.local.MyListEntity

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF130E0A)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF8A8280)
private val AccentOrange = Color(0xFFFF6A00)
private val AccentHover  = Color(0xFFFF8C30)
private val Gold         = Color(0xFFFFCC00)
private val GreenSaved   = Color(0xFF22C55E)

// ═══════════════════════════════════════════════════════════════════════
//  MY LIST SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListScreen(
    onMovieClick: (movieId: Int, title: String) -> Unit = { _, _ -> },
    onBack:       () -> Unit = {},
    viewModel:    MyListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MyListTopBar(itemCount = uiState.items.size, onBack = onBack)
            when {
                uiState.isLoading    -> MyListLoadingScreen()
                uiState.items.isEmpty() -> MyListEmptyScreen()
                else -> MyListGrid(
                    items        = uiState.items,
                    onMovieClick = onMovieClick,
                    onRemove     = { viewModel.removeFromList(it) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListTopBar(itemCount: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E0804), BgDeep)))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape)
            .background(Color.White.copy(0.08f)).focusable().clickable { onBack() },
            contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Column {
            Text("My List", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPure)
            if (itemCount > 0) Text("$itemCount saved", fontSize = 12.sp, color = TextDim)
        }
        Spacer(Modifier.weight(1f))
        if (itemCount > 0) {
            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                .background(AccentOrange.copy(0.15f))
                .border(1.dp, AccentOrange.copy(0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("$itemCount items", fontSize = 12.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  GRID
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListGrid(items: List<MyListEntity>, onMovieClick: (Int, String) -> Unit, onRemove: (Int) -> Unit) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(5),
        contentPadding        = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement   = Arrangement.spacedBy(20.dp),
        modifier              = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.movieId }) { item ->
            AnimatedVisibility(visible = true,
                enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f),
                exit  = fadeOut(tween(200)) + scaleOut(tween(200))) {
                MyListCard(item = item, onPlay = { onMovieClick(item.movieId, item.title) }, onRemove = { onRemove(item.movieId) })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MY LIST CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListCard(item: MyListEntity, onPlay: () -> Unit, onRemove: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "card_${item.movieId}")
    val ctx = LocalContext.current

    Box(modifier = Modifier.width(160.dp).height(240.dp)
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .shadow(if (isFocused) 24.dp else 4.dp, RoundedCornerShape(14.dp),
            ambientColor = if (isFocused) AccentOrange.copy(0.6f) else Color.Transparent,
            spotColor    = if (isFocused) AccentOrange.copy(0.9f) else Color.Transparent)
        .clip(RoundedCornerShape(14.dp))
        .border(if (isFocused) 2.5.dp else 0.dp, if (isFocused) AccentOrange else Color.Transparent, RoundedCornerShape(14.dp))
        .background(BgCard)
        .focusable(interactionSource = interactionSource)
        .clickable(interactionSource, null, onClick = onPlay)
        .pointerInput(Unit) { detectTapGestures(onTap = { onPlay() }) }) {
        if (!item.posterPath.isNullOrEmpty()) {
            AsyncImage(model = ImageRequest.Builder(ctx).data(TmdbApiService.posterUrl(item.posterPath)).crossfade(true).build(),
                contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A1F0F), Color(0xFF1A1410)))),
                contentAlignment = Alignment.Center) {
                Text(item.title.take(2).uppercase(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = AccentOrange.copy(0.5f))
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Transparent, 0.5f to Color.Transparent, 0.75f to Color.Black.copy(0.7f), 1f to Color.Black.copy(0.97f))))
        if (isFocused) Box(modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.TopCenter).background(AccentOrange))
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(28.dp).clip(CircleShape)
            .background(Color.Black.copy(if (isFocused) 0.8f else 0.5f)).clickable { onRemove() },
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Delete, null, tint = if (isFocused) Color(0xFFFF4444) else Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp)) {
            if (isFocused) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 4.dp)) {
                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(AccentOrange), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                    Text("Play", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = AccentOrange)
                }
            }
            Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MY LIST BUTTON  ✅ FIXED — no more clipping
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListButton(
    movieId:    Int,
    title:      String,
    posterPath: String?,
    rating:     Double = 0.0,
    viewModel:  MyListViewModel = viewModel()
) {
    val savedIds by viewModel.savedIds.collectAsState()
    val isSaved = savedIds.contains(movieId)

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.07f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "mylist_btn"
    )
    val bgColor by animateColorAsState(
        if (isSaved) GreenSaved.copy(0.18f) else Color.White.copy(0.08f),
        tween(300), label = "mylist_bg"
    )
    val borderColor by animateColorAsState(
        when {
            isSaved   -> GreenSaved.copy(0.80f)
            isFocused -> Color.White.copy(0.85f)
            else      -> Color.White.copy(0.30f)
        },
        tween(300), label = "mylist_border"
    )

    // ✅ FIX: no width constraint — let content determine natural width
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 12.dp else 2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null) {
                viewModel.toggleMyList(movieId, title, posterPath, rating)
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    viewModel.toggleMyList(movieId, title, posterPath, rating)
                })
            }
            // ✅ Same vertical padding as HeroPlayButton/HeroTrailerButton
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ Animated icon toggle
            if (isSaved) {
                Icon(Icons.Default.Check, null,
                    tint = GreenSaved, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Add, null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
            // ✅ maxLines = 1 prevents vertical wrapping
            Text(
                text          = if (isSaved) "Added" else "My List",
                fontSize      = 13.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = if (isSaved) GreenSaved else Color.White,
                letterSpacing = 0.5.sp,
                maxLines      = 1,
                overflow      = TextOverflow.Clip,
                softWrap      = false   // ✅ disables soft wrapping completely
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  EMPTY / LOADING STATES
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MyListEmptyScreen() {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 48.dp)) {
            Text("📋", fontSize = 56.sp)
            Text("Your list is empty", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPure)
            Text("Add movies and shows by pressing\n\"+ My List\" on any title.",
                fontSize = 14.sp, color = TextMid, textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.07f))
                    .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Add, null, tint = TextDim, modifier = Modifier.size(14.dp))
                        Text("My List", fontSize = 12.sp, color = TextDim)
                    }
                }
                Text("← Look for this button", fontSize = 11.sp, color = TextDim)
            }
        }
    }
}

@Composable
fun MyListLoadingScreen() {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("📋", fontSize = 48.sp)
            Text("Loading your list...", color = TextMid, fontSize = 14.sp)
        }
    }
}
