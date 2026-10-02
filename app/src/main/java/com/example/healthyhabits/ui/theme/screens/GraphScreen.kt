package com.example.healthyhabits.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthyhabits.data.local.GraphFilter
import com.example.healthyhabits.data.local.HabitViewModel
import com.example.healthyhabits.data.local.InsightReport
import com.example.healthyhabits.data.local.ProgressionPoint
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun GraphScreen(
    viewModel: HabitViewModel = viewModel()
) {
    val progressionPoints by viewModel.progressionData.collectAsState()
    val selectedFilter by viewModel.graphFilter.collectAsState()
    val insight by viewModel.insightReport.collectAsState()
    
    var selectedPoint by remember { mutableStateOf<ProgressionPoint?>(null) }

    // Tilbakestill valg når filter endres
    LaunchedEffect(selectedFilter) {
        selectedPoint = null
    }

    val neonLime = MaterialTheme.colorScheme.primary
    val softPink = MaterialTheme.colorScheme.tertiary
    val lavender = MaterialTheme.colorScheme.secondary
    val scheme = MaterialTheme.colorScheme
    val isLightTheme = scheme.background.luminance() > 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Trening",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when(selectedFilter) {
                        GraphFilter.WEEK -> "Ukentlig analyse"
                        GraphFilter.MONTH -> "Månedlig analyse"
                        GraphFilter.YEAR -> "Årlig analyse"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        // --- CHART AREA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.surfaceVariant.copy(alpha = if (isLightTheme) 0.65f else 0.35f))
                .padding(16.dp)
        ) {
            if (progressionPoints.isEmpty()) {
                Text(
                    text = "Ingen data tilgjengelig",
                    modifier = Modifier.align(Alignment.Center),
                    color = scheme.onSurfaceVariant
                )
            } else {
                // Beregner snittverdi basert på de aktive søylene som vises i grafen (slik at den stiplete linjen gir mening)
                val activePoints = progressionPoints.filter { it.minutes > 0 }
                val calculatedAvg = if (activePoints.isNotEmpty()) {
                    activePoints.map { it.minutes }.average().toFloat()
                } else 0f

                HabitBarChart(
                    points = progressionPoints,
                    avgValue = calculatedAvg,
                    onPointSelected = { selectedPoint = it }
                )
            }
        }

        // --- TOOLTIP / DETAILS ---
        if (selectedPoint != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = selectedPoint!!.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground
                    )
                    selectedPoint!!.range?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⏱️ Total tid : ${selectedPoint!!.minutes} min",
                        color = Color(0xFF4285F4),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedFilter == GraphFilter.WEEK) {
                            "✅ Fullførte aktiviteter : ${selectedPoint!!.completedTasks}"
                        } else {
                            "📅 Fullførte aktivitetsdager : ${selectedPoint!!.activeDays}"
                        },
                        color = Color(0xFFFFA000),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📈 Snitt per aktivitet : %.1f min".format(selectedPoint!!.avgPerActivity),
                        color = Color(0xFF00C853),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- STATISTICS SECTION ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Statistikk 📊",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val statsItems = listOf(
                InsightItemData("Beste Streak", "${insight.bestStreak}d", Icons.Default.LocalFireDepartment, Color(0xFFFF9800)),
                InsightItemData("Fullført", "${insight.totalCompletedTasks}/${insight.totalPlannedTasks}", Icons.Default.CheckCircle, neonLime),
                InsightItemData("Total Tid", "${insight.totalMinutes} min", Icons.Default.Info, softPink),
                InsightItemData("Snitt per aktivitet", "%.1f min".format(insight.avgPerActivity), Icons.Default.AutoGraph, Color(0xFF58A6FF))
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(160.dp).fillMaxWidth()
            ) {
                items(statsItems) { item ->
                    InsightCardComponent(item)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- FILTERS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterButton("Uke", selectedFilter == GraphFilter.WEEK) { viewModel.setGraphFilter(GraphFilter.WEEK) }
            FilterButton("Måned", selectedFilter == GraphFilter.MONTH) { viewModel.setGraphFilter(GraphFilter.MONTH) }
            FilterButton("År", selectedFilter == GraphFilter.YEAR) { viewModel.setGraphFilter(GraphFilter.YEAR) }
        }
    }
}

