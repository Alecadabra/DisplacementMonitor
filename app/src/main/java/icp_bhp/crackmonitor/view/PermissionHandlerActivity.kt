package icp_bhp.crackmonitor.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.PermissionHandler

class PermissionHandlerActivity : AppCompatActivity() {

    /** Handles permission logic and delegates result calls */
    private val permissionHandler = PermissionHandler(this)

    private val requiredPermissions: Collection<PermissionHandler.Permission>
        get() = PermissionHandler.Permission.values().filterNot {
            this.permissionHandler.hasPermission(it)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_handler)

        runPermRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionHandler.Permission.fromRequestCode(requestCode)?.also { perm ->
            this.permissionHandler.resultActivity(perm, resultCode, data)
            runPermRequest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionHandler.Permission.fromRequestCode(requestCode)?.also { perm ->
            this.permissionHandler.resultRequestPermission(perm, permissions, grantResults)
            runPermRequest()
        }
    }

    override fun onBackPressed() {
        if (this.requiredPermissions.isEmpty()) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "You still have permissions to grant", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runPermRequest() {
        this.requiredPermissions.firstOrNull()?.also { perm ->
            this.permissionHandler.requestPermission(perm)
        } ?: run {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            this.onBackPressed()
        }
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, PermissionHandlerActivity::class.java)
    }
}