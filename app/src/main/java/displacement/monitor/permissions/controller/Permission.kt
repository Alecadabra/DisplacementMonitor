package displacement.monitor.permissions.controller

import android.Manifest
import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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
 * Provides Android-specific logic for granting and checking the permissions that the app requires.
 */
sealed class Permission {

    // Abstract members ----------------------------------------------------------------------------

    /**
     * The activity/permission request code for the permission to use with `startActivityForResult`
     * and `requestPermissions`.
     */
    abstract val requestCode: Int

    /**
     * Human readable name of the permission.
     */
    abstract val name: String

    /**
     * Determines whether or not the permission is granted to the given context.
     */
    abstract fun isGrantedTo(context: Context): Boolean

    /**
     * Requests the permission to be granted to the given activity.
     */
    abstract fun requestWith(activity: Activity)

    /**
     * Requests the permission to be granted to the given fragment.
     */
    abstract fun requestWith(fragment: Fragment)

    // Overrides -----------------------------------------------------------------------------------

    override fun toString() = this.name

    // Independent utilities -----------------------------------------------------------------------

    companion object {
        val allPerms = listOf(SETTINGS, CAMERA, ADMIN)

        /**
         * Get a permission from a request code. The reverse of the
         * [Permission.requestCode][requestCode] property.
         */
        fun fromRequestCode(requestCode: Int) = allPerms.getOrNull(requestCode)
    }

    // Implementations -----------------------------------------------------------------------------

    /**
     * The permission to write to system settings.
     * In API 23 and above, this uses [Settings.ACTION_MANAGE_WRITE_SETTINGS], in lower levels,
     * this is the [Manifest.permission.WRITE_SETTINGS] Android permission.
     */
    object SETTINGS : Permission() {
        override val name = "Settings"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.WRITE_SETTINGS

        override fun isGrantedTo(context: Context): Boolean = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Settings.System.canWrite(context)
            }
            else -> {
                val state = ContextCompat.checkSelfPermission(context, permString)
                state == PackageManager.PERMISSION_GRANTED
            }
        }

        override fun requestWith(activity: Activity) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also { intent ->
                        intent.data = Uri.parse("package:${activity.packageName}")
                        activity.startActivityForResult(intent, requestCode)
                    }
                }
                else -> ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permString),
                    requestCode
                )
            }
        }

        override fun requestWith(fragment: Fragment) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also { intent ->
                        intent.data = Uri.parse("package:${fragment.requireActivity().packageName}")
                        fragment.startActivityForResult(intent, requestCode)
                    }
                }
                else -> fragment.requestPermissions(
                    arrayOf(permString),
                    requestCode
                )
            }
        }

    }

    /**
     * Permission to use the camera. This is the [Manifest.permission.CAMERA] Android permission.
     */
    object CAMERA : Permission() {
        override val name = "Camera"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.CAMERA

        override fun isGrantedTo(context: Context): Boolean {
            val state = ContextCompat.checkSelfPermission(context, permString)
            return state == PackageManager.PERMISSION_GRANTED
        }

        override fun requestWith(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permString),
                requestCode
            )
        }

        override fun requestWith(fragment: Fragment) {
            fragment.requestPermissions(
                arrayOf(permString),
                requestCode
            )
        }
    }

    /**
     * Corresponds to this app being an administrator as per the [DevicePolicyManager] using
     * the [AdminReceiver] subclass.
     */
    object ADMIN : Permission() {
        override val name = "Admin"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        override fun isGrantedTo(context: Context): Boolean {
            val policyKey = Context.DEVICE_POLICY_SERVICE
            val policyManager = context.getSystemService(policyKey) as DevicePolicyManager
            val adminReceiverComp = ComponentName(context, AdminReceiver::class.java)
            return policyManager.isAdminActive(adminReceiverComp)
        }

        override fun requestWith(activity: Activity) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
                val adminReceiverComp = ComponentName(activity, AdminReceiver::class.java)
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComp)
            }
            activity.startActivityForResult(intent, requestCode)
        }

        override fun requestWith(fragment: Fragment) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
                val adminReceiverComp = ComponentName(
                    fragment.requireContext(),
                    AdminReceiver::class.java
                )
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComp)
            }
            fragment.startActivityForResult(intent, requestCode)
        }

        class AdminReceiver : DeviceAdminReceiver()
    }
}