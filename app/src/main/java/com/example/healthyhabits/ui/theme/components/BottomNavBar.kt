package com.example.healthyhabits.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.healthyhabits.ui.theme.Screen

// Bunnmeny-komponent for navigasjon mellom appens hovedskjermer
@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route
    val scheme = MaterialTheme.colorScheme
    val accent = scheme.secondary

    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp, start = 10.dp, end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(40.dp)).background(scheme.surface).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home, "Hjem", route == Screen.Home.route, accent) { navigate(navController, Screen.Home.route) }
            NavItem(Icons.Default.FitnessCenter, "Øvelser", route == Screen.Library.route, accent) { navigate(navController, Screen.Library.route) }
            NavItem(Icons.Default.EditCalendar, "Plan", route == Screen.Plan.route, accent) { navigate(navController, Screen.Plan.route) }
            NavItem(Icons.Default.ShowChart, "Trend", route == Screen.Graph.route, accent) { navigate(navController, Screen.Graph.route) }
            NavItem(Icons.Default.Settings, "Innstillinger", route == Screen.Settings.route, accent) { navigate(navController, Screen.Settings.route) }
        }
    }
}

// Hjelpefunksjon for sikker navigasjon uten å duplisere skjermer i stabelen
private fun navigate(navController: NavController, route: String) {
    if (navController.currentDestination?.route == route) return
    navController.navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Enkeltstående menyelement i bunnmenyen
@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(if (isSelected) selectedColor else Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(23.dp))
    }
}
