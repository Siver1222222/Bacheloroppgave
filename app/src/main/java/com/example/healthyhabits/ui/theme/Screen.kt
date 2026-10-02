package com.example.healthyhabits.ui.theme

// Definerer ruter (routes) for navigasjon mellom skjermene i appen
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Plan : Screen("plan")
    object Graph : Screen("graph")
    object Library : Screen("library")
    object Settings : Screen("settings")

    // Hjelpefunksjon for å legge til argumenter i ruter ved behov
    fun withArgs(vararg args: String): String = buildString {
        append(route)
        args.forEach { append("/$it") }
    }
}
