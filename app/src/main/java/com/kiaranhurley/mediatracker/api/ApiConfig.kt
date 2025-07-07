package com.kiaranhurley.mediatracker.api

import com.kiaranhurley.mediatracker.api.services.TmdbService
import com.kiaranhurley.mediatracker.api.services.IgdbService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {

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

    val tmdbService: TmdbService by lazy {
        createTmdbRetrofit().create(TmdbService::class.java)
    }

    val igdbService: IgdbService by lazy {
        createIgdbRetrofit().create(IgdbService::class.java)
    }
}