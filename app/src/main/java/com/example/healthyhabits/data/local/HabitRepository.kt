package com.example.healthyhabits.data.local

import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao
) {

    // Hent alle vaner
    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    // Hent vaner med entries (til grafer)
    fun getHabitsWithEntries(): Flow<List<HabitWithEntries>> {
        return habitDao.getHabitsWithEntries()
    }

    // Hent entries for én vane
    fun getEntriesForHabit(habitId: Int): Flow<List<HabitEntry>> {
        return habitDao.getEntriesForHabit(habitId)
    }

    // Legg til vane
    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    // Slett vane
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    // Legg til registrering
    suspend fun insertEntry(entry: HabitEntry) {
        habitDao.insertEntry(entry)
    }
}