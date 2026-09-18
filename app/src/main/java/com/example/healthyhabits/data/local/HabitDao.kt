package com.example.healthyhabits.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Transaction
    @Query("SELECT * FROM habits")
    fun getHabitsWithEntries(): Flow<List<HabitWithEntries>>

    @Query("SELECT * FROM habit_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId")
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<HabitEntry>)

    @Delete
    suspend fun deleteEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM habit_entries WHERE date = :date")
    suspend fun deleteEntriesByDate(date: String)

    @Update
    suspend fun updateEntry(entry: HabitEntry)
}
