package com.example.healthyhabits.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    // Alle vaner som LiveData
    val habits = repository.getAllHabits().asLiveData()

    // Vaner med entries (til grafer)
    val habitsWithEntries = repository.getHabitsWithEntries().asLiveData()

    // Legg til en vane
    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    // Slett en vane
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Legg til en registrering
    fun addEntry(entry: HabitEntry) {
        viewModelScope.launch {
            repository.insertEntry(entry)
        }
    }

    // Hent entries for én spesifikk vane
    fun getEntriesForHabit(habitId: Int) =
        repository.getEntriesForHabit(habitId).asLiveData()
}