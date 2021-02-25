package displacement.monitor.setup.view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import displacement.monitor.R
import displacement.monitor.permissions.controller.Permission

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * An [AbstractSetupPageFragment] used to have all necessary permissions granted to the app as part
 * of the setup process.
 */
class PermissionsSetupFragment : AbstractSetupPageFragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    /**
     * Convenient access to each required [Permission], it's clickable button
     * (A [ConstraintLayout]), and it's [CheckBox] of whether or not it's granted.
     */
    private val permButtons by lazy {
        listOf(
            PermButton(Permission.ADMIN, this.views.adminBtn, this.views.adminCheck),
            PermButton(Permission.CAMERA, this.views.cameraBtn, this.views.cameraCheck),
            PermButton(Permission.SETTINGS, this.views.settingsBtn, this.views.settingsCheck)
        )
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permissions_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.views.nextBtn.setOnClickListener {
            this.pagerSetupActivity.pageNext()
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerSetupActivity.pageNext()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // If it's a result from a permission request, check the result
        Permission.fromRequestCode(requestCode)?.also { perm ->
            updateState(canAdvance(this.pagerSetupActivity))
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

        // If it's a result from a permission request, check the result
        Permission.fromRequestCode(requestCode)?.also { perm ->
            updateState(canAdvance(this.pagerSetupActivity))
            if (!perm.isGrantedTo(requireContext())) {
                Toast.makeText(
                    requireContext(), "Permission was not granted", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Setup page overrides ------------------------------------------------------------------------

    override val title: String = "Obtain Permissions"

    override fun canAdvance(activity: Activity): Boolean {
        // Can advance if all permissions are granted to this
        return Permission.allPerms.all { it.isGrantedTo(activity) }
    }

    override fun updateState(canAdvance: Boolean) {
        // Update the perm buttons
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

        // Update the next button
        this.views.nextBtn.also {
            it.isEnabled = canAdvance
            it.isClickable = canAdvance
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Associates a required [Permission] with it's clickable button
     * (A [ConstraintLayout]), and it's [CheckBox] of whether or not it's granted.
     */
    private data class PermButton(
        val permission: Permission,
        val button: ConstraintLayout,
        val checkBox: CheckBox
    )

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        view: View,
        val adminBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentAdmin),
        val adminCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentAdminCheck),
        val cameraBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentCamera),
        val cameraCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentCameraCheck),
        val settingsBtn: ConstraintLayout = view.findViewById(R.id.permissionsSetupFragmentSettings),
        val settingsCheck: CheckBox = view.findViewById(R.id.permissionsSetupFragmentSettingsCheck),
        val nextBtn: Button = view.findViewById(R.id.permissionsSetupFragmentNextButton),
    )

    companion object {
        private const val TAG = "PermissionsSetup"
    }
}