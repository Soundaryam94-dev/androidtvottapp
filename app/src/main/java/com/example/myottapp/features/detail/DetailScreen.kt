package com.example.myottapp.features.detail

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
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.CastDto
import com.example.myottapp.data.remote.dto.MovieDetailResponse
import com.example.myottapp.data.remote.dto.MovieDto
import com.example.myottapp.data.repository.VideoRepository
import com.example.myottapp.features.mylist.MyListButton
import com.example.myottapp.features.shared.HeroData
import com.example.myottapp.features.shared.HeroSection
import com.example.myottapp.features.shared.HERO_HEIGHT
import com.example.myottapp.features.shared.HeroPlayBtn
import com.example.myottapp.features.shared.HeroOutlineBtn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
data class DetailUiState(
    val movie:      MovieDetailResponse? = null,
    val cast:       List<CastDto>        = emptyList(),
    val similar:    List<MovieDto>       = emptyList(),
    val isLoading:  Boolean              = true,
    val error:      String?              = null,
    val isInMyList: Boolean              = false
)

// ═══════════════════════════════════════════════════════════════════════
//  VIEW MODEL
// ═══════════════════════════════════════════════════════════════════════
class DetailViewModel(private val context: android.content.Context) : ViewModel() {
    private val repository = VideoRepository(context)
    private val _uiState   = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadMovie(movieId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val movieResult   = repository.getMovieDetail(movieId)
                val castResult    = repository.getMovieCast(movieId)
                val similarResult = repository.getSimilarMovies(movieId)
                val inMyList      = repository.isInMyList(movieId)
                _uiState.update {
                    it.copy(
                        movie      = movieResult.getOrNull(),
                        cast       = castResult.getOrNull() ?: emptyList(),
                        similar    = similarResult.getOrNull()?.take(12) ?: emptyList(),
                        isLoading  = false,
                        error      = if (movieResult.isFailure) "Failed to load" else null,
                        isInMyList = inMyList
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleMyList(movieId: Int) {
        viewModelScope.launch {
            val movie = _uiState.value.movie ?: return@launch
            if (_uiState.value.isInMyList) {
                repository.removeFromMyList(movieId)
                _uiState.update { it.copy(isInMyList = false) }
            } else {
                repository.addToMyList(MovieDto(
                    id            = movie.id,
                    title         = movie.title,
                    overview      = movie.overview ?: "",
                    poster_path   = movie.poster_path,
                    backdrop_path = movie.backdrop_path,
                    vote_average  = movie.vote_average ?: 0.0,
                    release_date  = movie.release_date ?: ""
                ))
                _uiState.update { it.copy(isInMyList = true) }
            }
        }
    }
}

class DetailViewModelFactory(private val context: android.content.Context) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetailViewModel(context) as T
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ENTRY POINT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailScreen(
    movieId:       Int,
    onBack:        () -> Unit            = {},
    onPlayMovie:   (Int, String) -> Unit = { _, _ -> },
    onPlayTrailer: (Int, String) -> Unit = { _, _ -> },
    onMovieClick:  (Int, String) -> Unit = { _, _ -> }
) {
    val context   = LocalContext.current
    val viewModel: DetailViewModel = viewModel(factory = DetailViewModelFactory(context))
    val uiState  by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) { viewModel.loadMovie(movieId) }

    Box(modifier = Modifier.fillMaxSize().background(Bg)) {
        when {
            uiState.isLoading ->
                DetailShimmer()
            uiState.error != null && uiState.movie == null ->
                DetailError(message = uiState.error!!, onBack = onBack)
            else -> NetflixDetailContent(
                uiState        = uiState,
                onBack         = onBack,
                onPlayMovie    = onPlayMovie,
                onPlayTrailer  = onPlayTrailer,
                onMovieClick   = onMovieClick,
                onToggleMyList = { viewModel.toggleMyList(movieId) }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  DETAIL CONTENT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun NetflixDetailContent(
    uiState:        DetailUiState,
    onBack:         () -> Unit,
    onPlayMovie:    (Int, String) -> Unit,
    onPlayTrailer:  (Int, String) -> Unit,
    onMovieClick:   (Int, String) -> Unit,
    onToggleMyList: () -> Unit
) {
    val movie       = uiState.movie ?: return
    val scrollState = rememberScrollState()

    val title      = movie.title
    val overview   = movie.overview    ?: ""
    val rating     = movie.vote_average?: 0.0
    val runtime    = movie.runtime     ?: 0
    val year       = movie.release_date?.take(4) ?: ""
    val runtimeStr = if (runtime > 0) "${runtime / 60}h ${runtime % 60}m" else ""
    val tagline    = movie.tagline     ?: ""
    val genres     = movie.genres?.take(3)?.map { it.name } ?: emptyList()

    // ✅ Build HeroData for shared HeroSection
    val heroData = HeroData(
        movieId      = movie.id,
        title        = title,
        overview     = overview,
        tagline      = tagline,
        backdropPath = movie.backdrop_path,
        posterPath   = movie.poster_path,
        rating       = rating,
        year         = year,
        runtime      = runtimeStr,
        genres       = genres
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {

        // ✅ Shared HeroSection — same height as Home screen hero
        // onBack = non-null → shows back button
        // slideIndex = null → no carousel, static backdrop
        HeroSection(
            heroData   = heroData,
            onBack     = onBack,
            slideIndex = null,
            slideCount = 1,
            onDotClick = null,
            actionButtons = { _ ->
                // ✅ Using shared HeroButtons
                HeroPlayBtn(onClick = { onPlayMovie(movie.id, title) })
                HeroOutlineBtn(
                    label   = "Trailer",
                    icon    = Icons.Default.PlayArrow,
                    onClick = { onPlayTrailer(movie.id, title) }
                )
                MyListButton(
                    movieId    = movie.id,
                    title      = title,
                    posterPath = movie.poster_path,
                    rating     = rating
                )
            }
        )

        // ── Below fold ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Bg)
        ) {
            // ── Padded content section ────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            ) {
                if (genres.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    DetailInfoRow(
                        label = "Genres",
                        value = genres.joinToString(" · ")
                    )
                }

                if (uiState.cast.isNotEmpty()) {
                    Spacer(Modifier.height(28.dp))
                    CastRow(
                        cast        = uiState.cast,
                        leftPadding = 0.dp,
                        maxVisible  = 8
                    )
                }

                if (uiState.similar.isNotEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    DetailSectionHeader("More Like This")
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ✅ Similar LazyRow — outside padded column
            // contentPadding handles margins, clip=false prevents scale clipping
            if (uiState.similar.isNotEmpty()) {
                LazyRow(
                    contentPadding        = PaddingValues(
                        start = 48.dp,
                        end   = 48.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { clip = false }
                ) {
                    items(uiState.similar, key = { it.id }) { m ->
                        DetailSimilarCard(
                            movie   = m,
                            onClick = { onMovieClick(m.id, m.title) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ACTION BUTTONS
// ═══════════════════════════════════════════════════════════════════════
// Shared buttons used instead of local definitions

// ═══════════════════════════════════════════════════════════════════════
//  CAST CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailCastCard(member: CastDto) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused  by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.08f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "cast_${member.id}"
    )
    val ctx        = LocalContext.current
    val profileUrl = TmdbApiService.profileUrl(member.profile_path)

    Column(
        modifier = Modifier
            .width(80.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .focusable(interactionSource = interaction)
            .clickable(interaction, null) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(if (isFocused) 12.dp else 2.dp, CircleShape,
                    ambientColor = if (isFocused) Accent.copy(0.5f) else Color.Transparent)
                .clip(CircleShape)
                .background(BgCard)
                .border(if (isFocused) 2.dp else 1.dp,
                    if (isFocused) Accent else Color.White.copy(0.15f), CircleShape)
        ) {
            if (profileUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(profileUrl).crossfade(true).build(),
                    contentDescription = member.name,
                    contentScale       = ContentScale.Crop,
                    alignment          = Alignment.TopCenter,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(member.name.take(1), fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, color = Accent)
                }
            }
        }
        Text(
            text      = member.name.split(" ").first(),
            fontSize  = 10.sp,
            color     = if (isFocused) TextPrimary else TextSecond,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text      = member.character,
            fontSize  = 9.sp,
            color     = TextDim,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis,
            modifier  = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SIMILAR MOVIES CARD
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailSimilarCard(movie: MovieDto, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isFocused  by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        if (isFocused) 1.08f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "sim_${movie.id}"
    )
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .width(160.dp).height(220.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; clip = false }
            .shadow(if (isFocused) 18.dp else 2.dp, RoundedCornerShape(10.dp),
                ambientColor = if (isFocused) Accent.copy(0.5f) else Color.Transparent,
                spotColor    = if (isFocused) Accent.copy(0.8f) else Color.Transparent)
            .clip(RoundedCornerShape(10.dp))
            .border(if (isFocused) 2.dp else 0.dp,
                if (isFocused) Accent else Color.Transparent, RoundedCornerShape(10.dp))
            .background(BgCard)
            .focusable(interactionSource = interaction)
            .clickable(interaction, null, onClick = onClick)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
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
                Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF2A1F0F), BgCard))),
                contentAlignment = Alignment.Center
            ) {
                Text(movie.title.take(2).uppercase(), fontSize = 26.sp,
                    fontWeight = FontWeight.Black, color = Accent.copy(0.4f))
            }
        }

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Transparent,
            0.55f to Color.Transparent,
            1f to Color.Black.copy(0.93f)
        )))

        if (isFocused) {
            Box(Modifier.fillMaxWidth().height(3.dp)
                .align(Alignment.TopCenter).background(Accent))
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
                Text(
                    String.format(java.util.Locale.US, "%.1f", movie.vote_average),
                    fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Gold
                )
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
//  HELPERS
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailSectionHeader(text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier
            .size(width = 3.dp, height = 18.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.verticalGradient(listOf(Accent, Accent.copy(0.3f)))))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, letterSpacing = 0.2.sp)
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, fontSize = 13.sp, color = TextDim,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.width(80.dp))
        Text(value, fontSize = 13.sp, color = TextSecond,
            modifier = Modifier.weight(1f))
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  SHIMMER — uses shared HERO_HEIGHT
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailShimmer() {
    val shimmer by rememberInfiniteTransition(label = "shimmer")
        .animateFloat(0f, 1f,
            infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "s")
    val brush = Brush.horizontalGradient(
        colors = listOf(
            Color.White.copy(0.05f),
            Color.White.copy(0.12f),
            Color.White.copy(0.05f)
        ),
        startX = shimmer * 1200f,
        endX   = shimmer * 1200f + 400f
    )
    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        // ✅ Uses shared HERO_HEIGHT — matches real hero exactly
        Box(Modifier.fillMaxWidth().height(HERO_HEIGHT).background(brush))
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.padding(horizontal = 48.dp)) {
            Box(Modifier.width(120.dp).height(18.dp)
                .clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(brush))
                        Box(Modifier.width(50.dp).height(10.dp)
                            .clip(RoundedCornerShape(3.dp)).background(brush))
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.width(140.dp).height(18.dp)
                .clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) {
                    Box(Modifier.width(160.dp).height(220.dp)
                        .clip(RoundedCornerShape(10.dp)).background(brush))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  ERROR
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun DetailError(message: String, onBack: () -> Unit) {
    Box(
        modifier         = Modifier.fillMaxSize().background(Bg).padding(top = 150.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.padding(48.dp)
        ) {
            Text("⚠️", fontSize = 48.sp)
            Text("Failed to load", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(message, fontSize = 13.sp, color = TextSecond,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Box(modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Accent)
                .clickable { onBack() }
                .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Go Back", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  LEGACY ALIASES
// ═══════════════════════════════════════════════════════════════════════
@Composable fun DetailContent(
    uiState: DetailUiState, onBack: () -> Unit,
    onPlayMovie: (Int, String) -> Unit, onPlayTrailer: (Int, String) -> Unit,
    onMovieClick: (Int, String) -> Unit, onToggleMyList: () -> Unit
) = NetflixDetailContent(
    uiState, onBack, onPlayMovie, onPlayTrailer, onMovieClick, onToggleMyList
)

@Composable fun DetailLoadingScreen() = DetailShimmer()
@Composable fun DetailErrorScreen(message: String, onBack: () -> Unit) =
    DetailError(message, onBack)