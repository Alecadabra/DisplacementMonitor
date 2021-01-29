package icp_bhp.crackmonitor.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import icp_bhp.crackmonitor.controller.PermissionHandler.Permission.CAMERA
import icp_bhp.crackmonitor.controller.PermissionHandler.Permission.SETTINGS
import java.util.*

class PermissionHandler(private val activity: Activity) {

    // Public entry points -------------------------------------------------------------------------

    fun hasPermission(permission: Permission): Boolean = when (permission) {
        SETTINGS -> hasSettingsPermission()
        CAMERA -> hasCameraPermission()
    }

    fun requestPermission(permission: Permission) = when (permission) {
        SETTINGS -> requestSettingsPermission()
        CAMERA -> requestCameraPermission()
    }

    /*fun requestPermissionDialog(permission: Permission) = AlertDialog.Builder(this.activity).also {
        it.setTitle("$permission Permission Required")
        it.setNeutralButton("Grant Permission") { dialogInterface, _ ->
            dialogInterface.dismiss()
            requestPermission(permission)
        }
        it.setCancelable(false)
    }.create()*/

    // Activity delegates --------------------------------------------------------------------------

    fun resultActivity(
            permission: Permission,
            resultCode: Int,
            data: Intent?
    ) = when (permission) {
        SETTINGS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                check(Settings.System.canWrite(this.activity)) { "Permissions were not granted" }
            } else error("Internal error, wrong API level")
        }
        CAMERA -> error("Internal error")
    }

    fun resultRequestPermission(
            permission: Permission,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        fun checkGranted(perm: String) = permissions.indexOf(perm).takeIf { it != -1 }?.let { idx ->
            grantResults[idx] == PackageManager.PERMISSION_GRANTED
        } ?: false

        when (permission) {
            SETTINGS -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    check(checkGranted(Manifest.permission.WRITE_SETTINGS)) {
                        "Settings permission not granted"
                    }
                } else error("Internal error, wrong API level")
            }
            CAMERA -> {
                check(checkGranted(Manifest.permission.CAMERA)) {
                    "Camera permission not granted"
                }
            }
        }
    }

    // Private permission handling logic -----------------------------------------------------------

    private fun hasSettingsPermission(): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            Settings.System.canWrite(this.activity)
        }
        else -> {
            val state = ContextCompat.checkSelfPermission(this.activity, SETTINGS.permString)
            state == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasCameraPermission(): Boolean {
        val state = ContextCompat.checkSelfPermission(this.activity, CAMERA.permString)
        return state == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSettingsPermission() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also { intent ->
                intent.data = Uri.parse("package:${this.activity.packageName}")
                this.activity.startActivityForResult(intent, SETTINGS.requestCode)
            }
            Unit
        }
        else -> SETTINGS.request(this.activity)
    }

    private fun requestCameraPermission() = CAMERA.request(this.activity)

    enum class Permission(val permString: String) {
        SETTINGS(Manifest.permission.WRITE_SETTINGS),
        CAMERA(Manifest.permission.CAMERA);

        val requestCode = this.ordinal

        fun request(activity: Activity) = ActivityCompat.requestPermissions(
                activity,
                arrayOf(this.permString),
                this.requestCode
        )

        override fun toString() = super.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
                .replace("_", " ")

        companion object {
            fun fromRequestCode(codeInt: Int) = values().getOrNull(codeInt)
        }
    }
}