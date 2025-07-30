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
    private var isRefreshing = false
    
    suspend fun getAccessToken(): String? {
        // Check if we have a valid cached token
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime && !isRefreshing) {
            println("DEBUG: IgdbTokenProvider - Using cached IGDB token: ${cachedToken?.take(10)}...")
            return cachedToken
        }
        
        // Prevent multiple simultaneous refresh attempts
        if (isRefreshing) {
            println("DEBUG: IgdbTokenProvider - Token refresh already in progress, waiting...")
            // Simple wait mechanism - in production you might want a more sophisticated approach
            var attempts = 0
            while (isRefreshing && attempts < 30) {
                kotlinx.coroutines.delay(100)
                attempts++
            }
            return cachedToken
        }
        
        return refreshToken()
    }
    
    suspend fun refreshTokenOnApiFailure(): String? {
        println("DEBUG: IgdbTokenProvider - API call failed, forcing token refresh...")
        cachedToken = null // Invalidate current token
        return refreshToken()
    }
    
    private suspend fun refreshToken(): String? {
        isRefreshing = true
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
                
                // Use the actual expires_in from response, with fallback to 30 days
                val expiresInSeconds = tokenResponse?.expires_in ?: (30 * 24 * 60 * 60)
                tokenExpirationTime = System.currentTimeMillis() + (expiresInSeconds * 1000L)
                
                println("DEBUG: IgdbTokenProvider - Successfully got IGDB token: ${cachedToken?.take(10)}...")
                println("DEBUG: IgdbTokenProvider - Token expires in: $expiresInSeconds seconds")
                
                cachedToken
            } else {
                println("DEBUG: IgdbTokenProvider - IGDB token request failed: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                println("DEBUG: IgdbTokenProvider - Error body: $errorBody")
                println("DEBUG: IgdbTokenProvider - Possible issues:")
                println("DEBUG: IgdbTokenProvider - 1. Check IGDB client_id in strings.xml: $clientId")
                println("DEBUG: IgdbTokenProvider - 2. Check IGDB client_secret in strings.xml: ${clientSecret.take(10)}...")
                println("DEBUG: IgdbTokenProvider - 3. Verify credentials at https://dev.twitch.tv/console/apps")
                null
            }
        } catch (e: Exception) {
            println("DEBUG: IgdbTokenProvider - IGDB token exception: ${e.message}")
            println("DEBUG: IgdbTokenProvider - Stack trace:")
            e.printStackTrace()
            null
        } finally {
            isRefreshing = false
        }
    }
} 