package com.kiaranhurley.mediatracker.api.models

import com.google.gson.annotations.SerializedName

data class TmdbMovieResponse(
    val id: Int,
    val title: String,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    val overview: String?,
    val runtime: Int?,
    val popularity: Float,
    @SerializedName("vote_average")
    val voteAverage: Float,
    val genres: List<TmdbGenre>?,
    @SerializedName("production_companies")
    val productionCompanies: List<TmdbProductionCompany>?
)

data class TmdbGenre(
    val id: Int,
    val name: String
)

data class TmdbProductionCompany(
    val id: Int,
    val name: String
)

// This will be moved to TmdbSearchResponse.kt

data class TmdbCreditsResponse(
    val id: Int,
    val cast: List<TmdbCastMember>,
    val crew: List<TmdbCrewMember>
)

data class TmdbCastMember(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?
)

data class TmdbCrewMember(
    val id: Int,
    val name: String,
    val job: String,
    val department: String
)