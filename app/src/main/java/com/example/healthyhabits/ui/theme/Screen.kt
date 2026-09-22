package com.example.healthyhabits.ui.theme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Plan : Screen("plan")
    object Graph : Screen("graph")
    object Library : Screen("library")
    object Settings : Screen("settings")

    fun withArgs(vararg args: String): String = buildString {
        append(route)
        args.forEach { append("/$it") }
    }
}
