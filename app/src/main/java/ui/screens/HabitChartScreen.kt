package com.example.healthyhabits.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.healthyhabits.data.local.HabitViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Composable
fun HabitChartScreen(viewModel: HabitViewModel) {

    val habitsWithEntries by viewModel.habitsWithEntries.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vane Oversikt",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                BarChart(context).apply {
                    description.isEnabled = false
                    setDrawGridBackground(false)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    axisRight.isEnabled = false
                    legend.isEnabled = true
                    animateY(1000)
                }
            },
            update = { chart ->
                val entries = habitsWithEntries.mapIndexed { index, habitWithEntries ->
                    BarEntry(
                        index.toFloat(),
                        habitWithEntries.entries.count { it.completed }.toFloat()
                    )
                }

                val dataSet = BarDataSet(entries, "Fullførte vaner").apply {
                    color = Color.rgb(76, 175, 80)
                    valueTextColor = Color.BLACK
                    valueTextSize = 12f
                }

                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Antall fullførte vaner per kategori",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}