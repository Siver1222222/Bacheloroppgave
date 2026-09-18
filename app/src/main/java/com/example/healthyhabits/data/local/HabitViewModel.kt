package com.example.healthyhabits.data.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class GraphFilter {
    WEEK, MONTH, YEAR
}

enum class DailyStatus {
    NO_PLAN,         // Grå (ingen plan)
    FAILED,          // Oransje/Rød (Planlagt, men 0% gjort)
    LOW_PERFORMANCE, // Gul (Under 50% gjort)
    PARTIAL,         // Lavendel (50-99% gjort)
    COMPLETED        // Neon Lime (100% gjort)
}

data class ProgressionPoint(
    val timestamp: Long, 
    val score: Float, 
    val label: String,
    val minutes: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val activeDays: Int = 0,
    val range: String? = null,
    val avgPerActivity: Float = 0f // Snitt per aktivitet for denne stolpen
)

data class InsightReport(
    val totalMinutes: Int = 0,
    val avgMinutes: Float = 0f,
    val bestStreak: Int = 0,
    val completedDays: Int = 0,
    val totalCompletedTasks: Int = 0,
    val totalPlannedTasks: Int = 0,
    val avgProgress: Int = 0,
    val avgPerActivity: Float = 0f // Snitt per aktivitet for hele perioden
)

class HabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    val habits = repository.getAllHabits().asLiveData()
    val habitsWithEntries = repository.getHabitsWithEntries().asLiveData()

    private val _graphFilter = MutableStateFlow(GraphFilter.WEEK)
    val graphFilter: StateFlow<GraphFilter> = _graphFilter

    val history: StateFlow<List<HabitEntry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val parsedHistory = history.map { entries ->
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        entries.map { it to (sdf.parse(it.date)?.time ?: 0L) }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dailyProgression: StateFlow<Int> = history.map { entries ->
        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis())
        val dayEntries = entries.filter { it.date == today }
        if (dayEntries.isEmpty()) 0 
        else ((dayEntries.count { it.completed }.toFloat() / dayEntries.size) * 100).toInt()
    }.flowOn(Dispatchers.Default)
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val currentStreak: StateFlow<Int> = history.map { entries ->
        calculateStreak(entries)
    }.flowOn(Dispatchers.Default)
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val calendarStatus: StateFlow<Map<String, DailyStatus>> = history.map { entries ->
        entries.groupBy { it.date }.mapValues { (_, dayEntries) ->
            val total = dayEntries.size
            val completed = dayEntries.count { it.completed }
            val percentage = if (total == 0) 0 else (completed * 100) / total
            when {
                total == 0 -> DailyStatus.NO_PLAN
                percentage == 100 -> DailyStatus.COMPLETED
                percentage >= 50 -> DailyStatus.PARTIAL
                percentage > 0 -> DailyStatus.LOW_PERFORMANCE
                else -> DailyStatus.FAILED
            }
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyMap())
    
    val progressionData: StateFlow<List<ProgressionPoint>> = combine(parsedHistory, _graphFilter) { entries, filter ->
        val justEntries = entries.map { it.first }
        when (filter) {
            GraphFilter.WEEK -> calculateCurrentWeekPoints(justEntries)
            GraphFilter.MONTH -> calculateMonthPoints(justEntries)
            GraphFilter.YEAR -> calculateYearPoints(justEntries)
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val insightReport: StateFlow<InsightReport> = combine(parsedHistory, _graphFilter) { entries, filter ->
        calculateInsightOptimized(entries, filter)
    }.flowOn(Dispatchers.Default)
    .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = InsightReport())

    fun setGraphFilter(filter: GraphFilter) {
        _graphFilter.value = filter
    }

    fun addPlan(name: String, count: Int, date: String) {
        viewModelScope.launch {
            val entries = mutableListOf<HabitEntry>()
            repeat(count) {
                entries.add(HabitEntry(habitId = 1, date = date, activityName = name.trim(), completed = false, minutes = 0))
            }
            repository.insertEntries(entries)
        }
    }

    fun completeTask(entry: HabitEntry, minutes: Int) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(completed = true, minutes = minutes, timestamp = System.currentTimeMillis()))
        }
    }

    fun getDailyTasks(date: String): List<HabitEntry> = history.value.filter { it.date == date }

    private fun calculateInsightOptimized(entries: List<Pair<HabitEntry, Long>>, filter: GraphFilter): InsightReport {
        if (entries.isEmpty()) return InsightReport()
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)
        
        // Definer uken nøyaktig (Mandag til Søndag)
        val weekStartCal = Calendar.getInstance().apply { 
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = weekStartCal.timeInMillis
        val weekEnd = weekStartCal.apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis

        val filtered = entries.filter { (_, time) ->
            when (filter) {
                GraphFilter.WEEK -> time >= weekStart && time < weekEnd
                GraphFilter.MONTH -> {
                    val c = Calendar.getInstance().apply { timeInMillis = time }
                    c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
                }
                GraphFilter.YEAR -> {
                    val c = Calendar.getInstance().apply { timeInMillis = time }
                    c.get(Calendar.YEAR) == currentYear
                }
            }
        }

        val totalPlanned = filtered.size
        val totalCompleted = filtered.count { it.first.completed }

        // Debug-logging for 100% verifisering i Logcat
        android.util.Log.d("HabitStats", "--- Debug Statistikk ($filter) ---")
        android.util.Log.d("HabitStats", "Planlagt (Y): $totalPlanned | Fullført (X): $totalCompleted")
        filtered.forEach { (entry, _) ->
            android.util.Log.d("HabitStats", "ID: ${entry.id} | ${entry.activityName} [${entry.date}] | ${if (entry.completed) "✓" else "⭕"}")
        }

        if (filtered.isEmpty()) return InsightReport(bestStreak = calculateBestStreak(entries.map { it.first }))
        val byDay = filtered.groupBy { it.first.date }
        val totalMins = filtered.sumOf { it.first.minutes }
        val completedDays = byDay.values.count { day -> day.isNotEmpty() && day.all { it.first.completed } }
        
        return InsightReport(
            totalMinutes = totalMins,
            avgMinutes = if (byDay.isNotEmpty()) totalMins.toFloat() / byDay.size else 0f,
            completedDays = completedDays,
            totalCompletedTasks = totalCompleted,
            totalPlannedTasks = totalPlanned,
            bestStreak = calculateBestStreak(entries.map { it.first }),
            avgProgress = if (byDay.isNotEmpty()) (completedDays * 100) / byDay.size else 0,
            avgPerActivity = if (totalCompleted > 0) totalMins.toFloat() / totalCompleted else 0f
        )
    }

    private fun calculateBestStreak(entries: List<HabitEntry>): Int {
        val byDay = entries.groupBy { it.date }
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val sortedDates = byDay.keys.sortedBy { sdf.parse(it)?.time ?: 0L }
        var best = 0; var current = 0; var last: Calendar? = null

        sortedDates.forEach { d ->
            val dayTasks = byDay[d] ?: emptyList()
            if (dayTasks.isNotEmpty() && dayTasks.all { it.completed }) {
                val cur = Calendar.getInstance().apply { time = sdf.parse(d)!! }
                if (last != null) {
                    val diff = (cur.timeInMillis - last!!.timeInMillis) / (1000 * 60 * 60 * 24)
                    if (diff <= 3) current++ else current = 1
                } else current = 1
                last = cur
            } else if (last != null) {
                val cur = Calendar.getInstance().apply { time = sdf.parse(d)!! }
                if ((cur.timeInMillis - last!!.timeInMillis) / (1000 * 60 * 60 * 24) >= 3) current = 0
            }
            if (current > best) best = current
        }
        return best
    }

    private fun calculateStreak(entries: List<HabitEntry>): Int {
        if (entries.isEmpty()) return 0
        val byDay = entries.groupBy { it.date }
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        var streak = 0; var inactive = 0
        
        repeat(1001) { i ->
            val d = sdf.format(cal.time)
            val tasks = byDay[d] ?: emptyList()
            if (tasks.isNotEmpty() && tasks.all { it.completed }) {
                streak++; inactive = 0
            } else {
                // Her lander vi hvis det er ingen oppgaver ELLER hvis det er delvis fullført
                // Vi teller ikke i=0 (i dag) som inaktiv ennå, men alt annet teller mot brudd (3 dager)
                if (i != 0) inactive++
                if (inactive >= 3) return@repeat
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calculateCurrentWeekPoints(entries: List<HabitEntry>): List<ProgressionPoint> {
        val points = mutableListOf<ProgressionPoint>()
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val labelSdf = SimpleDateFormat("EEE", Locale("nb", "NO"))
        val cal = Calendar.getInstance(); cal.firstDayOfWeek = Calendar.MONDAY
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        
        val byDay = entries.groupBy { it.date }
        repeat(7) {
            val d = sdf.format(cal.time); val tasks = byDay[d] ?: emptyList()
            val completed = tasks.count { it.completed }
            val totalMins = tasks.sumOf { it.minutes }
            points.add(
                ProgressionPoint(
                    timestamp = cal.timeInMillis, 
                    score = if (tasks.isEmpty()) 0f else (completed.toFloat()/tasks.size)*100f, 
                    label = labelSdf.format(cal.time).replaceFirstChar { it.uppercase() }, 
                    minutes = totalMins, 
                    completedTasks = completed, 
                    totalTasks = tasks.size, 
                    activeDays = if (tasks.isNotEmpty()) 1 else 0,
                    avgPerActivity = if (completed > 0) totalMins.toFloat() / completed else 0f
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return points
    }

    private fun calculateMonthPoints(entries: List<HabitEntry>): List<ProgressionPoint> {
        val points = mutableListOf<ProgressionPoint>()
        val cal = Calendar.getInstance(); cal.firstDayOfWeek = Calendar.MONDAY
        val curM = cal.get(Calendar.MONTH); val curY = cal.get(Calendar.YEAR)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val rangeSdf = SimpleDateFormat("d. MMM", Locale("nb", "NO"))
        
        val byW = mutableMapOf<Int, MutableList<HabitEntry>>()
        entries.forEach { e ->
            val d = sdf.parse(e.date) ?: return@forEach
            val c = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY; time = d }
            if (c.get(Calendar.MONTH) == curM && c.get(Calendar.YEAR) == curY) byW.getOrPut(c.get(Calendar.WEEK_OF_YEAR)) { mutableListOf() }.add(e)
        }
        
        val weeks = mutableListOf<Int>()
        val temp = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY; set(Calendar.YEAR, curY); set(Calendar.MONTH, curM); set(Calendar.DAY_OF_MONTH, 1) }
        while (temp.get(Calendar.MONTH) == curM) {
            val w = temp.get(Calendar.WEEK_OF_YEAR)
            if (!weeks.contains(w)) weeks.add(w)
            temp.add(Calendar.DAY_OF_MONTH, 1)
        }

        weeks.forEach { w ->
            val tasks = byW[w] ?: emptyList()
            val rCal = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY; set(Calendar.YEAR, curY); set(Calendar.WEEK_OF_YEAR, w); set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
            val start = rangeSdf.format(rCal.time); rCal.add(Calendar.DAY_OF_WEEK, 6); val end = rangeSdf.format(rCal.time)
            
            val totalMins = tasks.sumOf { it.minutes }
            val completed = tasks.count { it.completed }
            
            points.add(
                ProgressionPoint(
                    timestamp = w.toLong(), 
                    score = 0f, 
                    label = "Uke $w", 
                    minutes = totalMins, 
                    completedTasks = completed, 
                    totalTasks = tasks.size, 
                    activeDays = tasks.groupBy { it.date }.size, 
                    range = "$start – $end",
                    avgPerActivity = if (completed > 0) totalMins.toFloat() / completed else 0f
                )
            )
        }
        return points
    }

    private fun calculateYearPoints(entries: List<HabitEntry>): List<ProgressionPoint> {
        val points = mutableListOf<ProgressionPoint>()
        val curY = Calendar.getInstance().get(Calendar.YEAR); val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val labelSdf = SimpleDateFormat("MMMM", Locale("nb", "NO"))
        val byM = mutableMapOf<Int, MutableList<HabitEntry>>()
        entries.forEach { e ->
            val d = sdf.parse(e.date) ?: return@forEach
            val c = Calendar.getInstance().apply { time = d }
            if (c.get(Calendar.YEAR) == curY) byM.getOrPut(c.get(Calendar.MONTH)) { mutableListOf() }.add(e)
        }
        val tempCal = Calendar.getInstance()
        repeat(12) { m ->
            val tasks = byM[m] ?: emptyList(); tempCal.set(Calendar.MONTH, m)
            val byDay = tasks.groupBy { it.date }
            val totalMins = tasks.sumOf { it.minutes }
            val completed = tasks.count { it.completed }
            
            points.add(
                ProgressionPoint(
                    timestamp = m.toLong(), 
                    score = 0f, 
                    label = labelSdf.format(tempCal.time).replaceFirstChar { it.uppercase() }, 
                    minutes = totalMins, 
                    completedTasks = byDay.values.count { d -> d.isNotEmpty() && d.all { it.completed } }, 
                    totalTasks = tasks.size, 
                    activeDays = byDay.size,
                    avgPerActivity = if (completed > 0) totalMins.toFloat() / completed else 0f
                )
            )
        }
        return points
    }

    fun addHabit(habit: Habit) = viewModelScope.launch { repository.insertHabit(habit) }
    fun addEntry(entry: HabitEntry) = viewModelScope.launch { repository.insertEntry(entry) }
    fun deleteAllEntries() = viewModelScope.launch { repository.deleteAllEntries() }
    fun deleteEntriesByDate(date: String) = viewModelScope.launch { repository.deleteEntriesByDate(date) }
    fun updateEntry(entry: HabitEntry) = viewModelScope.launch { repository.updateEntry(entry) }
    fun deleteEntry(entry: HabitEntry) = viewModelScope.launch { repository.deleteEntry(entry) }
}
