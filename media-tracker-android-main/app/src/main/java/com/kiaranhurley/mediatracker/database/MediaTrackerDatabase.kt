package com.kiaranhurley.mediatracker.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.kiaranhurley.mediatracker.database.dao.*
import com.kiaranhurley.mediatracker.database.entities.*

@Database(
    entities = [
        User::class,
        Film::class,
        Game::class,
        Rating::class,
        Review::class,
        WatchList::class,
        CustomList::class,
        ListItem::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MediaTrackerDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun filmDao(): FilmDao
    abstract fun gameDao(): GameDao
    abstract fun ratingDao(): RatingDao
    abstract fun reviewDao(): ReviewDao
    abstract fun watchListDao(): WatchListDao
    abstract fun customListDao(): CustomListDao
    abstract fun listItemDao(): ListItemDao

    companion object {
        @Volatile
        private var INSTANCE: MediaTrackerDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isPrivate column to reviews table
                database.execSQL("ALTER TABLE reviews ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
                
                // Add isPrivate column to ratings table
                database.execSQL("ALTER TABLE ratings ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes, just added getUserById query method to UserDao
                // Migration is empty but needed to update Room's hash
            }
        }

        fun getDatabase(context: Context): MediaTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaTrackerDatabase::class.java,
                    "media_tracker_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}