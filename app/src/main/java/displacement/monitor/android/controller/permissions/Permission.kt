package displacement.monitor.android.controller.permissions

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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

sealed class Permission {

    // Abstract members ----------------------------------------------------------------------------

    abstract val requestCode: Int

    abstract val name: String

    abstract fun isGrantedTo(activity: Activity): Boolean

    abstract fun requestWith(activity: Activity)

    // Overrides -----------------------------------------------------------------------------------

    override fun toString() = this.name

    // Independent utilities -----------------------------------------------------------------------

    companion object {
        val allPerms = listOf(SETTINGS, CAMERA, ADMIN)

        fun fromRequestCode(requestCode: Int) = allPerms.getOrNull(requestCode)
    }

    // Implementations -----------------------------------------------------------------------------

    object SETTINGS : Permission() {
        override val name = "Settings"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.WRITE_SETTINGS

        override fun isGrantedTo(activity: Activity): Boolean = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Settings.System.canWrite(activity)
            }
            else -> {
                val state = ContextCompat.checkSelfPermission(activity, permString)
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

    }

    object CAMERA : Permission() {
        override val name = "Camera"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.CAMERA

        override fun isGrantedTo(activity: Activity): Boolean {
            val state = ContextCompat.checkSelfPermission(activity, permString)
            return state == PackageManager.PERMISSION_GRANTED
        }

        override fun requestWith(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permString),
                requestCode
            )
        }
    }

    object ADMIN : Permission() {
        override val name = "Admin"

        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        override fun isGrantedTo(activity: Activity): Boolean {
            val policyKey = Context.DEVICE_POLICY_SERVICE
            val policyManager = activity.getSystemService(policyKey) as DevicePolicyManager
            val adminReceiverComp = ComponentName(activity, AdminReceiver::class.java)
            return policyManager.isAdminActive(adminReceiverComp)
        }

        override fun requestWith(activity: Activity) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
                val adminReceiverComp = ComponentName(activity, AdminReceiver::class.java)
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComp)
            }
            activity.startActivityForResult(intent, requestCode)
        }

        class AdminReceiver : DeviceAdminReceiver() {
            override fun onEnabled(context: Context, intent: Intent) {
                super.onEnabled(context, intent)

                Log.d(TAG, "AdminReceiver - Enabled")
            }

            override fun onDisabled(context: Context, intent: Intent) {
                super.onDisabled(context, intent)

                Log.d(TAG, "AdminReceiver - Enabled")
            }

            companion object {
                private const val TAG = "Permission\$ADMIN"
            }
        }
    }
}