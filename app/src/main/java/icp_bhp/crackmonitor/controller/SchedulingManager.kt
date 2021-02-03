package icp_bhp.crackmonitor.controller

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.SystemClock
import android.util.Log
import icp_bhp.crackmonitor.model.Settings
import java.time.Duration

class SchedulingManager(
    private val alarmManager: AlarmManager,
    private val alarmIntent: PendingIntent,
    private val settings: Settings,
) {
    fun start(millis: Long = 60_000L) {

        this.alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + millis,
            millis,
            alarmIntent
        )
    }

    fun cancel() {
        this.alarmManager.cancel(this.alarmIntent)
    }
}