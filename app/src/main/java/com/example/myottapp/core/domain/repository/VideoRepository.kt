package com.example.myottapp.data.repository

import android.content.Context
import com.example.myottapp.core.network.NetworkModule
import com.example.myottapp.core.network.SupabaseModule
import com.example.myottapp.core.network.TmdbApiService
import com.example.myottapp.data.local.CachedMovieEntity
import com.example.myottapp.data.local.MyListEntity
import com.example.myottapp.data.local.OttDatabase
import com.example.myottapp.data.local.WatchHistoryEntity
import com.example.myottapp.data.remote.dto.CastDto
import com.example.myottapp.data.remote.dto.MovieDetailResponse
import com.example.myottapp.data.remote.dto.CreditsResponse
import com.example.myottapp.data.remote.dto.MovieDto
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────
//  VideoRepository.kt
//  MyOTT + TMDB + Supabase Storage
//
//  TMDB API  → movie metadata, posters, trailers
//  Supabase  → HLS video files for Play Now
//  Room DB   → My List, watch history (local cache)
// ─────────────────────────────────────────────────────────────────────

class VideoRepository(context: Context) {

    private val api      = NetworkModule.tmdbApi
    private val db       = OttDatabase.getInstance(context)
    private val cacheDao = db.movieCacheDao()
    private val histDao  = db.watchHistoryDao()
    private val listDao  = db.myListDao()

    // ═══════════════════════════════════════════════════════════════════
    //  SUPABASE STORAGE — HLS VIDEO MAP
    //
    //  ✅ YOUR UPLOADED FILES:
    //    Supabase bucket: movies/hls/
    //    Files: playlist.m3u8, playlist0.ts ... playlist12.ts
    //
    //  HOW TO ADD MORE MOVIES:
    //  1. Download video → convert to HLS with FFmpeg
    //  2. Upload folder to Supabase under movies/moviename/
    //  3. Add entry below: tmdbId to "moviename/playlist.m3u8"
    // ═══════════════════════════════════════════════════════════════════

    private val supabaseVideoMap = mapOf(
        // ✅ YOUR UPLOADED HLS — all movies point to this for now
        // Replace with individual movie HLS when you upload more
        872585 to "hls/playlist.m3u8",   // Oppenheimer → your uploaded HLS
        550    to "hls/playlist.m3u8",   // Fight Club  → same HLS (demo)
        27205  to "hls/playlist.m3u8",   // Inception   → same HLS (demo)
        157336 to "hls/playlist.m3u8",   // Interstellar→ same HLS (demo)
        238    to "hls/playlist.m3u8",   // Godfather   → same HLS (demo)
    )

    // Fallback when movieId not in map
    private val fallbackVideoUrl =
        "https://jngqzbroftbvuhxwfgrl.supabase.co/storage/v1/object/public/movies/hls/playlist.m3u8"

