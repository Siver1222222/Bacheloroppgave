package com.example.healthyhabits.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthyhabits.data.local.DailyStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarStrip(
    accentColor: Color, 
    statusMap: Map<String, DailyStatus>, 
    weekOffset: Int = 0,
    selectedDate: String,
    onDayClick: (String) -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayDateStr = sdf.format(today.time)
    
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    
    val dayLabels = listOf("M", "T", "O", "T", "F", "L", "S")
    val scheme = MaterialTheme.colorScheme
    val isLightTheme = scheme.background.luminance() > 0.5f

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(7) { i ->
            val currentDate = cal.time
            val dateStr = sdf.format(currentDate)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            val isSelected = dateStr == selectedDate
            val isToday = dateStr == todayDateStr
            val isFuture = currentDate.after(today.time)
            
            val status = statusMap[dateStr] ?: DailyStatus.NO_PLAN
            val circleColor = when (status) {
                DailyStatus.COMPLETED -> Color(0xFFDFFF6D)       // Neon Lime (100%)
                DailyStatus.PARTIAL -> Color(0xFFC3B0FF)         // Lavendel (50-99%)
                DailyStatus.LOW_PERFORMANCE -> Color(0xFFFFEB3B) // Gul (< 50%)
                DailyStatus.FAILED -> {
                    if (isFuture) (if (isLightTheme) Color(0xFFF3F4F2) else Color.White) else Color(0xFFF78B4D) // Hvit for fremtiden, ellers oransje
                }
                DailyStatus.NO_PLAN -> if (isLightTheme) Color(0xFFE3E5E0) else Color(0xFF1E222D)
            }
            val contentColor = when {
                status == DailyStatus.NO_PLAN -> if (isSelected) scheme.onSurface else scheme.onSurfaceVariant
                status == DailyStatus.FAILED && isFuture -> scheme.onSurface
                else -> Color.Black
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onDayClick(dateStr) }) {
                Text(text = dayLabels[i], color = if (isSelected) accentColor else scheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                        .border(
                            width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp, 
                            color = if (isSelected) scheme.onBackground else if (isToday) scheme.outline else Color.Transparent, 
                            shape = CircleShape
                        ), 
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = dayNum.toString(), fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium, color = contentColor)
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

@Composable
fun DashboardWidget(title: String, value: String, label: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(160.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
            Column {
                Text(text = value, fontWeight = FontWeight.Black, color = Color.Black, fontSize = 24.sp)
                Text(text = label, color = Color.Black.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}
