package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Int = 0,
    val userId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val title: String?,
    val content: String,
    val rating: Float, // 0.0 to 10.0
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)