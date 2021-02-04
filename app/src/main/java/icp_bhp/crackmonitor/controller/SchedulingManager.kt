package icp_bhp.crackmonitor.controller

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import icp_bhp.crackmonitor.model.Settings

class SchedulingManager(
    private val context: Context,
    private val settings: Settings,
    private val scheduledIntent: Intent,
) {

    val isScheduled: Boolean
        get() = getAlarmIntent(PendingIntent.FLAG_NO_CREATE) != null

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun getAlarmIntent(flags: Int = 0): PendingIntent? = PendingIntent.getBroadcast(
        this.context,
        0,
        AlarmReceiver.getIntent(this.context, this.scheduledIntent),
        flags
    )

    fun start() {
        val periodMinutes = this.settings.periodicMeasurement.period
        val periodMillis = periodMinutes * 1000L

        Log.d(TAG, "Scheduling started at $periodMinutes minutes period")

        this.alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + periodMillis,
            periodMillis,
            getAlarmIntent()
        )
    }

    fun cancel() {
        Log.d(TAG, "Scheduling cancelled")

        this.alarmManager.cancel(this.getAlarmIntent())
        this.alarmManager
    }

    class AlarmReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // Resolve scheduled intent
            val scheduledIntent =
                intent.getParcelableExtra<Intent>(BUNDLE_SCHEDULED_INTENT)?.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                )

            if (scheduledIntent == null) {
                Log.e(TAG, "Received broadcast, could not resolve scheduled intent")
            } else {
                Log.d(TAG, "Received broadcast, starting scheduled intent")
                context.startActivity(scheduledIntent)
            }
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
