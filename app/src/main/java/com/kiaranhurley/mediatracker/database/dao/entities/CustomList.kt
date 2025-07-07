package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "custom_lists",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomList(
    @PrimaryKey(autoGenerate = true)
    val listId: Int = 0,
    val userId: Int,
    val name: String,
    val description: String?,
    val isPrivate: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)