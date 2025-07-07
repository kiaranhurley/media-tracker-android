package com.kiaranhurley.mediatracker.api

import android.content.Context
import com.kiaranhurley.mediatracker.api.services.TmdbService
import com.kiaranhurley.mediatracker.api.services.IgdbService
import com.kiaranhurley.mediatracker.api.services.IgdbAuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

    // Context for accessing string resources
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // API credentials - loaded from string resources
    private val IGDB_CLIENT_ID: String
        get() = appContext.getString(R.string.igdb_client_id)
    
    private val IGDB_CLIENT_SECRET: String
        get() = appContext.getString(R.string.igdb_client_secret)
    
    private val TMDB_API_KEY: String
        get() = appContext.getString(R.string.tmdb_api_key)

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createTmdbRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TmdbService.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createIgdbRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IgdbService.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createIgdbAuthRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tmdbService: TmdbService by lazy {
        createTmdbRetrofit().create(TmdbService::class.java)
    }
    
    // Helper function to get TMDB API key
    fun getTmdbApiKey(): String = TMDB_API_KEY

    val igdbService: IgdbService by lazy {
        createIgdbRetrofit().create(IgdbService::class.java)
    }
    
    // Helper function to get IGDB client ID
    fun getIgdbClientId(): String = IGDB_CLIENT_ID

    private val igdbAuthService: IgdbAuthService by lazy {
        createIgdbAuthRetrofit().create(IgdbAuthService::class.java)
    }

    // Function to get IGDB access token
    suspend fun getIgdbAccessToken(): String? {
        return try {
            val response = igdbAuthService.getAccessToken(
                clientId = IGDB_CLIENT_ID,
                clientSecret = IGDB_CLIENT_SECRET
            )
            if (response.isSuccessful) {
                response.body()?.access_token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}