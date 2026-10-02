package com.example.healthyhabits.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Room Entity som representerer en enkelt loggført aktivitet eller oppgave for en gitt dato
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
    val minutes: Int = 0, // Tidsbruk i minutter
    val activityName: String = "Aktivitet", // Navnet på aktiviteten (f.eks. "Løping")
    val completed: Boolean = false, // Om aktiviteten er fullført
    val timestamp: Long = System.currentTimeMillis() // Tidsstempel for nøyaktig sporing
)
