package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true)
    val gameId: Int = 0,
    val igdbId: Int,
    val name: String,
    val summary: String?,
    val firstReleaseDate: Long?,
    val aggregatedRating: Float?,
    val aggregatedRatingCount: Int?,
    val coverId: Int?,
    val coverUrl: String?,
    val platforms: String?,
    val developer: String?,
    val publisher: String?,
    val createdAt: Date = Date()
)