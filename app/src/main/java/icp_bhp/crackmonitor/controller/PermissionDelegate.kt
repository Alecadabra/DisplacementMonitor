package icp_bhp.crackmonitor.controller

import android.Manifest
import android.app.Activity
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
import icp_bhp.crackmonitor.controller.Permission.*
import java.util.*

class PermissionDelegate(private val activity: Activity) {

    /*fun requestPermissionDialog(permission: Permission) = AlertDialog.Builder(this.activity).also {
        it.setTitle("$permission Permission Required")
        it.setNeutralButton("Grant Permission") { dialogInterface, _ ->
            dialogInterface.dismiss()
            requestPermission(permission)
        }
        it.setCancelable(false)
    }.create()*/

    // Activity delegates --------------------------------------------------------------------------

    fun resultActivity(permission: Permission, resultCode: Int, data: Intent?) {
        check(resultCode == PackageManager.PERMISSION_GRANTED) {
            "$permission permission not granted"
        }
    }

    fun resultRequestPermission(
        permission: Permission,
        permissionStrings: Array<out String>,
        results: IntArray
    ) {
        fun checkGranted(permString: String) = permissionStrings.indexOf(permString).takeIf {
            it != -1
        }?.takeIf { idx ->
            results[idx] == PackageManager.PERMISSION_GRANTED
        } ?: error("$permString permission not granted")

        when (permission) {
            SETTINGS -> checkGranted(SETTINGS.permString)
            CAMERA -> checkGranted(CAMERA.permString)
            ADMIN -> error("Internal error")
        }
    }

}

sealed class Permission {

    abstract val requestCode: Int

    abstract fun isGrantedTo(activity: Activity): Boolean

    abstract fun requestWith(activity: Activity)

    override fun toString() = super.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)

    companion object {
        val allPerms = listOf(SETTINGS, CAMERA, ADMIN)

        fun fromRequestCode(requestCode: Int) = allPerms.getOrNull(requestCode)
    }

    // Implementations -------------------------------------------------------------------------

    object SETTINGS : Permission() {
        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.WRITE_SETTINGS

        override fun isGrantedTo(activity: Activity): Boolean = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Settings.System.canWrite(activity)
            }
            else -> {
                val state = ContextCompat.checkSelfPermission(activity, this.permString)
                state == PackageManager.PERMISSION_GRANTED
            }
        }

        override fun requestWith(activity: Activity) = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also { intent ->
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivityForResult(intent, requestCode)
                }
                Unit
            }
            else -> ActivityCompat.requestPermissions(
                activity,
                arrayOf(permString),
                this.requestCode
            )
        }

    }

    object CAMERA : Permission() {
        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        const val permString = Manifest.permission.CAMERA

        override fun isGrantedTo(activity: Activity): Boolean {
            val state = ContextCompat.checkSelfPermission(activity, this.permString)
            return state == PackageManager.PERMISSION_GRANTED
        }

        override fun requestWith(activity: Activity) = ActivityCompat.requestPermissions(
            activity,
            arrayOf(permString),
            this.requestCode
        )
    }

    object ADMIN : Permission() {
        override val requestCode: Int by lazy { allPerms.indexOf(this) }

        init {
            TODO("This does not properly resolve admin based on different android versions")
        }

        override fun isGrantedTo(activity: Activity): Boolean {
            val policyKey = Context.DEVICE_POLICY_SERVICE
            val policyManager = activity.getSystemService(policyKey) as DevicePolicyManager
            val adminReceiverClass = DeviceStateController.AdminReceiver::class.java
            val adminReceiver = ComponentName(activity, adminReceiverClass)
            return policyManager.isAdminActive(adminReceiver)
        }

        override fun requestWith(activity: Activity) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).also {
                val adminReceiverClass = DeviceStateController.AdminReceiver::class.java
                val adminReceiverComp = ComponentName(activity, adminReceiverClass)
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverComp)
            }
            activity.startActivityForResult(intent, requestCode)
        }
    }
}