package com.example.healthyhabits.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Data Access Object (DAO) med SQL-spørringer mot Room-databasen
@Dao
interface HabitDao {

    // Henter alle vaner som en reaktiv Flow
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    // Henter vaner med tilhørende oppføringer
    @Transaction
    @Query("SELECT * FROM habits")
    fun getHabitsWithEntries(): Flow<List<HabitWithEntries>>

    // Henter alle loggførte oppføringer sortert med nyeste dato først
    @Query("SELECT * FROM habit_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<HabitEntry>>

    // Henter oppføringer knyttet til en bestemt vane-ID
    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId")
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>>

    // Legger til en ny vane
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit)

    // Sletter en vane
    @Delete
    suspend fun deleteHabit(habit: Habit)

    // Legger til eller erstatter en enkelt trenings- eller vaneoppføring
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntry)

    // Legger til flere oppføringer samtidig (f.eks. ved planlegging)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<HabitEntry>)

    // Sletter en enkelt oppføring
    @Delete
    suspend fun deleteEntry(entry: HabitEntry)

    // Sletter alle oppføringer i databasen
    @Query("DELETE FROM habit_entries")
    suspend fun deleteAllEntries()

    // Sletter oppføringer for en spesifisert dato
    @Query("DELETE FROM habit_entries WHERE date = :date")
    suspend fun deleteEntriesByDate(date: String)

    // Oppdaterer en eksisterende oppføring (f.eks. ved fullføring)
    @Update
    suspend fun updateEntry(entry: HabitEntry)
}
