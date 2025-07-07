package com.kiaranhurley.mediatracker.api.services

import com.kiaranhurley.mediatracker.api.models.IgdbSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface IgdbService {

    companion object {
        const val BASE_URL = "https://api.igdb.com/v4/"
        const val CLIENT_ID = "j9otfot2lil3s2og262w0z9soif80l"
        // Note: You'll need to get a fresh access token periodically
        // This should be moved to a secure location in production
    }

    @POST("games")
    suspend fun searchGames(
        @Header("Client-ID") clientId: String = CLIENT_ID,
        @Header("Authorization") authorization: String, // "Bearer YOUR_ACCESS_TOKEN"
        @Body query: String
    ): Response<IgdbSearchResponse>

    @POST("games")
    suspend fun getGameDetails(
        @Header("Client-ID") clientId: String = CLIENT_ID,
        @Header("Authorization") authorization: String,
        @Body query: String
    ): Response<IgdbSearchResponse>

    @POST("games")
    suspend fun getPopularGames(
        @Header("Client-ID") clientId: String = CLIENT_ID,
        @Header("Authorization") authorization: String,
        @Body query: String = """
            fields name,summary,first_release_date,aggregated_rating,aggregated_rating_count,cover.url,platforms.name,involved_companies.company.name,involved_companies.developer,involved_companies.publisher;
            sort aggregated_rating desc;
            limit 20;
        """.trimIndent()
    ): Response<IgdbSearchResponse>

    @POST("games")
    suspend fun getTopRatedGames(
        @Header("Client-ID") clientId: String = CLIENT_ID,
        @Header("Authorization") authorization: String,
        @Body query: String = """
            fields name,summary,first_release_date,aggregated_rating,aggregated_rating_count,cover.url,platforms.name,involved_companies.company.name,involved_companies.developer,involved_companies.publisher;
            where aggregated_rating_count > 10;
            sort aggregated_rating desc;
            limit 20;
        """.trimIndent()
    ): Response<IgdbSearchResponse>
}