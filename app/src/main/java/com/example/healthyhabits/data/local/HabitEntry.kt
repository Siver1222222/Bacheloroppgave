package com.example.healthyhabits.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * HabitEntry - Representerer en enkelt loggføring av aktivitet i databasen.
 */
@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // Dato i formatet "dd.MM.yyyy"
    val minutes: Int = 0,
    val activityName: String = "Aktivitet", // Navnet på hva som ble gjort (f.eks. "Løping")
    val completed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis() // Brukes for nøyaktig beregning av inaktivitet
)
