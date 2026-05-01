package com.example.myottapp.features.movielist

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.example.myottapp.features.home.MovieRow
import com.example.myottapp.features.shared.HeroSection
import com.example.myottapp.features.shared.HeroData
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.MovieDto
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────
//  MoviesScreen.kt — Netflix-style Movies page with genre rows
//  Place at: features/movielist/MoviesScreen.kt
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
//  UI STATE
// ═══════════════════════════════════════════════════════════════════════
data class MoviesUiState(
    val trending:  List<MovieDto> = emptyList(),
    val popular:   List<MovieDto> = emptyList(),
    val topRated:  List<MovieDto> = emptyList(),
    val action:    List<MovieDto> = emptyList(),
    val comedy:    List<MovieDto> = emptyList(),
    val thriller:  List<MovieDto> = emptyList(),
    val drama:     List<MovieDto> = emptyList(),
    val scifi:     List<MovieDto> = emptyList(),
    val isLoading: Boolean        = true,
    val error:     String?        = null
)

// ═══════════════════════════════════════════════════════════════════════
//  VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════
class MoviesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoRepository(application)
    private val _state = MutableStateFlow(MoviesUiState())
    val state: StateFlow<MoviesUiState> = _state.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // ✅ Parallel fetch — all rows at once
                val trending  = async { repository.getTrendingMovies().getOrNull() ?: emptyList() }
                val popular   = async { repository.getPopularMovies().getOrNull() ?: emptyList() }
                val topRated  = async { repository.getTopRatedMovies().getOrNull() ?: emptyList() }
                // Genre IDs: Action=28, Comedy=35, Thriller=53, Drama=18, Sci-Fi=878
                val action    = async { repository.getMoviesByGenre(28).getOrNull() ?: emptyList() }
                val comedy    = async { repository.getMoviesByGenre(35).getOrNull() ?: emptyList() }
                val thriller  = async { repository.getMoviesByGenre(53).getOrNull() ?: emptyList() }
                val drama     = async { repository.getMoviesByGenre(18).getOrNull() ?: emptyList() }
                val scifi     = async { repository.getMoviesByGenre(878).getOrNull() ?: emptyList() }

                _state.update {
                    it.copy(
                        trending  = trending.await().take(15),
                        popular   = popular.await().take(15),
                        topRated  = topRated.await().take(15),
                        action    = action.await().take(15),
                        comedy    = comedy.await().take(15),
                        thriller  = thriller.await().take(15),
                        drama     = drama.await().take(15),
                        scifi     = scifi.await().take(15),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  MOVIES SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MoviesScreen(
    onMovieClick: (movieId: Int, title: String) -> Unit = { _, _ -> },
    onBack:       () -> Unit                            = {},
    viewModel:    MoviesViewModel                       = viewModel()
) {
    val state     by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when {
            state.isLoading -> MoviesShimmer()
            state.error != null && state.trending.isEmpty() ->
                MoviesError(onRetry = { viewModel.loadAll() })
            else -> LazyColumn(
                state               = listState,
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding      = PaddingValues(top = 64.dp, bottom = 48.dp)
            ) {
                // Section rows — each only shown if data loaded
                if (state.trending.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        MovieRow("Trending Now",    state.trending,  onMovieClick)
                    }
                }
                if (state.popular.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Popular Movies",  state.popular,   onMovieClick)
                    }
                }
                if (state.action.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Action",          state.action,    onMovieClick)
                    }
                }
                if (state.thriller.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Thriller",        state.thriller,  onMovieClick)
                    }
                }
                if (state.drama.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Drama",           state.drama,     onMovieClick)
                    }
                }
                if (state.comedy.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Comedy",          state.comedy,    onMovieClick)
                    }
                }
                if (state.scifi.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Sci-Fi",          state.scifi,     onMovieClick)
                    }
                }
                if (state.topRated.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        MovieRow("Top Rated",       state.topRated,  onMovieClick)
                    }
                }
            }
        }

        // ── Top bar ───────────────────────────────────────────────────
        MoviesTopBar(onBack = onBack)
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MoviesTopBar(onBack: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused by interaction.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth().height(64.dp)
            .background(Brush.verticalGradient(
                listOf(Color.Black.copy(0.97f), Color.Black.copy(0.75f), Color.Transparent)))
            .padding(horizontal = 48.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        Box(modifier = Modifier
            .size(36.dp).clip(CircleShape)
            .background(if (isFocused) Color.White.copy(0.14f) else Color.White.copy(0.08f))
            .border(if (isFocused) 1.5.dp else 0.dp,
                if (isFocused) Color.White.copy(0.60f) else Color.Transparent, CircleShape)
            .focusable(interactionSource = interaction)
            .clickable(interaction, null, onClick = onBack)
            .onKeyEvent { e ->
                if (e.type == KeyEventType.KeyUp &&
                    (e.key == Key.DirectionCenter || e.key == Key.Enter)) {
                    onBack(); true
                } else false
            },
            contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint = Color.White.copy(if (isFocused) 1f else 0.80f),
                modifier = Modifier.size(18.dp))
        }

        Text("Movies", fontSize = 20.sp, fontWeight = FontWeight.Black,
            color = TextPrimary, letterSpacing = 0.3.sp)
    }
}

// Removed duplicate MoviesRow and MoviesCard - now using shared MovieRow


// ═══════════════════════════════════════════════════════════════════════
//  SHIMMER LOADING
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MoviesShimmer() {
    val inf  = rememberInfiniteTransition(label = "sh")
    val anim by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "sv")
    val brush = Brush.horizontalGradient(
        listOf(Color.White.copy(0.05f), Color.White.copy(0.12f), Color.White.copy(0.05f)),
        startX = anim * 1200f, endX = anim * 1200f + 400f)
    Column(modifier = Modifier.fillMaxSize().background(Bg)
        .padding(top = 72.dp, start = 48.dp)) {
        repeat(3) {
            Box(Modifier.width(140.dp).height(16.dp)
                .clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(5) {
                    Box(Modifier.width(150.dp).height(215.dp)
                        .clip(RoundedCornerShape(10.dp)).background(brush))
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ERROR SCREEN
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun MoviesError(onRetry: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Bg).padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("⚠️", fontSize = 40.sp)
            Text("Failed to load movies", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Accent)
                .clickable { onRetry() }.padding(horizontal = 28.dp, vertical = 12.dp)) {
                Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
