package displacement.monitor.setup.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import displacement.monitor.R
import displacement.monitor.settings.view.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.view.activity.CalibrationActivity

/**
 * An [AbstractSetupPageFragment] to use that uses [CalibrationActivity] to calibrate the
 * image processing as part of the setup procedure.
 */
class CalibrationSetupFragment : AbstractSetupPageFragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the readout text
        val calibrationSettings = this.settings.calibration
        @SuppressLint("SetTextI18n")
        this.views.readout.text = """
            Configured initial distance: ${calibrationSettings.initialDistance}m
            Configured target size: ${calibrationSettings.targetSize}m
            Calibration value (Focal length): ${
            calibrationSettings.focalLength.takeUnless { it == 0.0 }?.let {
                "Measured (${"%.2f".format(it)}) - Calibration is done"
            } ?: "Not measured - Must be calibrated"
        }
        """.trimIndent()

        this.views.settingsBtn.setOnClickListener {
            startActivity(SettingsActivity.getIntent(requireContext()))
        }
        this.views.calibrationBtn.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(requireContext()))
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerSetupActivity.pageNext()
        }
        this.views.backBtn.setOnClickListener {
            this.pagerSetupActivity.pageBack()
        }
    }

    // Setup page overrides ------------------------------------------------------------------------

    override val title: String = "Calibration"

    override fun canAdvance(activity: Activity): Boolean {
        // Can advance if there is a non-zero value for focal length recorded
        return Settings(activity).calibration.focalLength != 0.0
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
        val readout: TextView = view.findViewById(R.id.calibrationSetupFragmentReadout),
        val settingsBtn: Button = view.findViewById(R.id.calibrationSetupFragmentSettingsBtn),
        val calibrationBtn: Button = view.findViewById(R.id.calibrationSetupFragmentCalibrateBtn),
        val backBtn: Button = view.findViewById(R.id.calibrationSetupFragmentBackButton),
        val nextBtn: Button = view.findViewById(R.id.calibrationSetupFragmentNextButton),
    )

    companion object {
        private const val TAG = "CalibrationSetup"
    }
}