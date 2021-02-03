package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.CameraFrameCallback
import icp_bhp.crackmonitor.controller.cv.*
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class CalibrationActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    /** Flag for if a value for focal length has been measured */
    private var measured: Boolean = false

    private val settings by lazy { Settings(this) }

    private val targetFinder by lazy { TargetFinder(this.settings) }

    /** Camera callback used for just showing the found target */
    private val targetFinderCamera = CameraFrameCallback { image ->
        var preview: Mat

        val oriented = fixOrientation(image, this.settings.cameraPreProcessing.warp)
        preview = resizeWithBorder(oriented, image.size())

        try {
            val target = this.targetFinder.findTarget(oriented)
            preview = resizeWithBorder(drawTarget(oriented, target), image.size())
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Could not find target", e)
        }

        preview
    }

    /** Camera callback used to measure the target */
    private val focalLengthMeasureCamera = CameraFrameCallback { image ->
        var preview: Mat

        val oriented = fixOrientation(image, this.settings.cameraPreProcessing.warp)
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
                if (!this@CalibrationActivity.measured) {
                    onMeasureFocalLength(focalLength)
                }
            }
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Could not find target", e)
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

        this.views.cameraBridgeViewBase.setCvCameraViewListener(this.targetFinderCamera)
        this.views.cameraBridgeViewBase.enableView()

        val calibrationSettings = this.settings.calibration
        @SuppressLint("SetTextI18n")
        this.views.readout.text = """
            Configured initial distance: ${calibrationSettings.initialDistance}m
            Configured target size: ${calibrationSettings.targetSize}m
            If incorrect, change in settings
            If correct, place at initial distance and calibrate
        """.trimIndent()

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }

        this.views.calibrateButton.setOnClickListener {
            this.views.cameraBridgeViewBase.setCvCameraViewListener(this.focalLengthMeasureCamera)
        }

        this.views.returnButton.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this)
    }

    // Focal length measurement callback -----------------------------------------------------------

    private fun onMeasureFocalLength(focalLength: Double) {
        this.views.cameraBridgeViewBase.disableView()

        if (!this.measured) {
            this.measured = true
            PreferenceManager.getDefaultSharedPreferences(this).edit().also { editor ->
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

    private inner class Views(
        val cameraBridgeViewBase: CameraBridgeViewBase = findViewById(R.id.calibrationActivityCameraView),
        val readout: TextView = findViewById(R.id.calibrationActivityReadout),
        val settingsButton: Button = findViewById(R.id.calibrationActivitySettingsButton),
        val calibrateButton: Button = findViewById(R.id.calibrationActivityCalibrateButton),
        val returnButton: Button = findViewById(R.id.calibrationActivityReturnButton),
    )

    companion object {
        const val TAG = "CalibrationActivity"

        fun getIntent(c: Context) = Intent(c, CalibrationActivity::class.java)
    }
}