package com.example.healthyhabits.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthyhabits.data.local.DailyStatus
import com.example.healthyhabits.data.local.HabitEntry
import com.example.healthyhabits.data.local.HabitViewModel
import com.example.healthyhabits.ui.theme.components.CalendarStrip
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutPlanScreen(
    viewModel: HabitViewModel
) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val calendarStatus by viewModel.calendarStatus.collectAsState()
    
    val localeNo = Locale("nb", "NO")
    val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    var selectedDate by remember { mutableStateOf(todayStr) }
    var currentWeekOffset by remember { mutableIntStateOf(0) }
    
    var editingEntry by remember { mutableStateOf<HabitEntry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<HabitEntry?>(null) }
    var dateToDeleteAll by remember { mutableStateOf<String?>(null) }

    val neonLime = MaterialTheme.colorScheme.primary
    val lavender = MaterialTheme.colorScheme.secondary
    val deepDark = MaterialTheme.colorScheme.background

    val displayedMonthYear = remember(currentWeekOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        SimpleDateFormat("MMMM yyyy", localeNo).format(cal.time).replaceFirstChar { it.uppercase() }
    }

    val selectedDateTasks = history.filter { it.date == selectedDate }
    
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    fun isPastDate(dateStr: String): Boolean {
        return try {
            val d = sdf.parse(dateStr)
            val t = sdf.parse(todayStr)
            d != null && d.before(t)
        } catch (e: Exception) { false }
    }

    Scaffold(
        containerColor = deepDark,
        floatingActionButton = {
            if (!isPastDate(selectedDate)) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = neonLime,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Legg til plan")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(text = "Treningsplan 📝", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = "Administrer dine mål og planer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = displayedMonthYear, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)).padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentWeekOffset-- }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Forrige uke", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                        if (currentWeekOffset != 0) {
                            IconButton(onClick = { currentWeekOffset = 0; selectedDate = todayStr }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Today, contentDescription = "I dag", tint = neonLime, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.onBackground, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(onClick = { currentWeekOffset++ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Neste uke", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                CalendarStrip(
                    accentColor = neonLime,
                    statusMap = calendarStatus,
                    weekOffset = currentWeekOffset,
                    selectedDate = selectedDate,
                    onDayClick = { selectedDate = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = if (selectedDate == todayStr) "Dagens oppgaver" else selectedDate
                    Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = lavender)
                    if (selectedDateTasks.isNotEmpty()) {
                        IconButton(onClick = { dateToDeleteAll = selectedDate }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Slett alt for dagen", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            if (selectedDateTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Ingen mål ble lagt til for denne dagen.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(selectedDateTasks) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                        PlanCard(entry, onEdit = { editingEntry = entry }, onDelete = { entryToDelete = entry })
                    }
                }
            }
            
            if (isPastDate(selectedDate)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Denne dagen er låst 🔒", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        PlanDialog(selectedDate, onDismiss = { showAddDialog = false }, onSave = { n, c, d -> viewModel.addPlan(n, c, d); showAddDialog = false }, neonLime = neonLime)
    }

    if (editingEntry != null) {
        EditDialog(editingEntry!!, onDismiss = { editingEntry = null }, onSave = { updated -> viewModel.updateEntry(updated); editingEntry = null }, neonLime = neonLime)
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Slett øvelse", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Er du sikker på at du vil slette '${entryToDelete!!.activityName}'?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(onClick = { viewModel.deleteEntry(entryToDelete!!); entryToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = MaterialTheme.colorScheme.onBackground)) { Text("Slett") }
            },
            dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } }
        )
    }

    if (dateToDeleteAll != null) {
        AlertDialog(
            onDismissRequest = { dateToDeleteAll = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Tøm dag", color = Color.Red) },
            text = { Text("Er du sikker på at du vil slette ALLE øvelser for $dateToDeleteAll?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(onClick = { viewModel.deleteEntriesByDate(dateToDeleteAll!!); dateToDeleteAll = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = MaterialTheme.colorScheme.onBackground)) { Text("Slett alle") }
            },
            dismissButton = { TextButton(onClick = { dateToDeleteAll = null }) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } }
        )
    }
}

@Composable
fun PlanCard(entry: HabitEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (entry.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (entry.activityName.isBlank()) "Sett inn aktivitet" else entry.activityName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (entry.activityName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else if (entry.completed) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground)
                Text(text = if (entry.completed) "Fullført på ${entry.minutes} min ✅" else "Venter ⭕", style = MaterialTheme.typography.labelSmall, color = if (entry.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Rediger", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Slett", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
fun PlanDialog(initialDate: String, onDismiss: () -> Unit, onSave: (String, Int, String) -> Unit, neonLime: Color) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("healthy_habits", android.content.Context.MODE_PRIVATE) }
    val libraryActivity = remember { prefs.getString("library_activity", "") ?: "" }
    var name by remember { mutableStateOf(libraryActivity) }
    var count by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val todayStr = sdf.format(Date())

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface, title = { Text("Planlegg din dag", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Mål-navn") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = count, onValueChange = { if (it.all { it.isDigit() }) count = it }, label = { Text("Antall aktiviteter") }, placeholder = { Text("F.eks. 5") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = date, onValueChange = { date = it; errorMessage = null }, label = { Text("Dato") }, modifier = Modifier.fillMaxWidth(), isError = errorMessage != null, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                if (errorMessage != null) Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = { 
                try {
                    val d = sdf.parse(date); val t = sdf.parse(todayStr)
                    if (d != null && d.before(t)) errorMessage = "Kan ikke planlegge i fortiden!"
                    else {
                        onSave(name.trim(), count.toIntOrNull() ?: 1, date)
                        prefs.edit().remove("library_activity").apply()
                    }
                } catch (e: Exception) { errorMessage = "Feil datoformat" }
            }, colors = ButtonDefaults.buttonColors(containerColor = neonLime, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Lagre Plan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } }
    )
}

@Composable
fun EditDialog(entry: HabitEntry, onDismiss: () -> Unit, onSave: (HabitEntry) -> Unit, neonLime: Color) {
    var name by remember { mutableStateOf(entry.activityName) }
    var minutes by remember { mutableStateOf(entry.minutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss, 
        containerColor = MaterialTheme.colorScheme.surface, 
        title = { Text("Rediger aktivitet", color = MaterialTheme.colorScheme.onBackground) },
        text = { 
            Column {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Navn") }, 
                    placeholder = { Text("F.eks. Løping") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime)
                )
                if (entry.completed) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = minutes, 
                        onValueChange = { if (it.all { char -> char.isDigit() }) minutes = it }, 
                        label = { Text("Minutter brukt") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), 
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime)
                    )
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = { onSave(entry.copy(activityName = name, minutes = minutes.toIntOrNull() ?: 0)) }, 
                colors = ButtonDefaults.buttonColors(containerColor = neonLime, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) { 
                Text("Lagre") 
            } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } }
    )
}
