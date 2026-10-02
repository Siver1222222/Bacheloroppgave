package com.example.healthyhabits.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room Entity som representerer en hovedkategori eller vanetype i databasen
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