    // ✅ Returns Supabase HLS URL for ExoPlayer
    fun getStreamUrl(movieId: Int): String {
        val fileName = supabaseVideoMap[movieId]
        return if (fileName != null) {
            val url = SupabaseModule.getVideoUrl(fileName)
            android.util.Log.d("STREAM", "Playing HLS: $url")
            url
        } else {
            android.util.Log.w("STREAM", "No video for movieId=$movieId, using fallback")
            fallbackVideoUrl
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TMDB — MOVIE LISTS
    // ═══════════════════════════════════════════════════════════════════

    suspend fun getTrendingMovies(): Result<List<MovieDto>> = runCatching {
        val movies = api.getTrendingMovies().results
        cacheDao.clearCategory("trending")
        cacheDao.insertMovies(movies.map { it.toCacheEntity("trending") })
        movies
    }

    suspend fun getPopularMovies(): Result<List<MovieDto>> = runCatching {
        val movies = api.getPopularMovies().results
        cacheDao.clearCategory("popular")
        cacheDao.insertMovies(movies.map { it.toCacheEntity("popular") })
        movies
    }

    suspend fun getTopRatedMovies(): Result<List<MovieDto>> = runCatching {
        val movies = api.getTopRatedMovies().results
        cacheDao.clearCategory("top_rated")
        cacheDao.insertMovies(movies.map { it.toCacheEntity("top_rated") })
        movies
    }

    suspend fun getNowPlayingMovies(): Result<List<MovieDto>> = runCatching {
        api.getNowPlayingMovies().results
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TMDB — MOVIE DETAILS
    // ═══════════════════════════════════════════════════════════════════

    suspend fun getMovieDetail(id: Int): Result<MovieDetailResponse> = runCatching {
        api.getMovieDetail(id)
    }

    suspend fun getMovieCast(id: Int): Result<List<CastDto>> = runCatching {
        api.getMovieCredits(id).cast.take(8)
    }

    suspend fun getTrailerKey(id: Int): Result<String> = runCatching {
        val videos = api.getMovieVideos(id).results
            .filter { it.site == "YouTube" }
        val candidates = listOfNotNull(
            videos.firstOrNull { it.type == "Trailer" && it.official },
            videos.firstOrNull { it.type == "Trailer" },
            videos.firstOrNull { it.type == "Teaser" && it.official },
            videos.firstOrNull { it.type == "Teaser" },
            videos.firstOrNull()
        )
        candidates.firstOrNull()?.key ?: ""
    }

    // ── Genre discovery ────────────────────────────────────────────────
    // TMDB genre IDs: Action=28, Comedy=35, Thriller=53, Drama=18, Sci-Fi=878
    suspend fun getMoviesByGenre(genreId: Int, page: Int = 1): Result<List<MovieDto>> =
        runCatching {
            api.discoverMovies(genreId = genreId, page = page)
                .results.take(15)
        }

    suspend fun getSimilarMovies(id: Int): Result<List<MovieDto>> = runCatching {
        api.getSimilarMovies(id).results
    }

    suspend fun searchMovies(query: String): Result<List<MovieDto>> = runCatching {
        api.searchMovies(query = query).results
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TMDB — FEATURED (Oppenheimer ID: 872585)
    // ═══════════════════════════════════════════════════════════════════

    suspend fun getFeaturedMovieDetail(): Result<MovieDetailResponse> = runCatching {
        api.getMovieDetail(872585)
    }

    suspend fun getFeaturedMovieCast(): Result<List<CastDto>> = runCatching {
        api.getMovieCredits(872585).cast.take(4)
    }

    suspend fun getMovieCredits(movieId: Int): Result<CreditsResponse> = runCatching {
        api.getMovieCredits(movieId)
    }

    suspend fun getFeaturedTrailerKey(): Result<String> = runCatching {
        val videos = api.getMovieVideos(872585).results
        videos.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }?.key ?: ""
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ROOM — WATCH HISTORY
    // ═══════════════════════════════════════════════════════════════════

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> =
        histDao.getWatchHistory()

    suspend fun saveProgress(
        movieId:         Int,
        title:           String,
        posterPath:      String?,
        progress:        Float,
        watchedDuration: Long,
        totalDuration:   Long
    ) {
        histDao.insertOrUpdate(WatchHistoryEntity(
            movieId         = movieId,
            title           = title,
            posterPath      = posterPath,
            progress        = progress,
            watchedDuration = watchedDuration,
            totalDuration   = totalDuration
        ))
    }

    suspend fun getProgress(movieId: Int): WatchHistoryEntity? =
        histDao.getProgress(movieId)

    // ✅ Remove from Continue Watching
    suspend fun removeFromHistory(movieId: Int) =
        histDao.removeFromHistory(movieId)

    // ✅ Save watch progress — call every 30s from PlayerScreen
    suspend fun saveWatchProgress(
        movieId:         Int,
        title:           String,
        posterPath:      String?,
        watchedDuration: Long,
        totalDuration:   Long
    ) {
        val progress = if (totalDuration > 0)
            (watchedDuration.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
        else 0f
        histDao.insertOrUpdate(WatchHistoryEntity(
            movieId         = movieId,
            title           = title,
            posterPath      = posterPath,
            progress        = progress,
            watchedDuration = watchedDuration,
            totalDuration   = totalDuration,
            lastWatched     = System.currentTimeMillis()
        ))
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ROOM — MY LIST
    // ═══════════════════════════════════════════════════════════════════

    fun getMyList(): Flow<List<MyListEntity>> = listDao.getMyList()

    suspend fun addToMyList(movie: MovieDto) {
        listDao.addToList(MyListEntity(
            movieId    = movie.id,
            title      = movie.title,
            posterPath = movie.poster_path
        ))
    }

    suspend fun removeFromMyList(movieId: Int) = listDao.removeFromList(movieId)

    suspend fun isInMyList(movieId: Int): Boolean = listDao.isInMyList(movieId) > 0

    // ═══════════════════════════════════════════════════════════════════
    //  AUTH HELPERS
    // ═══════════════════════════════════════════════════════════════════

    fun getCurrentUserId(): String? = null
    fun isLoggedIn(): Boolean = false

    // ═══════════════════════════════════════════════════════════════════
    //  IMAGE URL HELPERS
    // ═══════════════════════════════════════════════════════════════════

    fun posterUrl(path: String?)   = TmdbApiService.posterUrl(path)
    fun backdropUrl(path: String?) = TmdbApiService.backdropUrl(path)
    fun profileUrl(path: String?)  = TmdbApiService.profileUrl(path)
    fun trailerUrl(key: String)    = "https://www.youtube.com/watch?v=$key"

    // ═══════════════════════════════════════════════════════════════════
    //  EXTENSION
    // ═══════════════════════════════════════════════════════════════════

    private fun MovieDto.toCacheEntity(category: String) = CachedMovieEntity(
        id           = id,
        title        = title,
        overview     = overview,
        posterPath   = poster_path,
        backdropPath = backdrop_path,
        voteAverage  = vote_average,
        releaseDate  = release_date,
        category     = category
    )
}
