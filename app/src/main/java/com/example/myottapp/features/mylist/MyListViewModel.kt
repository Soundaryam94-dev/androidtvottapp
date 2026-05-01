package com.example.myottapp.features.mylist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myottapp.data.local.MyListEntity
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────
//  MyListViewModel.kt
//  Place at: features/mylist/MyListViewModel.kt
// ─────────────────────────────────────────────────────────────────────

data class MyListUiState(
    val items:     List<MyListEntity> = emptyList(),
    val isLoading: Boolean            = false,
    val message:   String?            = null   // toast-like feedback
)

class MyListViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app.applicationContext)

    // ✅ Observe Room DB as StateFlow — auto-updates UI on any change
    val uiState: StateFlow<MyListUiState> = repository
        .getMyList()
        .map { items -> MyListUiState(items = items, isLoading = false) }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5000),
            initialValue  = MyListUiState(isLoading = true)
        )

    // ✅ Per-movie saved state map — for instant button toggle (optimistic)
    private val _savedIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedIds: StateFlow<Set<Int>> = _savedIds.asStateFlow()

    init {
        // Sync savedIds with Room on startup
        viewModelScope.launch {
            repository.getMyList().collect { items ->
                _savedIds.value = items.map { it.movieId }.toSet()
            }
        }
    }

    // ✅ Toggle add/remove with optimistic update
    fun toggleMyList(
        movieId:    Int,
        title:      String,
        posterPath: String?,
        rating:     Double = 0.0
    ) {
        val isCurrentlySaved = _savedIds.value.contains(movieId)

        // ✅ Optimistic update — UI changes instantly before DB confirms
        if (isCurrentlySaved) {
            _savedIds.update { it - movieId }
        } else {
            _savedIds.update { it + movieId }
        }

        viewModelScope.launch {
            if (isCurrentlySaved) {
                repository.removeFromMyList(movieId)
            } else {
                repository.addToMyList(
                    com.example.myottapp.data.remote.dto.MovieDto(
                        id           = movieId,
                        title        = title,
                        overview     = "",
                        poster_path  = posterPath,
                        backdrop_path = null,
                        vote_average = rating,
                        release_date = ""
                    )
                )
            }
        }
    }

    fun isInMyList(movieId: Int): Boolean = _savedIds.value.contains(movieId)

    fun removeFromList(movieId: Int) {
        _savedIds.update { it - movieId }
        viewModelScope.launch {
            repository.removeFromMyList(movieId)
        }
    }
}
