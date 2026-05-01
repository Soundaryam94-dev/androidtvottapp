package com.example.myottapp.features.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myottapp.data.local.WatchHistoryEntity
import com.example.myottapp.data.remote.dto.CastDto
import com.example.myottapp.data.remote.dto.MovieDetailResponse
import com.example.myottapp.data.remote.dto.MovieDto
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.myottapp.features.shared.HeroData

data class HomeUiState(
    val currentTab:        String           = "home",
    val featuredMovie:      MovieDetailResponse?     = null,
    val featuredCast:       List<CastDto>            = emptyList(),
    val featuredTrailerKey: String                   = "",
    val trendingMovies:     List<MovieDto>           = emptyList(),
    val popularMovies:      List<MovieDto>           = emptyList(),
    val topRatedMovies:     List<MovieDto>           = emptyList(),
    val watchHistory:       List<WatchHistoryEntity> = emptyList(),
    val isLoading:          Boolean                  = true,
    val error:              String?                  = null,
    val hasData:            Boolean                  = false,
    val heroIndex:          Int                      = 0,
    val heroSlides:         List<HeroData> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val _uiState   = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
        observeWatchHistory()
        startHeroAutoScroll()
    }

    private fun observeWatchHistory() {
        viewModelScope.launch {
            repository.getWatchHistory()
                .catch { e -> android.util.Log.e("HomeVM", "Watch history: ${e.message}") }
                .collect { history ->
                    _uiState.update { it.copy(watchHistory = history) }
                }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // ✅ KEY FIX: All 6 API calls run in PARALLEL
                // Single state update = zero intermediate renders = no jump
                coroutineScope {
                    val movieDeferred    = async { repository.getFeaturedMovieDetail() }
                    val castDeferred     = async { repository.getFeaturedMovieCast() }
                    val trailerDeferred  = async { repository.getFeaturedTrailerKey() }
                    val trendingDeferred = async { repository.getTrendingMovies() }
                    val popularDeferred  = async { repository.getPopularMovies() }
                    val topRatedDeferred = async { repository.getTopRatedMovies() }

                    // ✅ ONE single update — Compose recomposes exactly once
                    val featured = movieDeferred.await().getOrNull()
                    val trending = trendingDeferred.await().getOrNull() ?: emptyList()

                    val slides = buildHeroSlides(featured, trending)

                    _uiState.update {
                        it.copy(
                            featuredMovie      = featured,
                            featuredCast       = castDeferred.await().getOrNull()    ?: emptyList(),
                            featuredTrailerKey = trailerDeferred.await().getOrNull() ?: "",
                            trendingMovies     = trending,
                            popularMovies      = popularDeferred.await().getOrNull()  ?: emptyList(),
                            topRatedMovies     = topRatedDeferred.await().getOrNull() ?: emptyList(),
                            heroSlides         = slides,
                            isLoading          = false,
                            hasData            = true,
                            error              = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun removeFromHistory(movieId: Int) {
        viewModelScope.launch {
            try { repository.removeFromHistory(movieId) }
            catch (e: Exception) {
                android.util.Log.e("HomeVM", "Remove history: ${e.message}")
            }
        }
    }

    fun retry() = loadContent()

    fun switchTab(tab: String) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateHeroIndex(index: Int) {
        _uiState.update { it.copy(heroIndex = index) }
    }

    private var autoScrollJob: kotlinx.coroutines.Job? = null
    private fun startHeroAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(7_000L)
                val currentSlides = _uiState.value.heroSlides
                if (currentSlides.size > 1) {
                    val nextIdx = (_uiState.value.heroIndex + 1) % currentSlides.size
                    _uiState.update { it.copy(heroIndex = nextIdx) }
                }
            }
        }
    }

    private fun buildHeroSlides(
        featured: MovieDetailResponse?,
        trending: List<MovieDto>
    ): List<HeroData> {
        return buildList {
            featured?.let { m ->
                add(HeroData(
                    movieId      = m.id,
                    title        = m.title,
                    overview     = m.overview ?: "",
                    tagline      = m.tagline ?: "",
                    backdropPath = m.backdrop_path,
                    posterPath   = m.poster_path,
                    rating       = m.vote_average ?: 0.0,
                    year         = m.release_date?.take(4) ?: "",
                    runtime      = m.runtime?.let { r ->
                        if (r > 0) "${r / 60}h ${r % 60}m" else ""
                    } ?: "",
                    genres = m.genres?.take(2)?.map { it.name } ?: emptyList()
                ))
            }
            trending
                .filter { it.id != featured?.id }
                .take(4)
                .forEach { m ->
                    add(HeroData(
                        movieId      = m.id,
                        title        = m.title,
                        overview     = m.overview,
                        backdropPath = m.backdrop_path,
                        posterPath   = m.poster_path,
                        rating       = m.vote_average,
                        year         = m.release_date.take(4)
                    ))
                }
        }
    }
}