package com.example.healthyhabits.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // Hent alle vaner
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    // Hent vaner med alle entries (til grafer)
    @Transaction
    @Query("SELECT * FROM habits")
    fun getHabitsWithEntries(): Flow<List<HabitWithEntries>>

    // Hent entries for én spesifikk vane
    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId")
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>>

    // Legg til en vane
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    // Slett en vane
    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Legg til en registrering
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntry)

    // Slett en registrering
    @Delete
    suspend fun deleteEntry(entry: HabitEntry)
}