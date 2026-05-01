package com.example.myottapp.core.network

import com.example.myottapp.BuildConfig
import com.example.myottapp.data.remote.dto.CreditsResponse
import com.example.myottapp.data.remote.dto.MovieDetailResponse
import com.example.myottapp.data.remote.dto.MovieResponse
import com.example.myottapp.data.remote.dto.TvShowResponse
import com.example.myottapp.data.remote.dto.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ─────────────────────────────────────────────────────────────────────
//  TmdbApiService.kt
//  Place at: core/network/TmdbApiService.kt
// ─────────────────────────────────────────────────────────────────────
interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US",
        @Query("page")     page:      Int    = 1
    ): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): MovieResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id")  movieId:   Int,
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): MovieDetailResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id")  movieId:   Int,
        @Query("api_key")  apiKey:    String = API_KEY
    ): CreditsResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id")  movieId:   Int,
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): VideoResponse

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id")  movieId:   Int,
        @Query("api_key")  apiKey:    String = API_KEY
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("query")    query:     String,
        @Query("language") language:  String = "en-US",
        @Query("page")     page:      Int    = 1
    ): MovieResponse

    // ✅ NEW — Genre-based discovery for Movies screen
    // genreId: 28=Action, 35=Comedy, 53=Thriller, 18=Drama, 878=Sci-Fi
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key")      apiKey:    String = API_KEY,
        @Query("with_genres")  genreId:   Int,
        @Query("page")         page:      Int    = 1,
        @Query("sort_by")      sortBy:    String = "popularity.desc",
        @Query("language")     language:  String = "en-US"
    ): MovieResponse

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key")  apiKey:    String = API_KEY,
        @Query("language") language:  String = "en-US"
    ): TvShowResponse

    @GET("trending/tv/week")
    suspend fun getTrendingTvShows(
        @Query("api_key")  apiKey:    String = API_KEY
    ): TvShowResponse

    companion object {
        val API_KEY: String get() = BuildConfig.TMDB_API_KEY

        const val BASE_URL       = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE     = "https://image.tmdb.org/t/p/"
        const val POSTER_W500    = "${IMAGE_BASE}w500"
        const val POSTER_W780    = "${IMAGE_BASE}w780"
        const val BACKDROP_W1280 = "${IMAGE_BASE}w1280"
        const val PROFILE_W185   = "${IMAGE_BASE}w185"

        fun posterUrl(path: String?)   = if (!path.isNullOrEmpty()) "$POSTER_W500$path"    else ""
        fun backdropUrl(path: String?) = if (!path.isNullOrEmpty()) "$BACKDROP_W1280$path" else ""
        fun profileUrl(path: String?)  = if (!path.isNullOrEmpty()) "$PROFILE_W185$path"   else ""
    }
}
