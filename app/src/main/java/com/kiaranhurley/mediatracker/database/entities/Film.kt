package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "films")
data class Film(
    @PrimaryKey(autoGenerate = true)
    val filmId: Int = 0,
    val tmdbId: Int,
    val title: String,
    val releaseDate: Date,
    val posterPath: String?,
    val overview: String?,
    val runtime: Int?,
    val popularity: Float,
    val voteAverage: Float,
    val director: String?,
    val genres: String?,
    val cast: String?,
    val productionCompanies: String?,
    val createdAt: Date = Date()
)