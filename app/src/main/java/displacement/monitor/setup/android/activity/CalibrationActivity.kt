package displacement.monitor.setup.android.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import displacement.monitor.R
import displacement.monitor.cv.android.view.CustomCameraView
import displacement.monitor.cv.*
import displacement.monitor.cv.controller.*
import displacement.monitor.cv.controller.ImageOperations.drawTarget
import displacement.monitor.cv.controller.ImageOperations.fixOrientation
import displacement.monitor.cv.controller.ImageOperations.resizeWithBorder
import displacement.monitor.settings.android.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.Mat

/**
 * Activity used to measure a focal length value to place in [Settings.Calibration.focalLength] for
 * a [CalibratedImageProcessor] to then use.
 */
class CalibrationActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views */
    private val views by lazy { Views() }

    /** Access to app settings */
    private val settings by lazy { Settings(this) }

    /** Flag for if a value for focal length has been measured */
    private var measured: Boolean = false

    /**
     * Used to find the target so it can be measured.
     */
    private val targetFinder by lazy { TargetFinder(this.settings) }

    /** Camera callback used for just showing the found target. */
    private val targetFinderCamera = CameraFrameCallback { image ->
        // Image shown to the user, updated as processing is done
        var preview: Mat

        // Orient the image however the settings call for
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

    /** Camera callback used to find the target and measure the focal length. */
    private val focalLengthMeasureCamera = CameraFrameCallback { image ->
        // Image shown to the user, updated as processing is done
        var preview: Mat

        // Orient the image however the settings call for
        val oriented = fixOrientation(image, this.settings.camera.warp)
        preview = resizeWithBorder(oriented, image.size())

        try {
            val target = this.targetFinder.findTarget(oriented)
            preview = resizeWithBorder(drawTarget(oriented, target), image.size())

            // Target is found, measure a focal length value
            val focalLength = TargetMeasurement.focalLengthReal(
                distanceReal = this.settings.calibration.initialDistance,
                lengthReal = this.settings.calibration.targetSize,
                lengthPx = target.edgeLength
            )
            CoroutineScope(Dispatchers.Main).launch {
                // Notify the activity of the measurement
                if (!this@CalibrationActivity.measured) {
                    onMeasureFocalLength(focalLength)
                }
            }
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to measure focal length (${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@CalibrationActivity.measured) {
                    @SuppressLint("SetTextI18n")
                    this@CalibrationActivity.views.readout.text = "Looking for target..."
                }
            }
        }

        preview
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)

        this.title = "Calibration"

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Set up views

        this.views.readout.also { readout ->
            val (targetSize, initialDistance) = this.settings.calibration.let {
                it.targetSize to it.initialDistance
            }
            @SuppressLint("SetTextI18n")
            readout.text = """
                If the wrong camera is showing or it is warped, fix it in settings.
                The target is configured to be ${targetSize}m square.
                Place the camera at ${initialDistance}m from the target and press calibrate.
                If any of the values above are wrong, fix them in settings.
            """.trimIndent()
        }

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }

        this.views.calibrateButton.setOnClickListener {
            // Change the camera to use the focal length measurement callback
            this.views.cameraView.start(this.settings, this.focalLengthMeasureCamera)
        }

        this.views.returnButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this)
        this.views.cameraView.start(this.settings, this.targetFinderCamera)
    }

    override fun onPause() {
        this.views.cameraView.stop()

        super.onPause()
    }

    override fun finish() {
        this.views.cameraView.stop()

        super.finish()
    }

    // Focal length measurement callback -----------------------------------------------------------

    private fun onMeasureFocalLength(focalLength: Double) {
        this.views.cameraView.stop()

        if (!this.measured) {
            this.measured = true
            // Override the value in settings
            this.settings.preferences.edit().also { editor ->
                editor.putString("calibration_focalLength", focalLength.toString())
                editor.apply()
            }

            @SuppressLint("SetTextI18n")
            this.views.readout.text = "Focal length of ${"%.2f".format(focalLength)}m measured"
        }

        this.views.settingsButton.visibility = View.GONE
        this.views.calibrateButton.visibility = View.GONE
        this.views.returnButton.visibility = View.VISIBLE
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.calibrationActivityCameraView),
        val readout: TextView = findViewById(R.id.calibrationActivityReadout),
        val settingsButton: Button = findViewById(R.id.calibrationActivitySettingsButton),
        val calibrateButton: Button = findViewById(R.id.calibrationActivityCalibrateButton),
        val returnButton: Button = findViewById(R.id.calibrationActivityReturnButton),
    )

    companion object {
        private const val TAG = "CalibrationActivity"

        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, CalibrationActivity::class.java)
    }
}