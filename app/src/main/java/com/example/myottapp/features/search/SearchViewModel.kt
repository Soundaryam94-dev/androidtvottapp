package com.example.myottapp.features.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myottapp.data.remote.dto.MovieDto
import com.example.myottapp.data.repository.VideoRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────
//  SearchViewModel.kt
//  Place at: features/search/SearchViewModel.kt
// ─────────────────────────────────────────────────────────────────────

data class SearchUiState(
    val query:      String         = "",
    val results:    List<MovieDto> = emptyList(),
    val isLoading:  Boolean        = false,
    val error:      String?        = null,
    val hasSearched: Boolean       = false   // true after first search
)

@OptIn(FlowPreview::class)
class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = VideoRepository(app.applicationContext)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Internal query flow for debounce
    private val _queryFlow = MutableStateFlow("")

    init {
        // ✅ Debounce: wait 500ms after user stops typing before API call
        viewModelScope.launch {
            _queryFlow
                .debounce(500L)
                .distinctUntilChanged()
                .filter { it.trim().length >= 2 }   // min 2 chars
                .collect { query -> searchMovies(query) }
        }
    }

    fun onQueryChanged(query: String) {
        android.util.Log.d("SEARCH", "▶ Query changed: $query")
        _uiState.update { it.copy(query = query) }
        _queryFlow.value = query

        // Clear results instantly when query is cleared
        if (query.trim().length < 2) {
            _uiState.update { it.copy(
                results     = emptyList(),
                isLoading   = false,
                error       = null,
                hasSearched = false
            )}
        }
    }

    private fun searchMovies(query: String) {
        viewModelScope.launch {
            android.util.Log.d("SEARCH", "▶ Searching: $query")
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.searchMovies(query.trim())
            android.util.Log.d("SEARCH", "▶ Result: ${result}")

            result.fold(
                onSuccess = { movies ->
                    android.util.Log.d("SEARCH", "▶ Found ${movies.size} movies")
                    _uiState.update { it.copy(
                        isLoading   = false,
                        results     = movies,
                        hasSearched = true,
                        error       = null
                    )}
                },
                onFailure = { e ->
                    android.util.Log.d("SEARCH", "▶ Error: ${e.message}")
                    _uiState.update { it.copy(
                        isLoading   = false,
                        results     = emptyList(),
                        hasSearched = true,
                        error       = e.message ?: "Search failed"
                    )}
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.update { SearchUiState() }
        _queryFlow.value = ""
    }
}
