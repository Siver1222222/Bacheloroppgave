package com.example.healthyhabits.data.local

import androidx.room.Embedded
import androidx.room.Relation

// Dataklasse for relasjon mellom Habit og tilhørende HabitEntry-oppføringer i Room
data class HabitWithEntries(
    @Embedded val habit: Habit,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val entries: List<HabitEntry>
)
