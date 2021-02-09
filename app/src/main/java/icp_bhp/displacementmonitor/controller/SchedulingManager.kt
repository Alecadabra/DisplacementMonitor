package icp_bhp.displacementmonitor.controller

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import icp_bhp.displacementmonitor.model.Settings

class SchedulingManager(
    private val context: Context,
    private val settings: Settings,
    private val scheduledIntent: Intent,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val alarmIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
            this.context,
            0,
            AlarmReceiver.getIntent(this.context, this.scheduledIntent),
            0
        )

    fun start() {
        val periodMinutes = this.settings.periodicMeasurement.period
        val periodMillis = periodMinutes * 60000L

        this.alarmManager.cancel(this.alarmIntent)

        this.alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + periodMillis,
            periodMillis,
            this.alarmIntent
        )

        Log.d(TAG, "Scheduling started at $periodMinutes minutes period")
    }

    fun cancel() {
        Log.d(TAG, "Scheduling cancelled")

        this.alarmManager.cancel(this.alarmIntent)
    }

    class AlarmReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // Resolve scheduled intent
            intent.getParcelableExtra<Intent>(BUNDLE_SCHEDULED_INTENT)?.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            )?.also { scheduledIntent ->
                // Scheduled intent resolved, start activity
                Log.d(TAG, "Received broadcast, starting scheduled intent")
                context.startActivity(scheduledIntent)
            } ?: Log.e(TAG, "Received broadcast, could not resolve scheduled intent")
        }

        companion object {
            fun getIntent(c: Context, intent: Intent) = Intent(
                c, AlarmReceiver::class.java
            ).putExtra(BUNDLE_SCHEDULED_INTENT, intent)
        }
    }

    companion object {
        private const val TAG = "SchedulingManager"

        private const val BUNDLE_SCHEDULED_INTENT = "$TAG::scheduledIntent"
    }
}
