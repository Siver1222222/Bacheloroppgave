package com.example.healthyhabits.ui.theme.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthyhabits.data.local.HabitViewModel
import com.example.healthyhabits.notifications.NotificationScheduler

private const val PREFS = "healthy_habits"
private const val FAVORITES_KEY = "library_favorites"

@Composable
fun SettingsScreen(context: Context, viewModel: HabitViewModel, darkMode: Boolean, onDarkModeChanged: (Boolean) -> Unit) {
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var editingName by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDataDeleteDialog by remember { mutableStateOf(false) }
    var dataDeletedMessage by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationScheduler.isEnabled(context)) }
    val favoriteCount = prefs.getStringSet(FAVORITES_KEY, emptySet())?.size ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Innstillinger", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("Tilpass Treningsvane etter dine behov.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f))
        }
        item {
            SectionTitle("Profil")
            SettingsCard {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Navn", fontWeight = FontWeight.SemiBold)
                    Text(if (name.isBlank()) "Ikke angitt" else name, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
                TextButton(onClick = { editingName = true }) { Text("Endre") }
            }
        }
        item {
            SectionTitle("Utseende")
            SettingsCard {
                Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Mørk modus", fontWeight = FontWeight.SemiBold)
                    Text(if (darkMode) "Mørkt tema er aktivt" else "Lyst tema er aktivt", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
                Switch(checked = darkMode, onCheckedChange = onDarkModeChanged)
            }
        }
        item {
            SectionTitle("Påminnelser")
            SettingsCard {
                Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Treningspåminnelser", fontWeight = FontWeight.SemiBold)
                    Text(if (notificationsEnabled) "Siste sjanse-varsel for streaken din" else "Påminnelser er slått av", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
                Switch(checked = notificationsEnabled, onCheckedChange = { enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        (context as? Activity)?.let {
                            ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 4106)
                        }
                    }
                    notificationsEnabled = enabled
                    NotificationScheduler.setEnabled(context, enabled)
                })
            }
        }
        item {
            SectionTitle("Bibliotek")
            SettingsCard {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Favoritter", fontWeight = FontWeight.SemiBold)
                    Text("$favoriteCount lagrede aktiviteter", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
            }
        }
        item {
            SectionTitle("Data og personvern")
            SettingsCard {
                Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Lokal lagring", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text("Planer, treningsdata, navn og favoritter lagres lokalt på enheten.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f), lineHeight = 20.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("Appen trenger ikke en konto for disse funksjonene.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .45f), fontSize = 12.sp)
                }
            }
        }
        item {
            SectionTitle("Data")
            SettingsCard {
                Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Slett treningsdata", fontWeight = FontWeight.SemiBold)
                    Text("Fjerner planer og registrerte økter, men beholder navn og bibliotek.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
                TextButton(onClick = { showDataDeleteDialog = true }) { Text("Slett") }
            }
        }
        item {
            SectionTitle("Preferanser")
            SettingsCard {
                Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Enkel og lokal app", fontWeight = FontWeight.SemiBold)
                    Text("Ingen konto eller sky-synkronisering er nødvendig.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                }
            }
        }
        item {
            SectionTitle("Om appen")
            SettingsCard {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Treningsvane", fontWeight = FontWeight.SemiBold)
                    Text("Planlegging, vaner og visuell oppfølging.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f))
                    Spacer(Modifier.height(4.dp))
                    Text("Versjon 1.11", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .45f), fontSize = 12.sp)
                }
                TextButton(onClick = { showAboutDialog = true }) { Text("Info") }
            }
        }
        item {
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.RestartAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Tilbakestill innstillinger")
            }
        }
    }

    if (editingName) {
        var draft by remember { mutableStateOf(name) }
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Ditt navn") },
            text = { OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("Navn") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { Button(onClick = { name = draft.trim(); prefs.edit().putString("user_name", name).apply(); editingName = false }) { Text("Lagre") } },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text("Avbryt") } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Tilbakestille innstillinger?") },
            text = { Text("Navn, mørk modus og favoritter blir tilbakestilt. Treningsplaner og registrerte treningsdata påvirkes ikke.") },
            confirmButton = {
                Button(onClick = {
                    name = ""
                    prefs.edit().remove("user_name").remove(FAVORITES_KEY).putBoolean("dark_mode", true).apply()
                    onDarkModeChanged(true)
                    showResetDialog = false
                }) { Text("Tilbakestill") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Avbryt") } }
        )
    }

    if (showDataDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDataDeleteDialog = false },
            title = { Text("Slett treningsdata?") },
            text = { Text("Dette sletter alle planlagte og registrerte treningsaktiviteter fra appen. Navn, favoritter og biblioteket beholdes. Handlingen kan ikke angres.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAllEntries()
                    dataDeletedMessage = true
                    showDataDeleteDialog = false
                }) { Text("Slett data") }
            },
            dismissButton = { TextButton(onClick = { showDataDeleteDialog = false }) { Text("Avbryt") } }
        )
    }

    if (dataDeletedMessage) {
        AlertDialog(
            onDismissRequest = { dataDeletedMessage = false },
            title = { Text("Ferdig") },
            text = { Text("Treningsdataene er slettet. Biblioteket og innstillingene dine er beholdt.") },
            confirmButton = { TextButton(onClick = { dataDeletedMessage = false }) { Text("OK") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Om Treningsvane") },
            text = { Text("Treningsvane er laget for å gjøre det enkelt å planlegge aktiviteter, registrere gjennomførte økter og følge utviklingen over tid. Dataene lagres lokalt på enheten.") },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Lukk") } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
}

@Composable
private fun SettingsCard(content: @Composable RowScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}
