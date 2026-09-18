package com.example.healthyhabits.ui.theme.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthyhabits.R
import com.example.healthyhabits.data.local.HabitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class WorkoutItem(
    val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val duration: String,
    val imageRes: Int,
    val description: String,
    val focus: String,
    val equipment: String,
    val suitableFor: String,
    val session: String,
    val benefits: List<String>,
    val tips: List<String>,
    val isCustom: Boolean = false
)

private fun builtIn(
    id: String, name: String, category: String, difficulty: String, duration: String, imageRes: Int,
    description: String, focus: String, equipment: String, suitableFor: String, session: String,
    benefits: List<String>, tips: List<String>
) = WorkoutItem(id, name, category, difficulty, duration, imageRes, description, focus, equipment, suitableFor, session, benefits, tips)

private val workouts = listOf(
    builtIn("running", "Løping", "Kondisjon", "Moderat", "20–60 min", R.drawable.workout_running,
        "Løping er en enkel kondisjonsaktivitet som kan tilpasses fra rolig jogg til raskere økter.",
        "Kondisjon, utholdenhet og bein", "Gode løpesko", "Nybegynnere og erfarne", "5–10 min rolig oppvarming → rolig/moderat løping → 5 min rolig avslutning",
        listOf("Kan forbedre kondisjon over tid", "Enkelt å variere tempo og varighet", "Krever lite utstyr"),
        listOf("Start rolig og øk gradvis", "Velg et tempo du kan holde kontrollert", "Ta en roligere dag hvis kroppen trenger det")),
    builtIn("walking", "Gåtur", "Kondisjon", "Enkel", "20–60 min", R.drawable.workout_walking,
        "Gåtur er en lavterskel aktivitet som passer godt i hverdagen. Tempoet kan økes når du ønsker mer intensitet.",
        "Hverdagsaktivitet, kondisjon og bevegelse", "Gode sko", "Alle nivåer", "5 min rolig gange → 20–50 min tur → rolig avslutning",
        listOf("Lav terskel for å komme i gang", "Kan gjøres nesten hvor som helst", "Passer godt som daglig vane"),
        listOf("Velg en rute du liker", "Prøv raskere tempo i korte perioder", "Gjør turen til en fast del av dagen")),
    builtIn("cycling", "Sykling", "Kondisjon", "Moderat", "30–90 min", R.drawable.workout_cycling,
        "Sykling gir jevn kondisjonstrening og kan gjennomføres ute eller på ergometersykkel.",
        "Kondisjon, utholdenhet og bein", "Sykkel og hjelm ute", "Alle nivåer", "10 min rolig → 20–60 min jevnt tempo → 5–10 min rolig",
        listOf("God kondisjonsaktivitet", "Lett å variere intensiteten", "Kan kombineres med transport"),
        listOf("Start med moderat tempo", "Sjekk at sykkelinnstillingen er komfortabel", "Ta med vann på lengre turer")),
    builtIn("strength", "Styrketrening", "Styrke", "Moderat", "30–60 min", R.drawable.workout_strength,
        "Styrketrening kan gjøres med egen kroppsvekt eller enkelt utstyr. Kontrollert teknikk er viktigere enn å bruke mest mulig belastning.",
        "Styrke, stabilitet og muskulær utholdenhet", "Kroppsvekt eller enkelt utstyr", "Alle nivåer", "Oppvarming → 4–6 enkle øvelser → rolige sett → lett avslutning",
        listOf("Kan styrke muskler", "Kan gjøres hjemme eller på treningssenter", "Enkelt å følge progresjon over tid"),
        listOf("Lær bevegelsene før du øker belastningen", "Bruk rolige og kontrollerte repetisjoner", "La kroppen få tid til å hente seg inn")),
    builtIn("yoga", "Yoga", "Bevegelighet", "Enkel", "15–45 min", R.drawable.workout_yoga,
        "Yoga kombinerer bevegelser, balanse og kontroll. Det finnes både rolige og mer fysisk krevende varianter.",
        "Bevegelighet, balanse og kroppsbevissthet", "Yogamatte er valgfritt", "Alle nivåer", "Rolig oppstart → enkle bevegelser → balanse/lett styrke → rolig avslutning",
        listOf("Kan gi en rolig treningsøkt", "Fokus på bevegelighet og kontroll", "Passer fint som en kort økt"),
        listOf("Beveg deg innenfor et komfortabelt område", "Pust rolig gjennom øvelsene", "Velg en enkel økt når du er nybegynner")),
    builtIn("swimming", "Svømming", "Kondisjon", "Moderat", "30–60 min", R.drawable.workout_swimming,
        "Svømming er en helkroppsaktivitet som kan gjøres i rolig eller høyere tempo og deles opp i lengder og pauser.",
        "Kondisjon, koordinasjon og hele kroppen", "Badetøy og tilgang til basseng", "Alle nivåer", "Rolig oppvarming → korte lengder med pauser → jevnt tempo → rolig avslutning",
        listOf("Aktiverer mange muskelgrupper", "Intensiteten kan styres med tempo og pauser", "Gir variasjon i kondisjonstreningen"),
        listOf("Start med korte lengder", "Ta pauser når du trenger det", "Velg svømmestil som føles komfortabel")),
    builtIn("football", "Fotball", "Lagidrett", "Moderat", "45–90 min", R.drawable.workout_football,
        "Fotball kombinerer løping, retningsendringer, koordinasjon og ferdigheter, og kan være både sosial og variert.",
        "Kondisjon, koordinasjon og samarbeid", "Fotballsko etter underlag", "Alle nivåer", "Oppvarming → pasninger/teknikk → spill → rolig nedtrapping",
        listOf("Gir variert bevegelse", "Trener koordinasjon og ferdigheter", "Sosial aktivitet med andre"),
        listOf("Varm opp før spill", "Drikk vann ved lengre økter", "Tilpass intensiteten etter nivå og dagsform")),
    builtIn("basketball", "Basketball", "Lagidrett", "Moderat", "45–90 min", R.drawable.workout_basketball,
        "Basketball består av løping, stopp, hopp og retningsendringer. Du kan trene alene eller sammen med andre.",
        "Kondisjon, koordinasjon og raske bevegelser", "Basketball og passende sko", "Alle nivåer", "Oppvarming → ballkontroll/skudd → spill eller øvelser → rolig avslutning",
        listOf("Gir variert aktivitet", "Trener koordinasjon og bevegelse", "Kan gjøres som både trening og spill"),
        listOf("Varm opp før raske bevegelser", "Start rolig hvis du er uvant med retningsendringer", "Ta pauser og drikk vann ved behov"))
)

