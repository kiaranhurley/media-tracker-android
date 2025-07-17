package com.kiaranhurley.mediatracker.api.services

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IgdbAuthService {
    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<IgdbTokenResponse>
}

data class IgdbTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String
)