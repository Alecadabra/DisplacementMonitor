package icp_bhp.crackmonitor.controller

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi

class DeviceStateController(private val activity: Activity) {

    fun start() {
        // Dim screen
        Settings.System.putInt(
            this.activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        this.activity.window.attributes = this.activity.window.attributes.also {
            it.screenBrightness = 0f
        }

        // Add window flags
        this.activity.window.addFlags(
            WindowFlag.FLAG_KEEP_SCREEN_ON or WindowFlag.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            this.activity.setShowWhenLocked(true)
            this.activity.setTurnScreenOn(true)
            val keyguardManager = this.activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this.activity, KeyguardCallback)
        } else {
            this.activity.window.addFlags(
                WindowFlag.FLAG_SHOW_WHEN_LOCKED or WindowFlag.FLAG_DISMISS_KEYGUARD or WindowFlag.FLAG_TURN_SCREEN_ON
            )
        }
    }

    fun finish() {
        this.activity.window.clearFlags(
            WindowFlag.FLAG_KEEP_SCREEN_ON
        )

        try {
            val policyKey = Context.DEVICE_POLICY_SERVICE
            val policyManager = this.activity.getSystemService(policyKey) as DevicePolicyManager
            val permissionHandler = PermissionDelegate(this.activity)

            if (Permission.ADMIN.isGrantedTo(this.activity)) {
                policyManager.lockNow()
            } else {
                Log.e(TAG, "Cannot lock screen (Not admin)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not turn off screen (Security Exception)", e)
        }

        this.activity.finish()
    }

    object AdminReceiver : DeviceAdminReceiver() {
        override fun onEnabled(context: Context, intent: Intent) {
            super.onEnabled(context, intent)

            Log.d(TAG, "AdminReceiver - Enabled")
        }

        override fun onDisabled(context: Context, intent: Intent) {
            super.onDisabled(context, intent)

            Log.d(TAG, "AdminReceiver - Enabled")
        }
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

private typealias WindowFlag = WindowManager.LayoutParams