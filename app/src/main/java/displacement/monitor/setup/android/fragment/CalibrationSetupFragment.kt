package displacement.monitor.setup.android.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import displacement.monitor.R
import displacement.monitor.cv.android.view.CustomCameraView
import displacement.monitor.cv.controller.*
import displacement.monitor.cv.controller.ImageOperations.drawTarget
import displacement.monitor.cv.controller.ImageOperations.fixOrientation
import displacement.monitor.cv.controller.ImageOperations.resizeWithBorder
import displacement.monitor.settings.android.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.android.activity.CalibrationActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import java.lang.Exception

/**
 * An [AbstractSetupPageFragment] to use that uses [CalibrationActivity] to calibrate the
 * image processing as part of the setup procedure.
 */
class CalibrationSetupFragment : AbstractSetupPageFragment("Calibration") {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    /** Access to app settings. */
    private val settings by lazy { Settings(requireContext()) }

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
            this.pagerActivity.pageNext()
        }
        this.views.backBtn.setOnClickListener {
            this.pagerActivity.pageBack()
        }
    }

    override fun onResume() {
        super.onResume()

        // Run the check to see if this step is done
        checkMeasured()
    }

    // Local logic ---------------------------------------------------------------------------------

    private fun checkMeasured() {
        // Calibration is done if there is a non-zero value for focal length in settings.
        val measured = this.settings.calibration.focalLength != 0.0

        this.views.nextBtn.also {
            it.isEnabled = measured
            it.isClickable = measured
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