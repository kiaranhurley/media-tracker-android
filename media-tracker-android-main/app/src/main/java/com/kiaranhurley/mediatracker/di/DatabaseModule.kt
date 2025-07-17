package com.kiaranhurley.mediatracker.di

import android.content.Context
import androidx.room.Room
import com.kiaranhurley.mediatracker.database.MediaTrackerDatabase
import com.kiaranhurley.mediatracker.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMediaTrackerDatabase(
        @ApplicationContext context: Context
    ): MediaTrackerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MediaTrackerDatabase::class.java,
            "media_tracker_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: MediaTrackerDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideFilmDao(database: MediaTrackerDatabase): FilmDao {
        return database.filmDao()
    }

    @Provides
    fun provideGameDao(database: MediaTrackerDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    fun provideRatingDao(database: MediaTrackerDatabase): RatingDao {
        return database.ratingDao()
    }

    @Provides
    fun provideReviewDao(database: MediaTrackerDatabase): ReviewDao {
        return database.reviewDao()
    }

    @Provides
    fun provideWatchListDao(database: MediaTrackerDatabase): WatchListDao {
        return database.watchListDao()
    }

    @Provides
    fun provideCustomListDao(database: MediaTrackerDatabase): CustomListDao {
        return database.customListDao()
    }

    @Provides
    fun provideListItemDao(database: MediaTrackerDatabase): ListItemDao {
        return database.listItemDao()
    }
} 