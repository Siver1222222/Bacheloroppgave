package com.example.healthyhabits.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.healthyhabits.ui.theme.components.CalendarStrip
import com.example.healthyhabits.ui.theme.components.DashboardWidget
import com.example.healthyhabits.data.local.HabitEntry
import com.example.healthyhabits.data.local.HabitViewModel
import com.example.healthyhabits.data.local.DailyStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onSave: (Int) -> Unit = {}
) {
    val history by viewModel.history.collectAsState()
    val dailyProgression by viewModel.dailyProgression.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val calendarStatus by viewModel.calendarStatus.collectAsState()
    
    val localeNo = Locale("nb", "NO")
    val todayDateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    var selectedDate by remember { mutableStateOf(todayDateStr) }
    var currentWeekOffset by remember { mutableIntStateOf(0) }
    
    var selectedDayDetails by remember { mutableStateOf<String?>(null) }
    var taskToVerify by remember { mutableStateOf<HabitEntry?>(null) }
    var taskToEdit by remember { mutableStateOf<HabitEntry?>(null) }
    var verificationMinutes by remember { mutableStateOf("") }

    var showPlanDialog by remember { mutableStateOf(false) }
    var planName by remember { mutableStateOf("") }
    var planCount by remember { mutableStateOf("") }
    var planDate by remember { mutableStateOf(todayDateStr) }
    var planErrorMessage by remember { mutableStateOf<String?>(null) }

    val neonLime = MaterialTheme.colorScheme.primary
    val lavender = MaterialTheme.colorScheme.secondary
    val softPink = MaterialTheme.colorScheme.tertiary
    val deepDark = MaterialTheme.colorScheme.background

    val selectedDateTasks = history.filter { it.date == selectedDate }

    val displayedMonthYear = remember(currentWeekOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        SimpleDateFormat("MMMM yyyy", localeNo).format(cal.time).replaceFirstChar { it.uppercase() }
    }

    val statusText = when {
        dailyProgression == 0 -> "Klar for en god start? 🚀"
        dailyProgression < 40 -> "God begynnelse! 👟"
        dailyProgression < 70 -> "Du er i flyten nå! 🔥"
        dailyProgression < 100 -> "Nesten i mål! ⚡"
        else -> "MÅL OPPNÅDD! 🎉"
    }

    Box(modifier = Modifier.fillMaxSize().background(deepDark)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Din Oversikt", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = { showPlanDialog = true }, modifier = Modifier.background(neonLime, CircleShape)) {
                        Icon(Icons.Default.Add, contentDescription = "Planlegg", tint = MaterialTheme.colorScheme.onPrimary)
                    }
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
                            IconButton(onClick = { currentWeekOffset = 0; selectedDate = todayDateStr }, modifier = Modifier.size(32.dp)) {
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
                    selectedDate = selectedDate
                ) { date ->
                    selectedDate = date
                    val tasks = viewModel.getDailyTasks(date)
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val isFuture = try {
                        val d = sdf.parse(date)
                        val t = sdf.parse(todayDateStr)
                        d != null && d.after(t)
                    } catch (e: Exception) { false }
                    
                    val info = when {
                        tasks.isNotEmpty() && tasks.all { it.completed } -> "Fullført ✅"
                        tasks.count { it.completed }.toFloat() / tasks.size >= 0.5f -> "Godt i gang 🔵"
                        tasks.any { it.completed } -> "Lav innsats 🟡"
                        tasks.isNotEmpty() -> if (isFuture) "Planlagt ⚪" else "Ikke påbegynt 🟠"
                        else -> "Ingen mål ble lagt til."
                    }
                    val list = tasks.take(10).joinToString("\n") { "${if (it.completed) "✅" else "⭕"} ${it.activityName}" }
                    selectedDayDetails = "Dato: $date\nStatus: $info\n\nOppgaver:\n$list"
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(100.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.CenterStart) {
                        Text(text = statusText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = neonLime)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardWidget(title = "Streak", value = "$currentStreak dager", label = "Hold koken! 🔥", color = softPink, modifier = Modifier.weight(1f))
                    DashboardWidget(title = "Dagens Mål", value = "$dailyProgression%", label = "Fullføringsgrad", color = lavender, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (selectedDateTasks.isNotEmpty()) {
                item {
                    val label = if (selectedDate == todayDateStr) "Dagens oppgaver" else "Oppgaver for $selectedDate"
                    Text(text = "$label (${selectedDateTasks.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                }
                items(selectedDateTasks) { task ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                        TaskItem(
                            task = task, 
                            accentColor = neonLime, 
                            isToday = selectedDate == todayDateStr,
                            onComplete = { taskToVerify = it }, 
                            onEdit = { taskToEdit = it },
                            onDelete = { viewModel.deleteEntry(it) }
                        )
                    }
                }
            } else {
                item {
                    val msg = if (selectedDate == todayDateStr) "Ingen plan for i dag.\nTrykk på + for å planlegge!" else "Ingen mål ble lagt til for denne dagen."
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text(text = msg, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // DIALOGS
        if (selectedDayDetails != null) {
            AlertDialog(onDismissRequest = { selectedDayDetails = null }, confirmButton = { TextButton(onClick = { selectedDayDetails = null }) { Text("Lukk", color = neonLime) } }, title = { Text("Dagens Status") }, text = { Text(selectedDayDetails!!) }, containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onBackground, textContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        }

        if (taskToVerify != null) {
            AlertDialog(
                onDismissRequest = { taskToVerify = null },
                title = { Text("Fullført?", color = MaterialTheme.colorScheme.onBackground) },
                text = {
                    Column {
                        Text("Har du gjennomført '${taskToVerify!!.activityName}'?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = verificationMinutes, onValueChange = { if (it.all { c -> c.isDigit() }) verificationMinutes = it }, placeholder = { Text("Minutter", color = MaterialTheme.colorScheme.onSurfaceVariant) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                    }
                },
                confirmButton = { Button(onClick = { val m = if (verificationMinutes.isEmpty()) 30 else (verificationMinutes.toIntOrNull() ?: 0); viewModel.completeTask(taskToVerify!!, m); taskToVerify = null; verificationMinutes = "" }, colors = ButtonDefaults.buttonColors(containerColor = neonLime, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Bekreft") } },
                dismissButton = { TextButton(onClick = { taskToVerify = null }) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (taskToEdit != null) {
            var n by remember { mutableStateOf(taskToEdit!!.activityName) }
            var m by remember { mutableStateOf(taskToEdit!!.minutes.toString()) }
            AlertDialog(
                onDismissRequest = { taskToEdit = null }, 
                title = { Text(if (taskToEdit!!.completed) "Rediger aktivitet" else "Endre navn", color = MaterialTheme.colorScheme.onBackground) }, 
                text = { 
                    Column {
                        OutlinedTextField(
                            value = n, 
                            onValueChange = { n = it }, 
                            label = { Text("Navn") }, 
                            modifier = Modifier.fillMaxWidth(), 
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime)
                        )
                        if (taskToEdit!!.completed) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = m, 
                                onValueChange = { if (it.all { c -> c.isDigit() }) m = it }, 
                                label = { Text("Minutter") }, 
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(), 
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime)
                            )
                        }
                    }
                }, 
                confirmButton = { 
                    Button(onClick = { 
                        viewModel.updateEntry(taskToEdit!!.copy(activityName = n, minutes = m.toIntOrNull() ?: 0)); 
                        taskToEdit = null 
                    }, colors = ButtonDefaults.buttonColors(containerColor = neonLime, contentColor = MaterialTheme.colorScheme.onPrimary)) { 
                        Text("Lagre") 
                    } 
                }, 
                dismissButton = {
                    TextButton(onClick = { taskToEdit = null }) {
                        Text("Avbryt", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showPlanDialog) {
            AlertDialog(
                onDismissRequest = { showPlanDialog = false },
                title = { Text("Planlegg din dag") },
                text = {
                    Column {
                        OutlinedTextField(value = planName, onValueChange = { planName = it }, label = { Text("Hva er målet?") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = planCount, onValueChange = { if (it.all { c -> c.isDigit() }) planCount = it }, label = { Text("Antall aktiviteter") }, placeholder = { Text("F.eks. 5") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = planDate, onValueChange = { planDate = it; planErrorMessage = null }, label = { Text("Dato") }, modifier = Modifier.fillMaxWidth(), isError = planErrorMessage != null, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedBorderColor = neonLime))
                        if (planErrorMessage != null) Text(text = planErrorMessage!!, color = Color.Red, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        try {
                            val d = sdf.parse(planDate); val t = sdf.parse(todayDateStr)
                            if (d != null && d.before(t)) planErrorMessage = "Kan ikke planlegge i fortiden!"
                            else { viewModel.addPlan(planName, planCount.toIntOrNull() ?: 1, planDate); showPlanDialog = false; planName = ""; planCount = ""; planErrorMessage = null }
                        } catch (e: Exception) { planErrorMessage = "Feil datoformat" }
                    }, colors = ButtonDefaults.buttonColors(containerColor = neonLime, contentColor = MaterialTheme.colorScheme.onPrimary)) { Text("Lagre") }
                },
                dismissButton = { TextButton(onClick = { showPlanDialog = false; planErrorMessage = null }) { Text("Avbryt", color = MaterialTheme.colorScheme.onBackground) } },
                containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun TaskItem(task: HabitEntry, accentColor: Color, isToday: Boolean, onComplete: (HabitEntry) -> Unit, onEdit: (HabitEntry) -> Unit, onDelete: (HabitEntry) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !task.completed && isToday) { onComplete(task) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.completed) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant),
        border = if (!task.completed) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)) else null
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.completed, 
                onCheckedChange = { if (!task.completed && isToday) onComplete(task) }, 
                enabled = isToday || task.completed,
                colors = CheckboxDefaults.colors(checkedColor = accentColor, uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (task.activityName.isBlank()) "Sett inn aktivitet" else task.activityName, color = if (task.activityName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else if (task.completed) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (task.completed) {
                    Text(text = "Fullført på ${task.minutes} min", color = accentColor.copy(alpha = 0.6f), fontSize = 12.sp)
                } else if (!isToday) {
                    Text(text = "Kan kun fullføres på datoen", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 10.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEdit(task) }) { 
                    Icon(Icons.Default.Edit, contentDescription = "Endre", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) 
                }
                if (task.completed) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                } else {
                    IconButton(onClick = { onDelete(task) }) { 
                        Icon(Icons.Default.Delete, contentDescription = "Slett", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) 
                    }
                }
            }
        }
    }
}
