package com.example.healthyhabits.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.healthyhabits.MainActivity
import com.example.healthyhabits.R
import com.example.healthyhabits.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InactivityNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!NotificationScheduler.isEnabled(context)) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val entries = db.habitDao().getAllEntries().first()
                val completed = entries.filter { it.completed }
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val lastDate = completed.mapNotNull { sdf.parse(it.date) }.filter { it.time <= System.currentTimeMillis() }.maxByOrNull { it.time }
                val reference = lastDate ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time
                val days = ((System.currentTimeMillis() - reference.time) / (24L * 60L * 60L * 1000L)).toInt()
                if (days >= 3) {
                    val prefs = context.getSharedPreferences("healthy_habits", Context.MODE_PRIVATE)
                    val marker = lastDate?.let { sdf.format(it) } ?: "no_activity"
                    if (prefs.getString("last_inactivity_notification", "") != marker) {
                        if (showNotification(context)) {
                            prefs.edit().putString("last_inactivity_notification", marker).apply()
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inactivity_reminders"
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Treningspåminnelser", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val openApp = PendingIntent.getActivity(
            context, 4104, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Det er tre dager siden sist")
            .setContentText("Kom i gang igjen og hold treningsvanen din i gang.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        manager.notify(4105, notification)
        return true
    }
}
