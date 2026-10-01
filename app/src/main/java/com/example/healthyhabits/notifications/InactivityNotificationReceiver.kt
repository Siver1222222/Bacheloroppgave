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

// BroadcastReceiver som mottar den daglige sjekk-alarmen fra AlarmManager klokken 17:00
class InactivityNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Avbryter dersom brukeren har deaktivert påminnelser i innstillingene
        if (!NotificationScheduler.isEnabled(context)) return

        // goAsync() gjør det mulig å gjøre asynkron sjekk mot databasen uten at Android avslutter prosessen
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Henter alle registrerte og fullførte treningsøkter fra Room-databasen
                val db = AppDatabase.getDatabase(context.applicationContext)
                val entries = db.habitDao().getAllEntries().first()
                val completed = entries.filter { it.completed }
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                // Finner datoen for brukerens siste registrerte økt
                val lastDate = completed.mapNotNull { sdf.parse(it.date) }
                    .filter { it.time <= System.currentTimeMillis() }
                    .maxByOrNull { it.time }

                val reference = lastDate ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time
                val days = ((System.currentTimeMillis() - reference.time) / (24L * 60L * 60L * 1000L)).toInt()
                
                // Dersom det har gått 2 hele dager (days >= 2), er brukeren på sin 3. inaktive dag.
                // Dette er den siste sjansen brukeren har til å gjennomføre en økt før streaken ryker!
                if (days >= 2) {
                    val prefs = context.getSharedPreferences("healthy_habits", Context.MODE_PRIVATE)
                    val marker = lastDate?.let { sdf.format(it) } ?: "no_activity"

                    // Sørger for at brukeren kun får ETT varsel per inaktive periode
                    if (prefs.getString("last_inactivity_notification", "") != marker) {
                        if (showNotification(context)) {
                            prefs.edit().putString("last_inactivity_notification", marker).apply()
                        }
                    }
                }
            } finally {
                // Frigjør den asynkrone kjøreren
                pendingResult.finish()
            }
        }
    }

    // Bygger og viser push-varselet på telefonen
    private fun showNotification(context: Context): Boolean {
        // Sjekker om varslingstillatelse er gitt på Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inactivity_reminders"

        // Oppretter varslingskanal for Android 8.0 (Oreo) og nyere
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Treningspåminnelser", NotificationManager.IMPORTANCE_DEFAULT))
        }

        // Handling for å åpne appen når brukeren trykker på varselet
        val openApp = PendingIntent.getActivity(
            context, 4104, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Henter navnet til brukeren dersom det er registrert
        val prefs = context.getSharedPreferences("healthy_habits", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "")?.trim().orEmpty()
        val nameSuffix = if (userName.isNotBlank()) ", $userName" else ""

        // Bygger selve varselet
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Siste sjanse til å redde streaken$nameSuffix! 🔥")
            .setContentText("Fullfør dagens plan nå for å unngå å miste treningsvanen du har bygget opp.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        manager.notify(4105, notification)
        return true
    }
}
