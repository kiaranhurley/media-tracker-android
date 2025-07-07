package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
)
data class Rating(
    @PrimaryKey(autoGenerate = true)
    val ratingId: Int = 0,
    val userId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val rating: Float, // 0.0 to 10.0
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)