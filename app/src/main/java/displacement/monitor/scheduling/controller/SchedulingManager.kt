package displacement.monitor.scheduling.controller

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import displacement.monitor.settings.model.Settings

/**
 * Contains the logic for scheduling a repeating intent to be started at some period.
 * @param context The context to use
 * @param settings Access to [Settings.PeriodicMeasurement.period] to determine the period
 * @param scheduledIntent The intent to start periodically
 */
class SchedulingManager(
    private val context: Context,
    private val settings: Settings,
    private val scheduledIntent: Intent,
) {

    val isScheduled: Boolean
        get() = PendingIntent.getBroadcast(
            this.context,
            0,
            this.scheduledIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * The intent for the [alarmManager] to start to consequently start the [scheduledIntent].
     */
    private val alarmIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
            this.context,
            0,
            AlarmReceiver.getIntent(this.context, this.scheduledIntent),
            0
        )

    /**
     * Starts the scheduling, cancelling any current scheduling for [scheduledIntent].
     */
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

    /**
     * Cancels any current scheduling for [scheduledIntent].
     */
    fun cancel() {
        Log.d(TAG, "Scheduling cancelled")

        this.alarmManager.cancel(this.alarmIntent)
    }

    /**
     * Broadcast receiver to receive the [alarmIntent] and consequently start the [scheduledIntent].
     */
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
            /**
             * Gets the intent to send a broadcast to this receiver.
             * @param c Context being called from
             * @param intent The activity intent to start when the broadcast is received
             * @return Intent to use with an [AlarmManager]
             */
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