package com.kiaranhurley.mediatracker.di

import android.content.Context
import com.kiaranhurley.mediatracker.R
import com.kiaranhurley.mediatracker.api.IgdbTokenProvider
import com.kiaranhurley.mediatracker.api.services.IgdbAuthService
import com.kiaranhurley.mediatracker.api.services.IgdbService
import com.kiaranhurley.mediatracker.api.services.TmdbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
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

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TmdbService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("igdb")
    fun provideIgdbRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IgdbService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("igdb_auth")
    fun provideIgdbAuthRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbService(@Named("tmdb") retrofit: Retrofit): TmdbService {
        return retrofit.create(TmdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideIgdbService(@Named("igdb") retrofit: Retrofit): IgdbService {
        return retrofit.create(IgdbService::class.java)
    }

    @Provides
    @Singleton
    fun provideIgdbAuthService(@Named("igdb_auth") retrofit: Retrofit): IgdbAuthService {
        return retrofit.create(IgdbAuthService::class.java)
    }

    @Provides
    @Singleton
    fun provideTmdbApiKey(@ApplicationContext context: Context): String {
        return context.getString(R.string.tmdb_api_key)
    }

    @Provides
    @Singleton
    @Named("igdb_client_id")
    fun provideIgdbClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.igdb_client_id)
    }

    @Provides
    @Singleton
    @Named("igdb_client_secret")
    fun provideIgdbClientSecret(@ApplicationContext context: Context): String {
        return context.getString(R.string.igdb_client_secret)
    }

    @Provides
    @Singleton
    fun provideIgdbTokenProvider(
        igdbAuthService: IgdbAuthService,
        @Named("igdb_client_id") clientId: String,
        @Named("igdb_client_secret") clientSecret: String
    ): IgdbTokenProvider {
        return IgdbTokenProvider(igdbAuthService, clientId, clientSecret)
    }
} 