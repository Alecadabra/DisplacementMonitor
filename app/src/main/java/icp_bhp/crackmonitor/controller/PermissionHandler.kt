package icp_bhp.crackmonitor.controller

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import icp_bhp.crackmonitor.controller.PermissionHandler.RequestCode.*

class PermissionHandler(private val activity: Activity) {

    fun getSettingsPermission() {
        val hasPerm = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                Settings.System.canWrite(this.activity)
            }
            else -> {
                val perm = Manifest.permission.WRITE_SETTINGS
                val state = ContextCompat.checkSelfPermission(this.activity, perm)
                state == PackageManager.PERMISSION_DENIED
            }
        }

        if (!hasPerm) {
            AlertDialog.Builder(this.activity).create().also { dialog ->
                dialog.setTitle("Settings Edit Permission Required")
                dialog.setButton(
                    DialogInterface.BUTTON_NEUTRAL,
                    "Grant Permission"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).also { intent ->
                                intent.data = Uri.parse("package:${this.activity.packageName}")
                                this.activity.startActivityForResult(intent, SETTINGS.int)
                            }
                        }
                        else -> {
                            ActivityCompat.requestPermissions(
                                this.activity,
                                arrayOf(Manifest.permission.WRITE_SETTINGS),
                                SETTINGS.int
                            )
                        }
                    }
                }
                dialog.show()
            }
        }
    }

    fun getCameraPermission() {
        val cameraPerm = Manifest.permission.CAMERA
        when (ContextCompat.checkSelfPermission(this.activity, cameraPerm)) {
            PackageManager.PERMISSION_DENIED -> {
                // Ask for camera perm
                ActivityCompat.requestPermissions(this.activity, arrayOf(cameraPerm), CAMERA.int)
            }
        }
    }

    fun activityResult(
        requestCode: RequestCode,
        resultCode: Int,
        data: Intent?
    ) = when (requestCode) {
        SETTINGS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                check(Settings.System.canWrite(this.activity)) { "Permissions were not granted" }
            } else error("Internal error, wrong API level")
        }
        CAMERA -> error("Internal error")
    }

    fun requestPermissionsResult(
        requestCode: RequestCode,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun checkGranted(perm: String) = permissions.indexOf(perm).takeIf { it != -1 }?.let { idx ->
            grantResults[idx] == PackageManager.PERMISSION_GRANTED
        } ?: false

        when (requestCode) {
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

    enum class RequestCode {
        SETTINGS,
        CAMERA;

        val int = this.ordinal

        companion object {
            fun fromInt(codeInt: Int) = values().getOrNull(codeInt)
        }
    }
}