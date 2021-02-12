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

class SettingsSetupFragment : AbstractSetupPageFragment("Configure Settings") {

    private val views by lazy { Views(requireView()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_setup, container, false)
    }

    override fun onResume() {
        super.onResume()

        checkSettingsValid()

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

    private fun checkSettingsValid() {
        val valid = runCatching {
            Settings(requireContext())
            this.views.nextBtn
        }.isSuccess

        this.views.nextBtn.also {
            it.isEnabled = valid
            it.isClickable = valid
        }
    }

    private inner class Views(
        view: View,
        val settingsButton: Button = view.findViewById(R.id.settingsSetupFragmentSettingsButton),
        val backBtn: Button = view.findViewById(R.id.settingsSetupFragmentBackButton),
        val nextBtn: Button = view.findViewById(R.id.settingsSetupFragmentNextButton),
    )
}