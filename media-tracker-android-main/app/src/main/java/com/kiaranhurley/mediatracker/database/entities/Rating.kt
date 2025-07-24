package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Rating(
    @PrimaryKey(autoGenerate = true)
    val ratingId: Int = 0,
    val userId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val rating: Float, // 0.0 to 5.0
    val isPrivate: Boolean = false, // New privacy field
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)