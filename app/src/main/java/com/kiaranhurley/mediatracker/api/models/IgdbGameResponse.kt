package com.kiaranhurley.mediatracker.api.models

import com.google.gson.annotations.SerializedName

data class IgdbGameResponse(
    val id: Int,
    val name: String,
    val summary: String?,
    @SerializedName("first_release_date")
    val firstReleaseDate: Long?, // Unix timestamp
    @SerializedName("aggregated_rating")
    val aggregatedRating: Float?,
    @SerializedName("aggregated_rating_count")
    val aggregatedRatingCount: Int?,
    val cover: IgdbCover?,
    val platforms: List<IgdbPlatform>?,
    @SerializedName("involved_companies")
    val involvedCompanies: List<IgdbInvolvedCompany>?
)

data class IgdbCover(
    val id: Int,
    val url: String?
)

data class IgdbPlatform(
    val id: Int,
    val name: String,
    val abbreviation: String?
)

data class IgdbInvolvedCompany(
    val id: Int,
    val company: IgdbCompany,
    val developer: Boolean,
    val publisher: Boolean
)

data class IgdbCompany(
    val id: Int,
    val name: String
)

// IGDB returns arrays directly, but this is for detailed game responses
typealias IgdbGameListResponse = List<IgdbGameResponse>