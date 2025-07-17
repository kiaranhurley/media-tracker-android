package com.kiaranhurley.mediatracker.api.models

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponse(
    val page: Int,
    val results: List<TmdbSearchMovie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

// Simplified movie data for search results
data class TmdbSearchMovie(
    val id: Int,
    val title: String,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    val overview: String?,
    val popularity: Float,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("genre_ids")
    val genreIds: List<Int>? // Search results only return genre IDs, not full objects
)