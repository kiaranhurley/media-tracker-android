package com.kiaranhurley.mediatracker.api

import com.kiaranhurley.mediatracker.api.services.IgdbAuthService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class IgdbTokenProvider @Inject constructor(
    private val igdbAuthService: IgdbAuthService,
    @Named("igdb_client_id") private val clientId: String,
    @Named("igdb_client_secret") private val clientSecret: String
) {
    
    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0
    
    suspend fun getAccessToken(): String? {
        // Check if we have a valid cached token
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return cachedToken
        }
        
        // Get new token
        return try {
            val response = igdbAuthService.getAccessToken(
                clientId = clientId,
                clientSecret = clientSecret
            )
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                cachedToken = tokenResponse?.access_token
                // IGDB tokens typically last for about 60 days, but we'll refresh every 30 days to be safe
                tokenExpirationTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
                cachedToken
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 