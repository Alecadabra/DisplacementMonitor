package displacement.monitor.setup.android.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import displacement.monitor.R
import displacement.monitor.permissions.model.Permission
import displacement.monitor.setup.android.activity.SetupSlidePagerActivity

class PermissionsSetupFragment : AbstractSetupPageFragment("Obtain Permissions") {

    private val views by lazy { Views(requireView()) }

    private val permButtons by lazy {
        listOf(
            PermButton(Permission.ADMIN, this.views.adminBtn, this.views.adminCheck),
            PermButton(Permission.CAMERA, this.views.cameraBtn, this.views.cameraCheck),
            PermButton(Permission.SETTINGS, this.views.settingsBtn, this.views.settingsCheck)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permissions_setup, container, false)
    }

    override fun onResume() {
        super.onResume()

        refreshPermList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "Received onActivityResult")
        Permission.fromRequestCode(requestCode)?.also { perm ->
            refreshPermList()
            if (!perm.isGrantedTo(requireContext())) {
                Toast.makeText(
                    requireContext(), "Permission was not granted", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "Received onRequestPermissionsResult")
        Permission.fromRequestCode(requestCode)?.also { perm ->
            refreshPermList()
            if (!perm.isGrantedTo(requireContext())) {
                Toast.makeText(
                    requireContext(), "Permission was not granted", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshPermList() {
        this.permButtons.forEach { (permission, button, check) ->
            val granted = permission.isGrantedTo(requireContext())

            button.isEnabled = !granted
            button.isClickable = !granted
            check.isChecked = granted
            check.isEnabled = !granted

            if (!granted) {
                button.setOnClickListener {
                    permission.requestWith(this)
                }
            }
        }

        val allGranted = this.permButtons.all { it.permission.isGrantedTo(requireContext()) }

        this.views.nextBtn.also {
            it.isEnabled = allGranted
            it.isClickable = allGranted
        }

        this.views.backBtn.setOnClickListener {
            this.pagerActivity.pageBack()
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerActivity.pageNext()
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerActivity.pageNext()
        }
    }

    private data class PermButton(
        val permission: Permission,
        val button: ConstraintLayout,
        val checkBox: CheckBox
    )

    private inner class Views(
        view: View,
        val adminBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentAdmin),
        val adminCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentAdminCheck),
        val cameraBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentCamera),
        val cameraCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentCameraCheck),
        val settingsBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentSettings),
        val settingsCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentSettingsCheck),
        val backBtn: Button = view.findViewById(R.id.permissionsSetupFragmentBackButton),
        val nextBtn: Button = view.findViewById(R.id.permissionsSetupFragmentNextButton),
    )

    companion object {
        private const val TAG = "PermissionsSetup"
    }
}