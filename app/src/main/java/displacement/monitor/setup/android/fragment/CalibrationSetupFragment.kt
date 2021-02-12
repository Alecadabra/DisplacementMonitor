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

class CalibrationSetupFragment : AbstractSetupPageFragment("Calibrate") {

    private val views by lazy { Views(requireView()) }

    private val settings by lazy { Settings(requireContext()) }

    private var measured: Boolean = false

    private val targetFinder by lazy { TargetFinder(this.settings) }

    /** Camera callback used for just showing the found target */
    private val targetFinderCamera = CameraFrameCallback { image ->
        var preview: Mat

        val oriented = fixOrientation(image, this.settings.camera.warp)
        preview = resizeWithBorder(oriented, image.size())

        try {
            val target = this.targetFinder.findTarget(oriented)
            preview = resizeWithBorder(drawTarget(oriented, target), image.size())
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to find target (${e.message})")
        }

        preview
    }

    /** Camera callback used to measure the target */
    private val focalLengthMeasureCamera = CameraFrameCallback { image ->
        var preview: Mat

        val oriented = fixOrientation(image, this.settings.camera.warp)
        preview = resizeWithBorder(oriented, image.size())

        try {
            val target = this.targetFinder.findTarget(oriented)
            preview = resizeWithBorder(drawTarget(oriented, target), image.size())

            val focalLength = TargetMeasurement.focalLengthReal(
                distanceReal = this.settings.calibration.initialDistance,
                lengthReal = this.settings.calibration.targetSize,
                lengthPx = target.edgeLength
            )
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@CalibrationSetupFragment.measured) {
                    onMeasureFocalLength(focalLength)
                }
            }
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to measure focal length (${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@CalibrationSetupFragment.measured) {
                    @SuppressLint("SetTextI18n")
                    this@CalibrationSetupFragment.views.readout.text = "Looking for target..."
                }
            }
        }

        preview
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calibration_setup, container, false)
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(requireContext(), TAG)

        checkMeasured()

        val calibrationSettings = this.settings.calibration
        @SuppressLint("SetTextI18n")
        this.views.readout.text = """
            Configured initial distance: ${calibrationSettings.initialDistance}m
            Configured target size: ${calibrationSettings.targetSize}m
            Calibration value (Focal length): ${
                calibrationSettings.focalLength.takeUnless { it == 0.0 }?.let {
                    "Measured (${"%.2f".format(it)})" 
                } ?: "Not measured"
            }
        """.trimIndent()

        this.views.cameraView.start(this.settings, this.targetFinderCamera)
        this.views.settingsBtn.setOnClickListener {
            startActivity(SettingsActivity.getIntent(requireContext()))
        }
        this.views.calibrationBtn.setOnClickListener {
            this.views.cameraView.start(this.settings, this.focalLengthMeasureCamera)
        }
        this.views.nextBtn.setOnClickListener {
            this.pagerActivity.pageNext()
        }
        this.views.backBtn.setOnClickListener {
            this.pagerActivity.pageBack()
        }
    }

    private fun onMeasureFocalLength(focalLength: Double) {
        this.views.cameraView.stop()

        if (!this.measured) {
            this.measured = true
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().also { editor ->
                editor.putString("calibration_focalLength", focalLength.toString())
                editor.apply()
            }

            onResume()
        }
    }

    private fun checkMeasured() {
        val measured = this.settings.calibration.focalLength != 0.0

        this.views.nextBtn.also {
            it.isEnabled = measured
            it.isClickable = measured
        }
    }

    private inner class Views(
        view: View,
        val readout: TextView = view.findViewById(R.id.calibrationSetupFragmentReadout),
        val cameraView: CustomCameraView = view.findViewById(R.id.calibrationSetupFragmentCameraView),
        val settingsBtn: Button = view.findViewById(R.id.calibrationSetupFragmentSettingsBtn),
        val calibrationBtn: Button = view.findViewById(R.id.calibrationSetupFragmentCalibrateBtn),
        val backBtn: Button = view.findViewById(R.id.calibrationSetupFragmentBackButton),
        val nextBtn: Button = view.findViewById(R.id.calibrationSetupFragmentNextButton),
    )

    companion object {
        private const val TAG = "CalibrationSetup"
    }
}