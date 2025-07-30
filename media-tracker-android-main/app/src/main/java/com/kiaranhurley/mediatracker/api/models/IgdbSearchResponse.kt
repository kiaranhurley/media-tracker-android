package com.kiaranhurley.mediatracker.api.models

import com.google.gson.annotations.SerializedName

// IGDB returns arrays directly for search, but we can create a simplified search model
typealias IgdbSearchResponse = List<IgdbSearchGame>

// Comprehensive game data for search results - matches IgdbGameResponse
data class IgdbSearchGame(
    val id: Int,
    val name: String?, // Some games might have null names
    val summary: String?,
    @SerializedName("first_release_date")
    val firstReleaseDate: Long?, // Unix timestamp
    @SerializedName("aggregated_rating")
    val aggregatedRating: Float?,
    @SerializedName("aggregated_rating_count")
    val aggregatedRatingCount: Int?,
    val cover: IgdbSearchCover?,
    val platforms: List<IgdbSearchPlatform>?,
    @SerializedName("involved_companies")
    val involvedCompanies: List<IgdbSearchInvolvedCompany>?
)

data class IgdbSearchCover(
    val id: Int,
    val url: String?,
    val image_id: String?, // IGDB image ID for constructing URLs
    val width: Int?,
    val height: Int?
)

data class IgdbSearchPlatform(
    val id: Int,
    val name: String,
    val abbreviation: String?
)

data class IgdbSearchInvolvedCompany(
    val id: Int,
    val company: IgdbSearchCompany,
    val developer: Boolean,
    val publisher: Boolean
)

data class IgdbSearchCompany(
    val id: Int,
    val name: String
)