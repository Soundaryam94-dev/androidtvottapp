package com.example.myottapp.features.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.remote.dto.VideoDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════
//  PLAYER UI STATE
// ═══════════════════════════════════════════════════════════════════════
data class PlayerUiState(
    val isLoading:    Boolean = true,
    val trailerUrl:   String? = null,   // full YouTube URL ready to play
    val trailerKey:   String? = null,   // YouTube video key (e.g. "dQw4w9WgXcQ")
    val movieTitle:   String  = "",
    val error:        String? = null,
    val trailerAvailable: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════
//  PLAYER VIEW MODEL
//  Resolves TMDB movieId → YouTube trailer URL.
//  The UI layer never touches raw URLs or API calls.
// ═══════════════════════════════════════════════════════════════════════
class PlayerViewModel(
    private val apiService: TmdbApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /**
     * Entry point called by PlayerActivity.
     * Accepts movieId + title. Fetches trailer from TMDB.
     */
    fun loadTrailer(movieId: Int, movieTitle: String) {
        if (movieId <= 0) {
            _uiState.update { it.copy(
                isLoading = false,
                error     = "Invalid movie ID",
                movieTitle = movieTitle
            )}
            return
        }

        _uiState.update { it.copy(isLoading = true, movieTitle = movieTitle, error = null) }

        viewModelScope.launch {
            try {
                val response = apiService.getMovieVideos(movieId)

                // ✅ FIX 8: Correct filtering — no broken sortedByDescending hack.
                //    Priority order: Official Trailer → Trailer → Teaser → Clip
                val trailer = pickBestVideo(response.results)

                if (trailer != null) {
                    val youtubeUrl = "https://www.youtube.com/watch?v=${trailer.key}"
                    _uiState.update { it.copy(
                        isLoading        = false,
                        trailerUrl       = youtubeUrl,
                        trailerKey       = trailer.key,
                        trailerAvailable = true,
                        error            = null
                    )}
                } else {
                    // ✅ Edge case: No trailer found — show proper message
                    _uiState.update { it.copy(
                        isLoading        = false,
                        trailerUrl       = null,
                        trailerKey       = null,
                        trailerAvailable = false,
                        error            = "Trailer not available for \"$movieTitle\""
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error     = "Failed to load trailer: ${e.localizedMessage}"
                )}
            }
        }
    }

    /**
     * Picks the best available YouTube video.
     *
     * Priority:
     *   1. Official Trailer  (site=YouTube, type=Trailer, official=true)
     *   2. Any Trailer       (site=YouTube, type=Trailer)
     *   3. Official Teaser   (site=YouTube, type=Teaser,  official=true)
     *   4. Any Teaser        (site=YouTube, type=Teaser)
     *   5. Any Clip          (site=YouTube, type=Clip)
     *   6. First YouTube     (site=YouTube, any type)
     *
     * ✅ FIX: Replaces the broken:
     *    .sortedByDescending { it.type == "Trailer" }
     *    (which compared Boolean to Boolean — unreliable ordering)
     */
    private fun pickBestVideo(videos: List<VideoDto>): VideoDto? {
        val youtubeOnly = videos.filter { it.site.equals("YouTube", ignoreCase = true) }
        if (youtubeOnly.isEmpty()) return null

        return youtubeOnly.firstOrNull { it.type == "Trailer" && it.official == true }
            ?: youtubeOnly.firstOrNull { it.type == "Trailer" }
            ?: youtubeOnly.firstOrNull { it.type == "Teaser"  && it.official == true }
            ?: youtubeOnly.firstOrNull { it.type == "Teaser" }
            ?: youtubeOnly.firstOrNull { it.type == "Clip" }
            ?: youtubeOnly.firstOrNull()
    }

    fun retry(movieId: Int, movieTitle: String) {
        loadTrailer(movieId, movieTitle)
    }

    // ── Factory ───────────────────────────────────────────────────────
    class Factory(private val apiService: TmdbApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
                return PlayerViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