@Composable
fun HabitBarChart(
    points: List<ProgressionPoint>,
    avgValue: Float,
    onPointSelected: (ProgressionPoint) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isLightTheme = scheme.background.luminance() > 0.5f
    val chartTextColor = if (isLightTheme) android.graphics.Color.parseColor("#5F6368") else android.graphics.Color.LTGRAY
    val chartAxisColor = if (isLightTheme) android.graphics.Color.parseColor("#A0A4AA") else android.graphics.Color.DKGRAY
    val chartGridColor = if (isLightTheme) android.graphics.Color.parseColor("#D9DDE3") else android.graphics.Color.parseColor("#1A202C")
    val averageLineColor = if (isLightTheme) android.graphics.Color.parseColor("#008C99") else android.graphics.Color.parseColor("#00E5FF")
    val highlightColor = if (isLightTheme) android.graphics.Color.parseColor("#30343B") else android.graphics.Color.WHITE

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setDrawValueAboveBar(false)
                legend.isEnabled = false
                setTouchEnabled(true)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    textColor = chartTextColor
                    textSize = 10f 
                    axisLineColor = chartAxisColor
                    granularity = 1f
                    isGranularityEnabled = true
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = chartGridColor
                    enableGridDashedLine(10f, 10f, 0f)
                    textColor = chartTextColor
                    textSize = 10f
                    axisLineColor = chartAxisColor
                    axisMinimum = 0f
                    setLabelCount(5, false)
                    spaceTop = 25f 
                }

                axisRight.isEnabled = false

                setScaleEnabled(false)
                setPinchZoom(false)
                setDoubleTapToZoomEnabled(false)
                
                animateY(500)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            // Oppdater listeneren i hver 'update' for å sikre at den bruker de nyeste punktene
            chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    val index = e.x.toInt()
                    if (index >= 0 && index < points.size) {
                        onPointSelected(points[index])
                    }
                }
                override fun onNothingSelected() {}
            })

            val barEntries = points.mapIndexed { index, point ->
                BarEntry(index.toFloat(), point.minutes.toFloat()) 
            }

            val labels = points.map { it.label }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.setLabelCount(labels.size, false)
            chart.xAxis.labelRotationAngle = if (labels.size > 10) -45f else 0f

            chart.axisLeft.removeAllLimitLines()
            if (avgValue > 0f) {
                val ll = LimitLine(avgValue, "Snitt: %.0f min".format(avgValue)).apply {
                    lineWidth = 1.5f
                    enableDashedLine(10f, 10f, 0f)
                    lineColor = averageLineColor 
                    textColor = averageLineColor
                    textSize = 10f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                }
                chart.axisLeft.addLimitLine(ll)
                chart.axisLeft.setDrawLimitLinesBehindData(false)
            }

            val dataSet = BarDataSet(barEntries, "Minutter").apply {
                color = android.graphics.Color.parseColor("#2E7DFF") 
                setDrawValues(false)
                highLightAlpha = 80
                highLightColor = highlightColor
            }

            val barData = BarData(dataSet).apply {
                barWidth = 0.6f 
            }
            
            val isDataNew = chart.data == null || chart.data.entryCount != barEntries.size || chart.data.yMax != barData.yMax
            chart.data = barData
            if (isDataNew) {
                chart.animateY(500) 
            } else {
                chart.notifyDataSetChanged()
            }
            chart.invalidate()
        }
    )
}

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) scheme.surfaceVariant else Color.Transparent,
            contentColor = if (selected) scheme.onSurface else scheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, scheme.outline.copy(alpha = 0.55f)) else null
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

data class InsightItemData(val title: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun InsightCardComponent(item: InsightItemData) {
    val scheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outline.copy(alpha = 0.35f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = item.title, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Text(text = item.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = scheme.onBackground)
            }
        }
    }
}
