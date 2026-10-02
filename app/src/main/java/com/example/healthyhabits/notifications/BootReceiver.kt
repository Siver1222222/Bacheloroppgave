package com.example.healthyhabits.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// BroadcastReceiver som lytter etter at telefonen har startet på nytt, og re-planlegger varsler
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Gjenoppretter alarmen via NotificationScheduler når enheten har fullført oppstart
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) NotificationScheduler.schedule(context)
    }
}
