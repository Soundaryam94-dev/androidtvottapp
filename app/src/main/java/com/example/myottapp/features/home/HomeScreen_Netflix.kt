@file:OptIn(ExperimentalComposeUiApi::class)

package com.example.myottapp.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myottapp.features.mylist.MyListButton
import com.example.myottapp.features.shared.HeroData
import com.example.myottapp.features.shared.HeroSection
import com.example.myottapp.features.shared.HERO_HEIGHT
import com.example.myottapp.features.shared.HeroPlayBtn
import com.example.myottapp.features.shared.HeroOutlineBtn

private const val MAX_ROW_ITEMS = 10

private val Bg          = Color(0xFF141414)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecond  = Color(0xFFB3B3B3)
private val TextDim     = Color(0xFF6A6A6A)
private val Accent      = Color(0xFFFF6A00)
private val AccentHover = Color(0xFFFF8C30)
private val Gold        = Color(0xFFFFCC00)

// ═══════════════════════════════════════════════════════════════════════
//  ENTRY POINT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NetflixHomeScreen(
    onPlayMovie:   (Int, String) -> Unit = { _, _ -> },
    onPlayTrailer: (Int, String) -> Unit = { _, _ -> },
    onDetails:     (Int) -> Unit         = {},
    onSearch:      () -> Unit            = {},
    onProfile:     () -> Unit            = {},
    onNavigate:    (String) -> Unit      = {},
    viewModel:     HomeViewModel         = viewModel()
) {
    val uiState   by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        TopBar(
            currentRoute    = uiState.currentTab,
            onSearch        = onSearch,
            onProfile       = onProfile,
            onNotifications = { onNavigate("notifications") },
            onNavigate      = { route ->
                when (route) {
                    "home", "movies" -> viewModel.switchTab(route)
                    else             -> onNavigate(route)
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading ->
                    NfShimmer(topPadding = 0.dp)

                uiState.error != null && !uiState.hasData ->
                    NfError(onRetry = { viewModel.retry() })

                else ->
                    AnimatedContent(
                        targetState    = uiState.currentTab,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "tab_content"
                    ) { tab ->
                        when (tab) {
                            "movies" -> MoviesTabContent(
                                uiState       = uiState,
                                listState     = listState,
                                onPlayMovie   = onPlayMovie,
                                onPlayTrailer = onPlayTrailer,
                                onDetails     = onDetails,
                                onNavigate    = onNavigate
                            )
                            else -> HomeTabContent(
                                uiState       = uiState,
                                listState     = listState,
                                onPlayMovie   = onPlayMovie,
                                onPlayTrailer = onPlayTrailer,
                                onDetails     = onDetails,
                                onNavigate    = onNavigate,
                                viewModel     = viewModel
                            )
                        }
                    }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  HOME TAB
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun HomeTabContent(
    uiState:       HomeUiState,
    listState:     LazyListState,
    onPlayMovie:   (Int, String) -> Unit,
    onPlayTrailer: (Int, String) -> Unit,
    onDetails:     (Int) -> Unit,
    onNavigate:    (String) -> Unit,
    viewModel:     HomeViewModel
) {
    val heroFocus             = remember { FocusRequester() }
    val continueWatchingFocus = remember { FocusRequester() }
    val trendingFocus         = remember { FocusRequester() }
    val popularFocus          = remember { FocusRequester() }
    val topRatedFocus         = remember { FocusRequester() }

    LazyColumn(
        state               = listState,
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            NfHero(
                uiState            = uiState,
                heroFocusRequester = heroFocus,
                onPlayMovie        = onPlayMovie,
                onPlayTrailer      = onPlayTrailer,
                onDetails          = onDetails,
                viewModel          = viewModel
            )
        }

        if (uiState.watchHistory.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Continue Watching",
                    itemCount      = uiState.watchHistory.size,
                    firstItemFocus = continueWatchingFocus
                ) {
                    uiState.watchHistory.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, histItem ->
                            item(key = histItem.movieId) {
                                ContinueWatchingCardNF(
                                    item           = histItem,
                                    focusRequester = if (index == 0) continueWatchingFocus else null,
                                    onClick        = { onPlayMovie(histItem.movieId, histItem.title) }
                                )
                            }
                        }
                    if (uiState.watchHistory.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_history") {
                            ViewAllCard(onClick = { onNavigate("movie_list/history") })
                        }
                    }
                }
            }
        }

        if (uiState.trendingMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Trending Now",
                    itemCount      = uiState.trendingMovies.size,
                    firstItemFocus = trendingFocus
                ) {
                    uiState.trendingMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) trendingFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.trendingMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_trending") {
                            ViewAllCard(onClick = { onNavigate("movie_list/trending") })
                        }
                    }
                }
            }
        }

        if (uiState.popularMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Popular Movies",
                    itemCount      = uiState.popularMovies.size,
                    firstItemFocus = popularFocus
                ) {
                    uiState.popularMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) popularFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.popularMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_popular") {
                            ViewAllCard(onClick = { onNavigate("movie_list/popular") })
                        }
                    }
                }
            }
        }

        if (uiState.topRatedMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Top Rated",
                    itemCount      = uiState.topRatedMovies.size,
                    firstItemFocus = topRatedFocus
                ) {
                    uiState.topRatedMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) topRatedFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.topRatedMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_toprated") {
                            ViewAllCard(onClick = { onNavigate("movie_list/toprated") })
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(56.dp)) }
    }
} // ✅ HomeTabContent closes here