private const val PREFS = "healthy_habits"
private const val FAVORITES_KEY = "library_favorites"
private const val CUSTOM_WORKOUTS_KEY = "custom_workouts"

@Composable
fun WorkoutLibraryScreen(viewModel: HabitViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var customWorkouts by remember { mutableStateOf(loadCustomWorkouts(prefs)) }
    var selected by remember { mutableStateOf<WorkoutItem?>(null) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Alle") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(loadFavorites(prefs)) }
    var addingIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteCustom by remember { mutableStateOf<WorkoutItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }
    val allWorkouts = remember(customWorkouts) { workouts + customWorkouts }

    fun toggleFavorite(workout: WorkoutItem) {
        favorites = if (workout.id in favorites) favorites - workout.id else favorites + workout.id
        saveFavorites(prefs, favorites)
    }

    fun addWorkout(workout: WorkoutItem) {
        if (workout.id in addingIds) return
        addingIds = addingIds + workout.id
        viewModel.addPlan(workout.name, 1, today)
        scope.launch {
            snackbarHostState.showSnackbar("${workout.name} er lagt til i dagens plan")
            delay(650)
            addingIds = addingIds - workout.id
        }
    }

    val categories = listOf("Alle", "Kondisjon", "Styrke", "Bevegelighet", "Lagidrett", "Annet")
    val filtered = allWorkouts.filter {
        val matchesSearch = search.isBlank() || it.name.contains(search, true) || it.category.contains(search, true) || it.focus.contains(search, true)
        val matchesCategory = category == "Alle" || it.category == category
        val matchesFavorite = !showFavoritesOnly || it.id in favorites
        matchesSearch && matchesCategory && matchesFavorite
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (selected == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Treningsbibliotek", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text("Utforsk aktiviteter eller lag din egen.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .62f))
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Søk") },
                        placeholder = { Text("Søk etter aktivitet") },
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${filtered.size} aktiviteter", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .55f), fontSize = 13.sp)
                        Spacer(Modifier.weight(1f))
                        FilterChip(
                            selected = showFavoritesOnly,
                            onClick = { showFavoritesOnly = !showFavoritesOnly },
                            label = { Text("Favoritter") },
                            leadingIcon = { Icon(if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { chip ->
                            FilterChip(selected = category == chip, onClick = { category = chip }, label = { Text(chip) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Lag egen aktivitet", fontWeight = FontWeight.Bold)
                    }
                }

                items(filtered, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        favorite = workout.id in favorites,
                        isAdding = workout.id in addingIds,
                        onDetails = { selected = workout },
                        onFavorite = { toggleFavorite(workout) },
                        onAdd = { addWorkout(workout) },
                        onDelete = if (workout.isCustom) ({ deleteCustom = workout }) else null
                    )
                }

                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 45.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .35f), modifier = Modifier.size(42.dp))
                                Spacer(Modifier.height(10.dp))
                                Text(if (showFavoritesOnly) "Du har ingen favoritter ennå." else "Fant ingen aktiviteter.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = .55f))
                                if (!showFavoritesOnly) {
                                    Spacer(Modifier.height(10.dp))
                                    TextButton(onClick = { showCreateDialog = true }) { Text("Lag en egen aktivitet") }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            WorkoutDetails(
                workout = selected!!,
                favorite = selected!!.id in favorites,
                isAdding = selected!!.id in addingIds,
                onBack = { selected = null },
                onFavorite = { toggleFavorite(selected!!) },
                onAdd = { addWorkout(selected!!) },
                onDelete = if (selected!!.isCustom) ({ deleteCustom = selected!! }) else null,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showCreateDialog) {
        CreateWorkoutDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { newWorkout ->
                val updated = customWorkouts + newWorkout
                customWorkouts = updated
                saveCustomWorkouts(prefs, updated)
                showCreateDialog = false
                scope.launch { snackbarHostState.showSnackbar("${newWorkout.name} er lagt til i biblioteket") }
            },
            existingNames = allWorkouts.map { it.name }
        )
    }

    if (deleteCustom != null) {
        val item = deleteCustom!!
        AlertDialog(
            onDismissRequest = { deleteCustom = null },
            title = { Text("Slett aktivitet?") },
            text = { Text("${item.name} fjernes fra ditt eget bibliotek. Eventuelle planer du allerede har laget blir ikke slettet.") },
            confirmButton = {
                Button(onClick = {
                    customWorkouts = customWorkouts.filterNot { it.id == item.id }
                    favorites = favorites - item.id
                    saveCustomWorkouts(prefs, customWorkouts)
                    saveFavorites(prefs, favorites)
                    if (selected?.id == item.id) selected = null
                    deleteCustom = null
                }) { Text("Slett") }
            },
            dismissButton = { TextButton(onClick = { deleteCustom = null }) { Text("Avbryt") } }
        )
    }
}

private fun loadFavorites(prefs: SharedPreferences): Set<String> =
    prefs.getStringSet(FAVORITES_KEY, emptySet())?.toSet() ?: emptySet()

private fun saveFavorites(prefs: SharedPreferences, favorites: Set<String>) {
    prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply()
}

private fun loadCustomWorkouts(prefs: SharedPreferences): List<WorkoutItem> {
    val raw = prefs.getString(CUSTOM_WORKOUTS_KEY, null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    WorkoutItem(
                        id = o.getString("id"), name = o.getString("name"), category = o.optString("category", "Annet"),
                        difficulty = o.optString("difficulty", "Moderat"), duration = o.optString("duration", "Valgfri"),
                        imageRes = 0, description = o.optString("description", "Egen aktivitet lagt til av brukeren."),
                        focus = o.optString("focus", "Bevegelse og aktivitet"), equipment = o.optString("equipment", "Valgfritt"),
                        suitableFor = o.optString("suitableFor", "Tilpasses eget nivå"), session = o.optString("session", "Tilpass varighet og intensitet etter behov."),
                        benefits = o.optString("benefits", "Fleksibel aktivitet").split("\u001F"),
                        tips = o.optString("tips", "Start rolig og øk gradvis.").split("\u001F"),
                        isCustom = true
                    )
                )
            }
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveCustomWorkouts(prefs: SharedPreferences, items: List<WorkoutItem>) {
    val array = JSONArray()
    items.forEach { w ->
        array.put(JSONObject().apply {
            put("id", w.id); put("name", w.name); put("category", w.category); put("difficulty", w.difficulty)
            put("duration", w.duration); put("description", w.description); put("focus", w.focus); put("equipment", w.equipment)
            put("suitableFor", w.suitableFor); put("session", w.session)
            put("benefits", w.benefits.joinToString("\u001F")); put("tips", w.tips.joinToString("\u001F"))
        })
    }
    prefs.edit().putString(CUSTOM_WORKOUTS_KEY, array.toString()).apply()
}

@Composable
private fun WorkoutCard(
    workout: WorkoutItem, favorite: Boolean, isAdding: Boolean,
    onDetails: () -> Unit, onFavorite: () -> Unit, onAdd: () -> Unit, onDelete: (() -> Unit)?
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(3.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(175.dp).clickable(onClick = onDetails)) {
                WorkoutImage(workout, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .55f)))))
                Surface(Modifier.padding(14.dp).align(Alignment.TopStart), shape = RoundedCornerShape(50.dp), color = Color.Black.copy(alpha = .55f)) {
                    Text(workout.category, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = .45f), RoundedCornerShape(50.dp))) {
                    Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Favoritt", tint = if (favorite) MaterialTheme.colorScheme.primary else Color.White)
                }
                Row(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalAlignment = Alignment.Bottom) {
                    Text(workout.name, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallInfo(workout.difficulty); SmallInfo(workout.duration) }
                Spacer(Modifier.height(10.dp))
                Text(workout.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .78f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Se detaljer") }
                    Button(onClick = onAdd, enabled = !isAdding, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                        if (isAdding) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp)); Text(if (isAdding) "Lagt til" else "Legg til")
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("Slett egen aktivitet")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetails(
    workout: WorkoutItem, favorite: Boolean, isAdding: Boolean, onBack: () -> Unit,
    onFavorite: () -> Unit, onAdd: () -> Unit, onDelete: (() -> Unit)?, modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 120.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(270.dp)) {
                WorkoutImage(workout, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .15f), Color.Black.copy(alpha = .65f)))))
                IconButton(onClick = onBack, modifier = Modifier.padding(14.dp).background(Color.Black.copy(alpha = .55f), RoundedCornerShape(50.dp))) { Icon(Icons.Default.ArrowBack, "Tilbake", tint = Color.White) }
                IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd).padding(14.dp).background(Color.Black.copy(alpha = .55f), RoundedCornerShape(50.dp))) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favoritt", tint = if (favorite) MaterialTheme.colorScheme.primary else Color.White) }
                Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                    Text(workout.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp)); Text(workout.category, color = Color.White.copy(alpha = .85f))
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    InfoChip(workout.difficulty); InfoChip(workout.duration); InfoChip(workout.equipment)
                }
                DetailSection("Om aktiviteten", workout.description)
                DetailSection("Dette trener du", workout.focus)
                DetailSection("Utstyr", workout.equipment)
                DetailSection("Passer for", workout.suitableFor)
                DetailSection("Forslag til økt", workout.session)
                Text("Fordeler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                workout.benefits.forEach { BulletRow(it) }
                Spacer(Modifier.height(20.dp)); Text("Tips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                workout.tips.forEach { BulletRow(it) }
                Spacer(Modifier.height(26.dp))
                Button(onClick = onAdd, enabled = !isAdding, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    if (isAdding) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp)); Text(if (isAdding) "Lagt til i planen" else "Legg til i dagens plan", fontWeight = FontWeight.Bold)
                }
                if (onDelete != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.Delete, null); Spacer(Modifier.width(7.dp)); Text("Slett egen aktivitet")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateWorkoutDialog(onDismiss: () -> Unit, onCreate: (WorkoutItem) -> Unit, existingNames: List<String>) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Annet") }
    var duration by remember { mutableStateOf("30 min") }
    var difficulty by remember { mutableStateOf("Moderat") }
    var description by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("Valgfritt") }
    var error by remember { mutableStateOf("") }
    val categories = listOf("Kondisjon", "Styrke", "Bevegelighet", "Lagidrett", "Annet")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lag egen aktivitet") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 430.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it; error = "" }, label = { Text("Navn *") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { Text("Kategori", fontWeight = FontWeight.SemiBold) }
                item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { categories.forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) } } }
                item { OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Varighet") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = difficulty, onValueChange = { difficulty = it }, label = { Text("Nivå") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Utstyr") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Beskrivelse (valgfritt)") }, minLines = 3, modifier = Modifier.fillMaxWidth()) }
                if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cleanName = name.trim()
                when {
                    cleanName.isBlank() -> error = "Skriv inn et navn på aktiviteten."
                    existingNames.any { it.equals(cleanName, true) } -> error = "Denne aktiviteten finnes allerede."
                    else -> onCreate(
                        WorkoutItem(
                            id = "custom_${UUID.randomUUID()}", name = cleanName, category = category, difficulty = difficulty.ifBlank { "Moderat" },
                            duration = duration.ifBlank { "Valgfri" }, imageRes = 0,
                            description = description.ifBlank { "Egen aktivitet lagt til i Treningsvane." },
                            focus = "Bevegelse og aktivitet", equipment = equipment.ifBlank { "Valgfritt" }, suitableFor = "Tilpasses eget nivå",
                            session = "Start rolig, gjennomfør aktiviteten i ønsket varighet og avslutt rolig.",
                            benefits = listOf("Gir deg frihet til å registrere aktiviteter som ikke finnes i standardbiblioteket", "Kan tilpasses din egen hverdag"),
                            tips = listOf("Start rolig", "Tilpass intensiteten etter dagsform", "Husk pauser og væske ved behov"), isCustom = true
                        )
                    )
                }
            }) { Text("Opprett") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Avbryt") } }
    )
}

@Composable
private fun WorkoutImage(workout: WorkoutItem, modifier: Modifier) {
    if (workout.imageRes != 0) {
        Image(painterResource(workout.imageRes), workout.name, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable private fun DetailSection(title: String, text: String) {
    Spacer(Modifier.height(20.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground); Spacer(Modifier.height(7.dp))
    Text(text, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .75f), lineHeight = 23.sp)
}

@Composable private fun SmallInfo(text: String) {
    Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = .13f)) { Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary) }
}

@Composable private fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(50.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = .15f)) { Text(text, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary) }
}

@Composable private fun BulletRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .75f), lineHeight = 21.sp)
    }
}
