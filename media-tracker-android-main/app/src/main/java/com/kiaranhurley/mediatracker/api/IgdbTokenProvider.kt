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
            println("DEBUG: IgdbTokenProvider - Using cached IGDB token: ${cachedToken?.take(10)}...")
            return cachedToken
        }
        
        // Get new token
        return try {
            println("DEBUG: IgdbTokenProvider - Requesting new IGDB access token...")
            println("DEBUG: IgdbTokenProvider - Client ID: ${clientId.take(10)}...")
            println("DEBUG: IgdbTokenProvider - Client Secret: ${clientSecret.take(10)}...")
            
            val response = igdbAuthService.getAccessToken(
                clientId = clientId,
                clientSecret = clientSecret
            )
            
            println("DEBUG: IgdbTokenProvider - Token request response code: ${response.code()}")
            println("DEBUG: IgdbTokenProvider - Token request response message: ${response.message()}")
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                cachedToken = tokenResponse?.access_token
                // IGDB tokens typically last for about 60 days, but we'll refresh every 30 days to be safe
                tokenExpirationTime = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
                
                println("DEBUG: IgdbTokenProvider - Successfully got IGDB token: ${cachedToken?.take(10)}...")
                println("DEBUG: IgdbTokenProvider - Token expires in: ${tokenResponse?.expires_in} seconds")
                
                cachedToken
            } else {
                println("DEBUG: IgdbTokenProvider - IGDB token request failed: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                println("DEBUG: IgdbTokenProvider - Error body: $errorBody")
                println("DEBUG: IgdbTokenProvider - Check your IGDB client_id and client_secret in strings.xml")
                null
            }
        } catch (e: Exception) {
            println("DEBUG: IgdbTokenProvider - IGDB token exception: ${e.message}")
            println("DEBUG: IgdbTokenProvider - Stack trace:")
            e.printStackTrace()
            null
        }
    }
} 