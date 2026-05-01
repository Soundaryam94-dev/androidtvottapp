package com.example.myottapp.features.search

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.MovieDto

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF130E0A)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF8A8280)
private val AccentOrange = Color(0xFFFF6A00)
private val Gold         = Color(0xFFFFCC00)
private val SearchBarBg  = Color(0xFF1A1410)

// ═══════════════════════════════════════════════════════════════════════
//  SEARCH SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchScreen(
    onMovieClick: (movieId: Int, title: String) -> Unit = { _, _ -> },
    onBack:       () -> Unit = {},
    viewModel:    SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchTopBar(
                query          = uiState.query,
                onQueryChanged = viewModel::onQueryChanged,
                onClear        = viewModel::clearSearch,
                onBack         = onBack,
                focusRequester = focusRequester
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading  -> SearchLoadingScreen()
                    uiState.error != null -> SearchErrorScreen(message = uiState.error!!)
                    uiState.results.isNotEmpty() -> SearchResultsList(
                        results      = uiState.results,
                        query        = uiState.query,
                        onMovieClick = onMovieClick
                    )
                    uiState.hasSearched && uiState.query.trim().length >= 2 ->
                        SearchEmptyScreen(query = uiState.query)
                    else -> SearchPromptScreen(onQueryChanged = viewModel::onQueryChanged)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SEARCH TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchTopBar(
    query:          String,
    onQueryChanged: (String) -> Unit,
    onClear:        () -> Unit,
    onBack:         () -> Unit,
    focusRequester: FocusRequester
) {
    val searchInteraction = remember { MutableInteractionSource() }
    val isSearchFocused   by searchInteraction.collectIsFocusedAsState()
    val focusManager      = LocalFocusManager.current  // ✅ dismiss keyboard on Enter

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0E0804), BgDeep)))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(44.dp).clip(CircleShape)
                .background(Color.White.copy(0.08f))
                .focusable().clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // Search bar
        Row(
            modifier = Modifier
                .weight(1f).height(52.dp)
                .shadow(
                    if (isSearchFocused) 16.dp else 4.dp,
                    RoundedCornerShape(26.dp),
                    ambientColor = if (isSearchFocused) AccentOrange.copy(0.4f) else Color.Transparent,
                    spotColor    = if (isSearchFocused) AccentOrange.copy(0.6f) else Color.Transparent
                )
                .clip(RoundedCornerShape(26.dp))
                .background(SearchBarBg)
                .border(
                    if (isSearchFocused) 2.dp else 1.dp,
                    if (isSearchFocused) AccentOrange else Color.White.copy(0.1f),
                    RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Search, null,
                tint     = if (isSearchFocused) AccentOrange else TextDim,
                modifier = Modifier.size(20.dp))

            BasicTextField(
                value         = query,
                onValueChange = onQueryChanged,
                modifier      = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .focusable(interactionSource = searchInteraction),
                textStyle = TextStyle(
                    color      = TextPure,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush     = SolidColor(AccentOrange),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // ✅ Dismiss keyboard when Enter/Search pressed
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text("Search movies, shows...", color = TextDim, fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                }
            )

            AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .size(24.dp).clip(CircleShape)
                        .background(Color.White.copy(0.15f))
                        .clickable { onClear() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Clear, null,
                        tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        AnimatedVisibility(visible = query.isNotEmpty()) {
            Text("Search", fontSize = 12.sp,
                color = AccentOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SEARCH RESULTS LIST
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchResultsList(
    results:      List<MovieDto>,
    query:        String,
    onMovieClick: (Int, String) -> Unit
) {
    val listState    = rememberLazyListState()
    val focusManager = LocalFocusManager.current  // ✅ dismiss keyboard on card click

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Results for \"$query\"",
                fontSize = 14.sp, color = TextMid, fontWeight = FontWeight.Medium)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentOrange.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${results.size} found",
                    fontSize = 11.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            state               = listState,
            contentPadding      = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.fillMaxSize()
        ) {
            items(results, key = { it.id }) { movie ->
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn() + slideInVertically()
                ) {
                    SearchMovieCard(
                        movie   = movie,
                        onClick = {
                            focusManager.clearFocus()            // ✅ dismiss keyboard first
                            onMovieClick(movie.id, movie.title)  // ✅ then navigate
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SEARCH MOVIE CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchMovieCard(movie: MovieDto, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scaleValue by animateFloatAsState(
        targetValue   = if (isFocused) 1.02f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "search_card_${movie.id}"
    )
    val ctx  = LocalContext.current
    val year = movie.release_date.take(4)

    Row(
        modifier = Modifier
            .fillMaxWidth().height(120.dp)
            .graphicsLayer { scaleX = scaleValue; scaleY = scaleValue }
            .shadow(
                elevation    = if (isFocused) 16.dp else 3.dp,
                shape        = RoundedCornerShape(14.dp),
                ambientColor = if (isFocused) AccentOrange.copy(0.5f) else Color.Transparent,
                spotColor    = if (isFocused) AccentOrange.copy(0.7f) else Color.Transparent
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isFocused)
                    Brush.horizontalGradient(listOf(Color(0xFF1E1208), Color(0xFF150F07)))
                else
                    Brush.horizontalGradient(listOf(BgCard, Color(0xFF0F0A07)))
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) AccentOrange else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster
        Box(
            modifier = Modifier
                .width(80.dp).fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
        ) {
            if (!movie.poster_path.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(TmdbApiService.posterUrl(movie.poster_path))
                        .crossfade(true).build(),
                    contentDescription = movie.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.verticalGradient(
                            listOf(Color(0xFF2A1F0F), Color(0xFF1A1410)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(movie.title.take(2).uppercase(), fontSize = 22.sp,
                        fontWeight = FontWeight.Black, color = AccentOrange.copy(0.6f))
                }
            }
            if (isFocused) {
                Box(modifier = Modifier.fillMaxSize().background(AccentOrange.copy(0.15f)))
            }
        }

        // Info
        Column(
            modifier = Modifier
                .weight(1f).fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = movie.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isFocused) Color.White else TextPure,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                if (movie.overview.isNotEmpty()) {
                    Text(
                        text       = movie.overview,
                        fontSize   = 11.sp,
                        color      = TextDim,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (movie.vote_average > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(12.dp))
                        Text(
                            String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold
                        )
                    }
                }
                if (year.isNotEmpty()) {
                    Text(year, fontSize = 11.sp, color = TextMid)
                }
            }
        }

        // Play icon on focus
        if (isFocused) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp).size(36.dp)
                    .clip(CircleShape).background(AccentOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  LOADING
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchLoadingScreen() {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = AccentOrange,
                modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
            Text("Searching...", color = TextMid, fontSize = 14.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  EMPTY STATE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchEmptyScreen(query: String) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 48.dp)) {
            Text("🔍", fontSize = 48.sp)
            Text("No results found", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = TextPure)
            Text("No movies found for \"$query\".\nTry a different keyword.",
                fontSize = 13.sp, color = TextMid,
                textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ERROR STATE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchErrorScreen(message: String) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 48.dp)) {
            Text("⚠️", fontSize = 48.sp)
            Text("Search failed", fontSize = 20.sp,
                fontWeight = FontWeight.Bold, color = TextPure)
            Text(message, fontSize = 13.sp, color = TextMid, textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PROMPT STATE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun SearchPromptScreen(
    onQueryChanged: (String) -> Unit = {}
) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(horizontal = 48.dp)
        ) {
            Text("🎬", fontSize = 56.sp)
            Text("Search Movies & Shows", fontSize = 22.sp,
                fontWeight = FontWeight.Bold, color = TextPure)
            Text("Type at least 2 characters to start searching",
                fontSize = 13.sp, color = TextMid, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Popular searches:", fontSize = 11.sp, color = TextDim)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Action", "Drama", "Marvel", "Netflix").forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(0.07f))
                            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(16.dp))
                            .focusable()
                            .clickable { onQueryChanged(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(tag, fontSize = 11.sp, color = TextMid)
                    }
                }
            }
        }
    }
}
