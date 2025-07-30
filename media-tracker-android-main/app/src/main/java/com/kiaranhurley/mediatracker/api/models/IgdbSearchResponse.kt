package com.kiaranhurley.mediatracker.api.models

import com.google.gson.annotations.SerializedName

// IGDB returns arrays directly for search, but we can create a simplified search model
typealias IgdbSearchResponse = List<IgdbSearchGame>

// Simplified game data for search results
data class IgdbSearchGame(
    val id: Int,
    val name: String?, // Some games might have null names
    val summary: String?,
    @SerializedName("first_release_date")
    val firstReleaseDate: Long?, // Unix timestamp
    @SerializedName("aggregated_rating")
    val aggregatedRating: Float?,
    val cover: IgdbSearchCover?
)

data class IgdbSearchCover(
    val id: Int,
    val url: String?
)