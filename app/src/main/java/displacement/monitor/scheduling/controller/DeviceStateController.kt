package displacement.monitor.scheduling.controller

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import displacement.monitor.permissions.controller.Permission

/**
 * Delegate for an activity to use to wake/lock the screen.
 */
class DeviceStateController(private val activity: Activity) {

    /**
     * Wakes the device, attempts to dismiss the keyguard if the device in unprotected or
     * otherwise shows the activity over the lock screen.
     */
    fun start() {
        /* Dim screen - Unused but possible battery optimisation
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
            keyguardManager.requestDismissKeyguard(this.activity, null)
        } else {
            @Suppress("DEPRECATION")
            this.activity.window.addFlags(
                Flag.FLAG_SHOW_WHEN_LOCKED or Flag.FLAG_DISMISS_KEYGUARD or Flag.FLAG_TURN_SCREEN_ON
            )
        }
    }

    /**
     * Locks the screen using [lockScreen], and then calls [Activity.finish] on the activity.
     */
    fun finish() {
        // Allow screen to turn off
        this.activity.window.clearFlags(Flag.FLAG_KEEP_SCREEN_ON)

        lockScreen()

        // Finish activity
        this.activity.finish()
    }

    /**
     * Attempts to lock the screen with [DevicePolicyManager.lockNow] if the app is an admin.
     */
    fun lockScreen() {
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
    }

    companion object {
        private const val TAG = "DeviceStateController"
    }
}

/**
 * Alias of [WindowManager.LayoutParams] to manage window flags.
 */
private typealias Flag = WindowManager.LayoutParams