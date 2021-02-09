package displacement.monitor.controller

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import displacement.monitor.controller.permissions.Permission

class DeviceStateController(private val activity: Activity) {

    fun start() {
        // Dim screen
        /*
        Settings.System.putInt(
            this.activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        Settings.System.putInt(
            this.activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            0
        )*/

        // Wake lock and show in lock screen
        this.activity.window.addFlags(
            Flag.FLAG_KEEP_SCREEN_ON or Flag.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Turn screen on and dismiss keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.activity.setShowWhenLocked(true)
            this.activity.setTurnScreenOn(true)

            val keyguardKey = Context.KEYGUARD_SERVICE
            val keyguardManager = this.activity.getSystemService(keyguardKey) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this.activity, KeyguardCallback)
        } else {
            this.activity.window.addFlags(
                Flag.FLAG_SHOW_WHEN_LOCKED or Flag.FLAG_DISMISS_KEYGUARD or Flag.FLAG_TURN_SCREEN_ON
            )
        }
    }

    fun finish() {
        // Allow screen to turn off
        this.activity.window.clearFlags(Flag.FLAG_KEEP_SCREEN_ON)

        // Lock device if possible
        try {
            if (Permission.ADMIN.isGrantedTo(this.activity)) {
                val policyKey = Context.DEVICE_POLICY_SERVICE
                val policyManager = this.activity.getSystemService(policyKey) as DevicePolicyManager
                policyManager.lockNow()
            } else {
                Log.e(TAG, "Cannot lock screen (Not admin)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not turn off screen (Security Exception)", e)
        }

        // Finish activity
        this.activity.finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    object KeyguardCallback : KeyguardManager.KeyguardDismissCallback() {
        override fun onDismissCancelled() {
            Log.d(TAG, "Keyguard - Dismiss cancelled")
        }

        override fun onDismissError() {
            Log.d(TAG, "Keyguard - Dismiss cancelled")
        }

        override fun onDismissSucceeded() {
            Log.d(TAG, "Keyguard - Dismiss success")
        }
    }

    companion object {
        private const val TAG = "DeviceStateController"
    }
}

private typealias Flag = WindowManager.LayoutParams