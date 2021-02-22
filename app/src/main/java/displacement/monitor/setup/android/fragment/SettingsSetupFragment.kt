package displacement.monitor.setup.android.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import displacement.monitor.R
import displacement.monitor.settings.android.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import java.lang.Exception

/**
 * An [AbstractSetupPageFragment] used to have the app configuration's set using the
 * [SettingsActivity] as part of the setup procedure.
 */
class SettingsSetupFragment : AbstractSetupPageFragment("Configure Settings") {

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
            this.pagerActivity.pageBack()
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerActivity.pageNext()
        }
    }

    override fun onResume() {
        super.onResume()

        // Run the check to see if this step is done
        checkSettingsValid()
    }

    // Local logic ---------------------------------------------------------------------------------

    private fun checkSettingsValid() {
        // Settings are valid if the constructor does not throw an exception
        val valid = runCatching { Settings(requireContext()) }.isSuccess

        this.views.nextBtn.also {
            it.isEnabled = valid
            it.isClickable = valid
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