package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WatchList(
    @PrimaryKey(autoGenerate = true)
    val watchListId: Int = 0,
    val userId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val addedAt: Date = Date()
)