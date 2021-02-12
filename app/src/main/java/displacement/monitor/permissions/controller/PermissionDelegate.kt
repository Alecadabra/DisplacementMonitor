package displacement.monitor.permissions.controller

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import displacement.monitor.permissions.model.Permission

class PermissionDelegate(private val activity: Activity) {

    // Activity delegates --------------------------------------------------------------------------

    fun resultActivity(permission: Permission, resultCode: Int, data: Intent?) {
        check(permission.isGrantedTo(this.activity)) {
            "$permission permission not granted"
        }
    }

    fun resultRequestPermission(
        permission: Permission,
        permStrings: Array<out String>,
        permResults: IntArray
    ) {
        val resultMap = List(permResults.size) { permStrings[it] to permResults[it] }.toMap()

        fun checkGranted(permString: String, errorMessage: String) {
            val result = resultMap[permString]
            check(result != null && result == PackageManager.PERMISSION_GRANTED) { errorMessage }
        }

        when (permission) {
            Permission.SETTINGS -> checkGranted(Permission.SETTINGS.permString, "Settings permission not granted")
            Permission.CAMERA -> checkGranted(Permission.CAMERA.permString, "Camera permission not granted")
            Permission.ADMIN -> error("Internal error")
        }

        check(permission.isGrantedTo(this.activity)) {
            "$permission permission not granted"
        }
    }
}