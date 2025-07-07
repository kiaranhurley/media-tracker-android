package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val displayName: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val pronouns: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)