package com.example.healthyhabits.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

// Klasse som setter opp og håndterer faste daglige alarmer med AlarmManager
object NotificationScheduler {
    private const val REQUEST_CODE = 4103
    private const val PREFS = "healthy_habits"
    private const val ENABLED_KEY = "notifications_enabled"

    // Sjekker om brukeren har slått på påminnelser i innstillingene
    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(ENABLED_KEY, true)

    // Aktiverer og deaktiverer påminnelser
    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ENABLED_KEY, enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    // Planlegger en daglig gjentakende alarm til klokken 17:00
    fun schedule(context: Context) {
        if (!isEnabled(context)) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, InactivityNotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Konfigurerer klokkeslettet til 17:00
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Dersom klokken har passert 17:00 i dag, settes alarmen til 17:00 i morgen
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        // Setter opp en dagsversjon av alarmen
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }

    // Kansellerer planlagte alarmer i systemet
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, InactivityNotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }
}
