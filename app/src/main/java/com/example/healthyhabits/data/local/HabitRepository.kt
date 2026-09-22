package com.example.healthyhabits.data.local

import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao
) {

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    fun getHabitsWithEntries(): Flow<List<HabitWithEntries>> = habitDao.getHabitsWithEntries()

    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>> = habitDao.getEntriesForHabit(habitId)

    fun getAllEntries(): Flow<List<HabitEntry>> = habitDao.getAllEntries()

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun insertEntry(entry: HabitEntry) = habitDao.insertEntry(entry)

    suspend fun insertEntries(entries: List<HabitEntry>) = habitDao.insertEntries(entries)

    suspend fun deleteEntry(entry: HabitEntry) = habitDao.deleteEntry(entry)

    suspend fun deleteAllEntries() = habitDao.deleteAllEntries()

    suspend fun deleteEntriesByDate(date: String) = habitDao.deleteEntriesByDate(date)

    suspend fun updateEntry(entry: HabitEntry) = habitDao.updateEntry(entry)
}
