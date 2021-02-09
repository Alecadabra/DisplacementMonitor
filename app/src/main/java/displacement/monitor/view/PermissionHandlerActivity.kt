package displacement.monitor.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.controller.permissions.Permission
import displacement.monitor.controller.permissions.PermissionDelegate

class PermissionHandlerActivity : AppCompatActivity() {

    /** Delegates activity/permission result calls */
    private val permissionDelegate by lazy { PermissionDelegate(this) }

    private val requiredPermissions: Collection<Permission>
        get() = Permission.allPerms.filterNot { it.isGrantedTo(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_handler)

        this.title = "Permissions"
    }

    override fun onResume() {
        super.onResume()

        runPermRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Permission.fromRequestCode(requestCode)?.also { perm ->
            try {
                this.permissionDelegate.resultActivity(perm, resultCode, data)
                runPermRequest()
            } catch (e: IllegalStateException) {
                val message = "Permission request failed: ${e.message}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Permission.fromRequestCode(requestCode)?.also { perm ->
            try {
                this.permissionDelegate.resultRequestPermission(perm, permissions, grantResults)
                runPermRequest()
            } catch (e: IllegalStateException) {
                val message = "Permission request failed: ${e.message}"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
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
        val nextPerm = this.requiredPermissions.firstOrNull()

        if (nextPerm == null) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            AlertDialog.Builder(this).also {
                it.setTitle("$nextPerm permission required")
                it.setPositiveButton("Grant Permission") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    nextPerm.requestWith(this)
                }
                it.setCancelable(false)
            }.create().show()
        }
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, PermissionHandlerActivity::class.java)
    }
}