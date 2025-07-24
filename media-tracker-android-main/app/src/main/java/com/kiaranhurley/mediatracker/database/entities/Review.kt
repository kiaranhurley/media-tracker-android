package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
    ],
    indices = [Index(value = ["userId"])]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Int = 0,
    val userId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val title: String?,
    val content: String,
    val rating: Float, // 0.0 to 5.0
    val isPrivate: Boolean = false, // New privacy field
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)