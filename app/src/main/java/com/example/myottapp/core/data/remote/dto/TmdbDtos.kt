package com.example.myottapp.data.remote.dto

import com.google.gson.annotations.SerializedName
// ─────────────────────────────────────────────────────────────────────
//  TmdbDtos.kt
//  Place at: data/remote/dto/TmdbDtos.kt
// ─────────────────────────────────────────────────────────────────────

data class MovieResponse(
    val results: List<MovieDto> = emptyList(),
    val total_pages: Int = 0,
    val total_results: Int = 0,
    val page: Int = 1
)

data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double = 0.0,
    val release_date: String = "",
    val genre_ids: List<Int> = emptyList(),
    val popularity: Double = 0.0
)

data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double = 0.0,
    val release_date: String = "",
    val runtime: Int = 0,
    val genres: List<GenreDto> = emptyList(),
    val tagline: String = "",
    val original_language: String?  = null,
    val vote_count:        Int?     = null,
    val status:            String?  = null,
    val budget:            Long?    = null,
    val revenue:           Long?    = null,
)

data class GenreDto(val id: Int, val name: String)

data class CreditsResponse(
    val id: Int = 0,
    val cast: List<CastDto> = emptyList(),
    val crew: List<CrewDto> = emptyList()
)

data class CastDto(
    val id: Int,
    val name: String,
    val character: String = "",
    val profile_path: String?,
    val order: Int = 0
)

data class CrewDto(
    val id: Int,
    val name: String,
    val job: String = "",
    val department: String = "",
    val profile_path: String?
)

data class VideoResponse(
    val id: Int = 0,
    val results: List<VideoDto> = emptyList()
)

data class VideoDto(
    @SerializedName("id")           val id:          String,
    @SerializedName("key")          val key:         String,
    @SerializedName("name")         val name:        String,
    @SerializedName("site")         val site:        String,
    @SerializedName("type")         val type:        String,
    @SerializedName("official")     val official:    Boolean = false,
    @SerializedName("published_at") val publishedAt: String  = "",
    @SerializedName("size")         val size:        Int     = 1080
)

data class TvShowResponse(
    val results: List<TvShowDto> = emptyList(),
    val total_pages: Int = 0
)

data class TvShowDto(
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double = 0.0,
    val first_air_date: String = ""
)
