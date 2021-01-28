package icp_bhp.crackmonitor.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.PermissionHandler

class PermissionHandlerActivity : AppCompatActivity() {

    private val permissionHandler = PermissionHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_handler)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionHandler.RequestCode.fromInt(requestCode)?.also { code ->
            this.permissionHandler.activityResult(code, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionHandler.RequestCode.fromInt(requestCode)?.also { code ->
            this.permissionHandler.requestPermissionsResult(code, permissions, grantResults)
        }
    }
}