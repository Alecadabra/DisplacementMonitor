package displacement.monitor.setup.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import displacement.monitor.R
import displacement.monitor.settings.view.activity.SettingsActivity
import displacement.monitor.settings.model.Settings

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
 * An [AbstractSetupPageFragment] used to have the app configuration's set using the
 * [SettingsActivity] as part of the setup procedure.
 */
class SettingsSetupFragment : AbstractSetupPageFragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(requireContext()))
        }
        this.views.backBtn.setOnClickListener {
            this.pagerSetupActivity.pageBack()
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerSetupActivity.pageNext()
        }
    }

    // Setup page overrides ------------------------------------------------------------------------

    override val title: String = "Configure Settings"

    override fun canAdvance(activity: Activity): Boolean {
        // Settings are valid if the settings constructor does not throw an exception
        return runCatching { Settings(activity) }.isSuccess
    }

    override fun updateState(canAdvance: Boolean) {
        this.views.nextBtn.also {
            it.isEnabled = canAdvance
            it.isClickable = canAdvance
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        view: View,
        val settingsButton: Button = view.findViewById(R.id.settingsSetupFragmentSettingsButton),
        val backBtn: Button = view.findViewById(R.id.settingsSetupFragmentBackButton),
        val nextBtn: Button = view.findViewById(R.id.settingsSetupFragmentNextButton),
    )
}