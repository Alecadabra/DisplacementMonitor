package icp_bhp.crackmonitor.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.Permission
import icp_bhp.crackmonitor.controller.PermissionDelegate

class PermissionHandlerActivity : AppCompatActivity() {

    /** Handles permission logic and delegates result calls */
    private val permissionHandler = PermissionDelegate(this)

    private val requiredPermissions: Collection<Permission>
        get() = Permission.allPerms.filterNot { it.isGrantedTo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_handler)

        this.title = "Permissions"

        runPermRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Permission.fromRequestCode(requestCode)?.also { perm ->
            this.permissionHandler.resultActivity(perm, resultCode, data)
            runPermRequest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Permission.fromRequestCode(requestCode)?.also { perm ->
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
        this.requiredPermissions.firstOrNull()?.requestWith(this) ?: run {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, PermissionHandlerActivity::class.java)
    }
}