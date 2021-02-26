package displacement.monitor.scheduling.controller

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import displacement.monitor.permissions.controller.Permission

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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

    fun goFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.activity.window.decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            this.activity.window.addFlags(Flag.FLAG_FULLSCREEN)
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