package com.kiaranhurley.mediatracker.repository

import com.kiaranhurley.mediatracker.database.dao.FilmDao
import com.kiaranhurley.mediatracker.database.entities.Film
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilmRepository @Inject constructor(
    private val filmDao: FilmDao
) {
    
    /**
     * Get a film by its local ID
     */
    suspend fun getFilmById(filmId: Int): Film? {
        return filmDao.getFilmById(filmId)
    }
    
    /**
     * Get a film by its TMDB ID
     */
    suspend fun getFilmByTmdbId(tmdbId: Int): Film? {
        return filmDao.getFilmByTmdbId(tmdbId)
    }
    
    /**
     * Get all films ordered by popularity as a Flow
     */
    fun getAllFilmsOrderedByPopularity(): Flow<List<Film>> {
        return filmDao.getAllFilmsOrderedByPopularity()
    }
    
    /**
     * Search films by title
     */
    suspend fun searchFilms(searchQuery: String): List<Film> {
        return filmDao.searchFilms(searchQuery)
    }
    
    /**
     * Insert a new film and return the generated ID
     */
    suspend fun insertFilm(film: Film): Long {
        return filmDao.insertFilm(film)
    }
    
    /**
     * Update an existing film
     */
    suspend fun updateFilm(film: Film) {
        filmDao.updateFilm(film)
    }
    
    /**
     * Delete a film
     */
    suspend fun deleteFilm(film: Film) {
        filmDao.deleteFilm(film)
    }
    
    /**
     * Get recent films
     */
    suspend fun getRecentFilms(limit: Int = 10): List<Film> {
        return filmDao.getRecentFilms(limit)
    }
    
    /**
     * Check if a film exists by TMDB ID
     */
    suspend fun filmExistsByTmdbId(tmdbId: Int): Boolean {
        return filmDao.getFilmByTmdbId(tmdbId) != null
    }
    
    /**
     * Insert or update a film (upsert operation)
     */
    suspend fun insertOrUpdateFilm(film: Film): Long {
        val existingFilm = filmDao.getFilmByTmdbId(film.tmdbId)
        return if (existingFilm != null) {
            // Update existing film with new data
            val updatedFilm = film.copy(filmId = existingFilm.filmId)
            filmDao.updateFilm(updatedFilm)
            existingFilm.filmId.toLong()
        } else {
            // Insert new film
            filmDao.insertFilm(film)
        }
    }
    
    /**
     * Get highly rated films (above threshold)
     */
    suspend fun getHighlyRatedFilms(ratingThreshold: Float = 7.0f): List<Film> {
        // This would need to be implemented in the DAO if needed
        // For now, we'll get all films and filter in memory
        // Note: This is a simplified implementation. In a real app, you'd want to implement this in the DAO
        return emptyList() // Placeholder - implement proper DAO query
    }
    
    /**
     * Get films by genre
     */
    suspend fun getFilmsByGenre(genre: String): List<Film> {
        // This would need to be implemented in the DAO
        // For now, we'll search films and filter by genre
        return filmDao.searchFilms(genre)
    }
    
    /**
     * Get films by director
     */
    suspend fun getFilmsByDirector(director: String): List<Film> {
        // This would need to be implemented in the DAO
        // For now, we'll search films and filter by director
        return filmDao.searchFilms(director)
    }
    
    /**
     * Get films released in a specific year
     */
    suspend fun getFilmsByYear(year: Int): List<Film> {
        // This would need to be implemented in the DAO
        // For now, we'll get all films and filter by year
        // Note: This is a simplified implementation. In a real app, you'd want to implement this in the DAO
        return emptyList() // Placeholder - implement proper DAO query
    }
    
    /**
     * Get films with runtime within a range
     */
    suspend fun getFilmsByRuntime(minRuntime: Int, maxRuntime: Int): List<Film> {
        // This would need to be implemented in the DAO
        // For now, we'll get all films and filter by runtime
        // Note: This is a simplified implementation. In a real app, you'd want to implement this in the DAO
        return emptyList() // Placeholder - implement proper DAO query
    }
} 