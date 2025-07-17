package com.kiaranhurley.mediatracker.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "list_items",
    foreignKeys = [
        ForeignKey(
            entity = CustomList::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class ListItem(
    @PrimaryKey(autoGenerate = true)
    val listItemId: Int = 0,
    val listId: Int,
    val itemId: Int,
    val itemType: String, // "FILM" or "GAME"
    val addedAt: Date = Date()
)