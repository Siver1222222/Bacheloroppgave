package com.example.healthyhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthyhabits.data.local.AppDatabase
import com.example.healthyhabits.data.local.Habit
import com.example.healthyhabits.data.local.HabitEntry
import com.example.healthyhabits.data.local.HabitRepository
import com.example.healthyhabits.data.local.HabitViewModel
import com.example.healthyhabits.ui.theme.TreningsvaneTheme
import com.example.healthyhabits.ui.theme.Screen
import com.example.healthyhabits.ui.theme.components.BottomNavBar
import com.example.healthyhabits.ui.theme.screens.GraphScreen
import com.example.healthyhabits.ui.theme.screens.HomeScreen
import com.example.healthyhabits.ui.theme.screens.SettingsScreen
import com.example.healthyhabits.ui.theme.screens.WorkoutLibraryScreen
import com.example.healthyhabits.ui.theme.screens.WorkoutPlanScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = HabitRepository(database.habitDao())
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HabitViewModel(repository) as T
            }
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("healthy_habits", MODE_PRIVATE) }
            var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }

            TreningsvaneTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val habitViewModel: HabitViewModel = viewModel(factory = viewModelFactory)

                LaunchedEffect(Unit) {
                    habitViewModel.addHabit(
                        Habit(id = 1, name = "Trening", description = "Daglig aktivitet")
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = habitViewModel,
                                onSave = { mins ->
                                    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                                    habitViewModel.addEntry(
                                        HabitEntry(habitId = 1, date = dateStr, minutes = mins)
                                    )
                                }
                            )
                        }
                        composable(Screen.Library.route) {
                            WorkoutLibraryScreen(viewModel = habitViewModel)
                        }
                        composable(Screen.Plan.route) {
                            WorkoutPlanScreen(viewModel = habitViewModel)
                        }
                        composable(Screen.Graph.route) {
                            GraphScreen(viewModel = habitViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                context = context,
                                viewModel = habitViewModel,
                                darkMode = darkMode,
                                onDarkModeChanged = { enabled ->
                                    darkMode = enabled
                                    prefs.edit().putBoolean("dark_mode", enabled).apply()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
