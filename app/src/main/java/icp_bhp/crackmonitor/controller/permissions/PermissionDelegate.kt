package icp_bhp.crackmonitor.controller.permissions

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import icp_bhp.crackmonitor.controller.permissions.Permission.*

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
            SETTINGS -> checkGranted(SETTINGS.permString, "Settings permission not granted")
            CAMERA -> checkGranted(CAMERA.permString, "Camera permission not granted")
            ADMIN -> error("Internal error")
        }

        check(permission.isGrantedTo(this.activity)) {
            "$permission permission not granted"
        }
    }
}
