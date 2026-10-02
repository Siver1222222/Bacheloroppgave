package com.example.healthyhabits.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room Database-klasse som definerer tabeller og gir tilgang til DAO-objekter
@Database(entities = [Habit::class, HabitEntry::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    // Returnerer Data Access Object for vaner og oppføringer
    abstract fun habitDao(): HabitDao

    companion object {
        // Singleton-forekomst for å unngå flere samtidige database-tilkoblinger
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
