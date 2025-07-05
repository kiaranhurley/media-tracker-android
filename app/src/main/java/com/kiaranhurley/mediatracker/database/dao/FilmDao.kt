package com.kiaranhurley.mediatracker.database.dao

import androidx.room.*
import com.kiaranhurley.mediatracker.database.entities.Film
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {
    @Query("SELECT * FROM films WHERE filmId = :filmId")
    suspend fun getFilmById(filmId: Int): Film?

    @Query("SELECT * FROM films WHERE tmdbId = :tmdbId")
    suspend fun getFilmByTmdbId(tmdbId: Int): Film?

    @Query("SELECT * FROM films ORDER BY popularity DESC")
    fun getAllFilmsOrderedByPopularity(): Flow<List<Film>>

    @Query("SELECT * FROM films WHERE title LIKE '%' || :searchQuery || '%'")
    suspend fun searchFilms(searchQuery: String): List<Film>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilm(film: Film): Long

    @Update
    suspend fun updateFilm(film: Film)

    @Delete
    suspend fun deleteFilm(film: Film)

    @Query("SELECT * FROM films ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFilms(limit: Int): List<Film>
}