// ═══════════════════════════════════════════════════════════════════════
//  MOVIES TAB
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MoviesTabContent(
    uiState:       HomeUiState,
    listState:     LazyListState,
    onPlayMovie:   (Int, String) -> Unit,
    onPlayTrailer: (Int, String) -> Unit,
    onDetails:     (Int) -> Unit,
    onNavigate:    (String) -> Unit
) {
    val moviesListState = rememberLazyListState()
    val trendingFocus   = remember { FocusRequester() }
    val popularFocus    = remember { FocusRequester() }
    val topRatedFocus   = remember { FocusRequester() }

    LazyColumn(
        state               = moviesListState,
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF1A0E04), Bg)
                    )),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text       = "Movies",
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Black,
                    color      = TextPrimary,
                    modifier   = Modifier.padding(start = 48.dp, bottom = 20.dp)
                )
            }
        }

        if (uiState.trendingMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Trending Movies",
                    itemCount      = uiState.trendingMovies.size,
                    firstItemFocus = trendingFocus
                ) {
                    uiState.trendingMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) trendingFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.trendingMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_trending") {
                            ViewAllCard(onClick = { onNavigate("movie_list/trending") })
                        }
                    }
                }
            }
        }

        if (uiState.popularMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Popular Movies",
                    itemCount      = uiState.popularMovies.size,
                    firstItemFocus = popularFocus
                ) {
                    uiState.popularMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) popularFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.popularMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_popular") {
                            ViewAllCard(onClick = { onNavigate("movie_list/popular") })
                        }
                    }
                }
            }
        }

        if (uiState.topRatedMovies.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                TvContentRow(
                    title          = "Top Rated",
                    itemCount      = uiState.topRatedMovies.size,
                    firstItemFocus = topRatedFocus
                ) {
                    uiState.topRatedMovies.take(MAX_ROW_ITEMS)
                        .forEachIndexed { index, movie ->
                            item(key = movie.id) {
                                TvMovieCard(
                                    movie          = movie,
                                    focusRequester = if (index == 0) topRatedFocus else null,
                                    onClick        = { onDetails(movie.id) }
                                )
                            }
                        }
                    if (uiState.topRatedMovies.size > MAX_ROW_ITEMS) {
                        item(key = "viewall_toprated") {
                            ViewAllCard(onClick = { onNavigate("movie_list/toprated") })
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(56.dp)) }
    }
} // ✅ MoviesTabContent closes here

