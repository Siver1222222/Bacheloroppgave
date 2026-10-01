package com.example.healthyhabits

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
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
import com.example.healthyhabits.notifications.NotificationScheduler
import com.example.healthyhabits.ui.theme.screens.GraphScreen
import com.example.healthyhabits.ui.theme.screens.HomeScreen
import com.example.healthyhabits.ui.theme.screens.SettingsScreen
import com.example.healthyhabits.ui.theme.screens.WorkoutLibraryScreen
import com.example.healthyhabits.ui.theme.screens.WorkoutPlanScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Hovedaktiviteten som starter appen (Entry Point)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Oppretter Room-databasen for lokal lagring på telefonen
        val database = AppDatabase.getDatabase(applicationContext)
        
        // Oppretter repository som håndterer dataflyt mellom database og ViewModel
        val repository = HabitRepository(database.habitDao())

        // ViewModelFactory brukes til å sende repository inn i HabitViewModel
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HabitViewModel(repository) as T
            }
        }

        // Aktiverer kant-til-kant skjermvisning (statusbar og navigasjonsbar)
        enableEdgeToEdge()

        // Sjekker og ber om varslingstillatelse dersom enheten kjører Android 13 eller nyere
        if (NotificationScheduler.isEnabled(this) &&
            android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4106)
        }
        
        // Planlegger den daglige streak-påminnelsen klokken 17:00
        NotificationScheduler.schedule(this)

        // Setter opp brukergrensesnittet med Jetpack Compose
        setContent {
            val context = LocalContext.current
            
            // Henter lagret mørk/lys tema-preferanse fra SharedPreferences
            val prefs = remember { context.getSharedPreferences("healthy_habits", MODE_PRIVATE) }
            var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", true)) }

            // Tilpasser fargen på systemlinjene basert på om mørk modus er aktiv
            LaunchedEffect(darkMode) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !darkMode
                controller.isAppearanceLightNavigationBars = !darkMode
            }

            // Bruker appens egendefinerte tema (TreningsvaneTheme)
            TreningsvaneTheme(darkTheme = darkMode) {
                // Navikasjonskontroller for å bytte mellom skjermer
                val navController = rememberNavController()
                val habitViewModel: HabitViewModel = viewModel(factory = viewModelFactory)

                // Legger til standard vanekategori i databasen hvis den ikke finnes fra før
                LaunchedEffect(Unit) {
                    habitViewModel.addHabit(
                        Habit(id = 1, name = "Trening", description = "Daglig aktivitet")
                    )
                }

                // Scaffold gir standard layout med bunnmeny (BottomNavBar)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    // NavHost styrer hvilken skjerm som skal vises basert på valgt rute
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Hjem-skjermen (Dashboard)
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
                        // Treningsbiblioteket
                        composable(Screen.Library.route) {
                            WorkoutLibraryScreen(viewModel = habitViewModel)
                        }
                        // Planleggingsskjermen
                        composable(Screen.Plan.route) {
                            WorkoutPlanScreen(viewModel = habitViewModel)
                        }
                        // Statistikk og grafskjermen
                        composable(Screen.Graph.route) {
                            GraphScreen(viewModel = habitViewModel)
                        }
                        // Innstillingsskjermen
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
