package com.example.myottapp.features.movielist

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.MovieDto
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val BgDeep       = Color(0xFF060404)
private val BgCard       = Color(0xFF130E0A)
private val TextPure     = Color(0xFFFFFFFF)
private val TextMid      = Color(0xFFBBB3AB)
private val TextDim      = Color(0xFF5A5450)
private val AccentOrange = Color(0xFFFF6A00)
private val Gold         = Color(0xFFFFCC00)

// ═══════════════════════════════════════════════════════════════════════
//  LIST TYPE
// ═══════════════════════════════════════════════════════════════════════
enum class MovieListType(val displayName: String) {
    TRENDING("Trending Now"),
    POPULAR("Popular Movies"),
    TOP_RATED("Top Rated")
}

// ═══════════════════════════════════════════════════════════════════════
//  UI STATE
// ═══════════════════════════════════════════════════════════════════════
data class MovieListUiState(
    val movies:    List<MovieDto> = emptyList(),
    val isLoading: Boolean        = true,
    val error:     String?        = null
)

// ═══════════════════════════════════════════════════════════════════════
//  VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════
class MovieListViewModel(private val context: android.content.Context) : ViewModel() {
    private val repository = VideoRepository(context)
    private val _uiState   = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    fun load(type: MovieListType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = when (type) {
                MovieListType.TRENDING  -> repository.getTrendingMovies()
                MovieListType.POPULAR   -> repository.getPopularMovies()
                MovieListType.TOP_RATED -> repository.getTopRatedMovies()
            }
            _uiState.update {
                it.copy(
                    movies    = result.getOrNull() ?: emptyList(),
                    isLoading = false,
                    error     = if (result.isFailure) "Failed to load movies" else null
                )
            }
        }
    }
}

class MovieListViewModelFactory(private val context: android.content.Context) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MovieListViewModel(context) as T
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MOVIE LIST SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MovieListScreen(
    listType:     MovieListType,
    onBack:       () -> Unit                            = {},
    onMovieClick: (movieId: Int, title: String) -> Unit = { _, _ -> }
) {
    val context   = LocalContext.current
    val viewModel: MovieListViewModel = viewModel(
        factory = MovieListViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(listType) { viewModel.load(listType) }

    // ✅ First card auto-focused when movies load
    val firstItemFocus  = remember { FocusRequester() }
    var hasInitialFocus by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.movies.isNotEmpty()) {
        if (uiState.movies.isNotEmpty() && !hasInitialFocus) {
            kotlinx.coroutines.delay(200L)
            try {
                firstItemFocus.requestFocus()
                hasInitialFocus = true
            } catch (_: Exception) { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0603), BgDeep)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            MovieListTopBar(
                title  = listType.displayName,
                count  = uiState.movies.size,
                onBack = onBack
            )

            when {
                uiState.isLoading -> Box(
                    modifier         = Modifier.fillMaxSize().padding(top = 100.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = AccentOrange,
                            modifier    = Modifier.size(48.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Loading ${listType.displayName}...",
                            color    = TextMid,
                            fontSize = 13.sp
                        )
                    }
                }

                uiState.error != null -> Box(
                    modifier         = Modifier.fillMaxSize().padding(top = 120.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(
                            uiState.error!!,
                            color     = TextMid,
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> LazyVerticalGrid(
                    columns               = GridCells.Fixed(5),
                    contentPadding        = PaddingValues(
                        start  = 24.dp,
                        end    = 24.dp,
                        top    = 8.dp,
                        bottom = 32.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(16.dp),
                    modifier              = Modifier.fillMaxSize()
                ) {
                    // ✅ itemsIndexed — gives first card the focusRequester
                    itemsIndexed(
                        items = uiState.movies,
                        key   = { _, movie -> movie.id }
                    ) { index, movie ->
                        MovieGridCard(
                            movie          = movie,
                            // ✅ Only first card gets focusRequester
                            focusRequester = if (index == 0) firstItemFocus else null,
                            onClick        = { onMovieClick(movie.id, movie.title) }
                        )
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
fun MovieListTopBar(title: String, count: Int, onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused         by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(
                listOf(Color(0xFF0E0804), BgDeep)
            ))
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ✅ Back button with D-pad support
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isFocused) Color.White.copy(0.15f)
                    else Color.White.copy(0.08f)
                )
                .border(
                    width = if (isFocused) 1.5.dp else 0.dp,
                    color = Color.White.copy(0.4f),
                    shape = CircleShape
                )
                .focusable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                    onClick           = onBack
                )
                .onKeyEvent { e ->
                    when {
                        e.type == KeyEventType.KeyDown &&
                                (e.key == Key.DirectionCenter || e.key == Key.Enter) -> true
                        e.type == KeyEventType.KeyUp &&
                                (e.key == Key.DirectionCenter || e.key == Key.Enter) -> {
                            onBack(); true
                        }
                        else -> false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint     = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text       = title,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Black,
                color      = TextPure
            )
            if (count > 0) {
                Text(
                    text     = "$count movies",
                    fontSize = 12.sp,
                    color    = TextDim
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MOVIE GRID CARD — focusRequester param added
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MovieGridCard(
    movie:          MovieDto,
    onClick:        () -> Unit,
    focusRequester: FocusRequester? = null  // ✅ added
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused         by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.07f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "grid_${movie.id}"
    )
    val ctx  = LocalContext.current
    val year = movie.release_date.take(4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            // ✅ clip=false allows scale to render beyond bounds
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(
                elevation    = if (isFocused) 20.dp else 3.dp,
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
            // ✅ focusRequester applied BEFORE focusable
            .then(
                if (focusRequester != null)
                    Modifier.focusRequester(focusRequester)
                else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            // ✅ D-pad: KeyDown consumed, KeyUp fires action
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
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    movie.title.take(2).uppercase(),
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Black,
                    color      = AccentOrange.copy(0.4f)
                )
            }
        }

        // Bottom gradient
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                0f    to Color.Transparent,
                0.55f to Color.Transparent,
                1f    to Color.Black.copy(0.92f)
            ))
        )

        // Focus top accent bar
        if (isFocused) {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(AccentOrange)
            )
        }

        // Focus play overlay
        if (isFocused) {
            Box(
                modifier = Modifier.size(36.dp).align(Alignment.Center)
                    .shadow(8.dp, CircleShape, ambientColor = AccentOrange.copy(0.5f))
                    .clip(CircleShape).background(AccentOrange.copy(0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow, null,
                    tint     = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Rating badge
        if (movie.vote_average > 0) {
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(7.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.Star, null,
                    tint = Gold, modifier = Modifier.size(9.dp))
                Text(
                    String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold
                )
            }
        }

        // Year badge
        if (year.isNotEmpty()) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(7.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(AccentOrange.copy(0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(year, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    color = Color.White)
            }
        }

        // Title
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text     = movie.title,
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color    = Color.White, maxLines = 2,
                overflow = TextOverflow.Ellipsis, lineHeight = 14.sp
            )
        }
    }
}