// ═══════════════════════════════════════════════════════════════════════
//  TV CONTENT ROW — defined here, used by both tabs
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TvContentRow(
    title:          String,
    itemCount:      Int,
    firstItemFocus: FocusRequester,
    modifier:       Modifier         = Modifier,
    content:        LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
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
            if (itemCount > 0) {
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(0.08f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text     = "${minOf(itemCount, MAX_ROW_ITEMS + 1)}",
                        fontSize = 10.sp,
                        color    = TextSecond
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            state                 = listState,
            contentPadding        = PaddingValues(horizontal = 48.dp),
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
                },
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TV MOVIE CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun TvMovieCard(
    movie:          com.example.myottapp.data.remote.dto.MovieDto,
    focusRequester: FocusRequester? = null,
    onClick:        () -> Unit,
    width:          Dp = 150.dp,
    height:         Dp = 215.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.08f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "card_${movie.id}"
    )
    val ctx  = androidx.compose.ui.platform.LocalContext.current
    val year = movie.release_date.take(4)

    Box(
        modifier = Modifier
            .width(width).height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(
                elevation    = if (isFocused) 22.dp else 2.dp,
                shape        = RoundedCornerShape(10.dp),
                ambientColor = if (isFocused) Accent.copy(0.55f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.90f) else Color.Transparent
            )
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Accent else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color(0xFF1F1F1F))
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
        if (!movie.poster_path.isNullOrEmpty()) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(ctx)
                    .data(com.example.myottapp.core.network.TmdbApiService.posterUrl(movie.poster_path))
                    .crossfade(true).build(),
                contentDescription = movie.title,
                contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
                    .focusProperties { canFocus = false }
            )
        } else {
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF2A1F0F), Color(0xFF1F1F1F)))),
                contentAlignment = Alignment.Center
            ) {
                Text(movie.title.take(2).uppercase(),
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Black,
                    color      = Accent.copy(0.4f))
            }
        }

        Box(Modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                0f    to Color.Transparent,
                0.55f to Color.Transparent,
                1f    to Color.Black.copy(0.93f)))
            .focusProperties { canFocus = false })

        if (isFocused) {
            Box(Modifier.fillMaxWidth().height(3.dp)
                .align(Alignment.TopCenter).background(Accent))
        }

        if (isFocused) {
            Box(
                modifier = Modifier.size(32.dp).align(Alignment.Center)
                    .shadow(10.dp, CircleShape, ambientColor = Accent.copy(0.5f))
                    .clip(CircleShape).background(Accent.copy(0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        if (movie.vote_average > 0) {
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.Star, null,
                    tint = Gold, modifier = Modifier.size(9.dp))
                Text(String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
        }

        if (year.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                .clip(RoundedCornerShape(4.dp)).background(Accent.copy(0.85f))
                .padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(year, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    color = Color.White)
            }
        }

        Text(
            text     = movie.title,
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color    = Color.White, maxLines = 2,
            overflow = TextOverflow.Ellipsis, lineHeight = 13.sp,
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  HERO
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NfHero(
    uiState:            HomeUiState,
    heroFocusRequester: FocusRequester,
    onPlayMovie:        (Int, String) -> Unit,
    onPlayTrailer:      (Int, String) -> Unit,
    onDetails:          (Int) -> Unit,
    viewModel:          HomeViewModel = viewModel()
) {
    val slides = uiState.heroSlides

    if (slides.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(Color(0xFF1A0E04)))
        return
    }

    val currentIdx = uiState.heroIndex

    var hasInitialFocus by remember { mutableStateOf(false) }
    LaunchedEffect(slides.isNotEmpty()) {
        if (slides.isNotEmpty() && !hasInitialFocus) {
            kotlinx.coroutines.delay(200L)
            try {
                heroFocusRequester.requestFocus()
                hasInitialFocus = true
            } catch (_: Exception) { }
        }
    }

    val slide = slides.getOrNull(currentIdx) ?: slides.first()

    HeroSection(
        heroData   = slide,
        onBack     = null,
        slideIndex = currentIdx,
        slideCount = slides.size,
        onDotClick = { viewModel.updateHeroIndex(it) },
        actionButtons = { data ->
            HeroPlayBtn(
                onClick        = { onPlayMovie(data.movieId, data.title) },
                focusRequester = heroFocusRequester
            )
            HeroOutlineBtn(
                label   = "More Info",
                icon    = Icons.Default.Info,
                onClick = { onDetails(data.movieId) }
            )
            MyListButton(
                movieId    = data.movieId,
                title      = data.title,
                posterPath = data.posterPath,
                rating     = data.rating
            )
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════
//  PLAY BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NfPlayBtn(
    onClick:        () -> Unit,
    focusRequester: FocusRequester? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "play_scale"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (isFocused) 18.dp else 4.dp, RoundedCornerShape(10.dp),
                ambientColor = Accent.copy(0.6f), spotColor = Accent.copy(0.8f))
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFocused)
                    Brush.horizontalGradient(listOf(AccentHover, Accent))
                else
                    Brush.horizontalGradient(listOf(Accent, Color(0xFFDD5500)))
            )
            .then(if (isFocused) Modifier.border(
                2.dp, Color.White.copy(0.65f), RoundedCornerShape(10.dp)) else Modifier)
            .then(if (focusRequester != null)
                Modifier.focusRequester(focusRequester) else Modifier)
            .focusable(interactionSource = interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .onKeyEvent { event ->
                val code = event.nativeKeyEvent.keyCode
                if (code == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                    code == android.view.KeyEvent.KEYCODE_ENTER) {
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
                        onClick(); true
                    } else false
                } else false
            }
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(Icons.Default.PlayArrow, null,
                tint = Color.White, modifier = Modifier.size(19.dp))
            Text("Play", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = Color.White, softWrap = false)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  OUTLINE BUTTON
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NfOutlineBtn(
    label:   String,
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused   by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "outline_$label"
    )
    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Color.White.copy(0.20f) else Color.White.copy(0.08f))
            .border(if (isFocused) 2.dp else 1.dp,
                if (isFocused) Color.White.copy(0.85f) else Color.White.copy(0.25f),
                RoundedCornerShape(10.dp))
            .focusable(interactionSource = interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .onKeyEvent { event ->
                val code = event.nativeKeyEvent.keyCode
                if (code == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                    code == android.view.KeyEvent.KEYCODE_ENTER) {
                    if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) {
                        onClick(); true
                    } else false
                } else false
            }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Color.White, softWrap = false)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  HERO BADGE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun HeroBadge(label: String, accent: Boolean = false) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(if (accent) Accent.copy(0.18f) else Color.White.copy(0.10f))
        .border(1.dp,
            if (accent) Accent.copy(0.45f) else Color.White.copy(0.22f),
            RoundedCornerShape(4.dp))
        .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 10.sp,
            color      = if (accent) AccentHover else Color.White.copy(0.85f),
            fontWeight = FontWeight.SemiBold)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SHIMMER
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NfShimmer(topPadding: Dp = 0.dp) {
    val inf  = rememberInfiniteTransition(label = "shimmer")
    val anim by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "sv")
    val brush = Brush.horizontalGradient(
        listOf(Color.White.copy(0.05f), Color.White.copy(0.12f), Color.White.copy(0.05f)),
        startX = anim * 1200f, endX = anim * 1200f + 400f
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(top = topPadding),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(brush))
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 48.dp)) {
            repeat(2) {
                Box(Modifier.width(160.dp).height(18.dp)
                    .clip(RoundedCornerShape(4.dp)).background(brush))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(5) {
                        Box(Modifier.width(150.dp).height(215.dp)
                            .clip(RoundedCornerShape(10.dp)).background(brush))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ERROR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NfError(onRetry: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Bg).padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("⚠️", fontSize = 40.sp)
            Text("Something went wrong", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Accent)
                .clickable { onRetry() }
                .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}