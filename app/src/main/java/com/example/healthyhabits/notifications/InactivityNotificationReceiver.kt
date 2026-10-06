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

        val isTest = intent?.getBooleanExtra("is_test", false) == true

        // goAsync() gjør det mulig å gjøre asynkron sjekk mot databasen uten at Android avslutter prosessen
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Henter alle registrerte og fullførte treningsøkter fra Room-databasen
                val db = AppDatabase.getDatabase(context.applicationContext)
                val entries = db.habitDao().getAllEntries().first()
                val completed = entries.filter { it.completed }
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                // Dagens dato satt til midnatt (00:00:00)
                val todayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Finner datoen for brukerens siste registrerte økt (satt til midnatt)
                val lastDate = completed.mapNotNull {
                    try { sdf.parse(it.date) } catch (e: Exception) { null }
                }.filter { it.time <= System.currentTimeMillis() }
                .maxByOrNull { it.time }

                val lastCal = Calendar.getInstance().apply {
                    if (lastDate != null) {
                        time = lastDate
                    } else {
                        // Hvis ingen økt er fullført noen gang, regner vi at brukeren har 3 dager inaktivitet
                        add(Calendar.DAY_OF_YEAR, -3)
                    }
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Nøyaktig antall hele kalenderdager med inaktivitet
                val daysInactive = ((todayCal.timeInMillis - lastCal.timeInMillis) / (24L * 60L * 60L * 1000L)).toInt()

                // Varsel utløses på den 3. dagen med inaktivitet (eller flere), eller ved manuell test
                if (daysInactive >= 3 || isTest) {
                    val prefs = context.getSharedPreferences("healthy_habits", Context.MODE_PRIVATE)
                    val todayStr = sdf.format(todayCal.time)

                    // Bruker dagens dato som markør slik at brukeren får maks ett varsel per dag
                    if (isTest || prefs.getString("last_inactivity_notification", "") != todayStr) {
                        if (showNotification(context)) {
                            prefs.edit().putString("last_inactivity_notification", todayStr).apply()
                        }
                    }
                }
            } finally {
                // Re-planlegger neste 17:00 alarm for i morgen slik at syklusen fortsetter
                NotificationScheduler.schedule(context)
                pendingResult.finish()
            }
        }
    }

    // Bygger og viser push-varselet som en pop-up på telefonen
    private fun showNotification(context: Context): Boolean {
        // Sjekker om varslingstillatelse er gitt på Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inactivity_reminders_v2"

        // Oppretter varslingskanal med IMPORTANCE_HIGH slik at varselet POPPER OPP på skjermen med lyd/vibrasjon
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId,
                "Treningspåminnelser",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Påminnelser for å ta vare på treningsstreaken din"
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
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

        // Bygger selve varselet med HØY prioritet for at det skal poppe opp øverst på skjermen
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Siste sjanse til å redde streaken$nameSuffix! 🔥")
            .setContentText("Det har gått 3 dager uten aktivitet. Fullfør en økt i dag for å unngå å miste treningsvanen din!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        manager.notify(4105, notification)
        return true
    }
}
