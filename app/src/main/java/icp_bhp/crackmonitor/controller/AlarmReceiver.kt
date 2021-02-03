package icp_bhp.crackmonitor.controller

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import icp_bhp.crackmonitor.view.ScheduledMeasurementActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("AlarmReceiver", "Received broadcast, starting activity")
        context.startActivity(ScheduledMeasurementActivity.getIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    companion object {
        fun getIntent(c: Context) = Intent(c, AlarmReceiver::class.java)

        fun getPendingIntent(c: Context): PendingIntent = PendingIntent.getBroadcast(c, 0, getIntent(c), 0)
    }
}