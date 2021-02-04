package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.cv.CalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.CameraFrameCallback
import icp_bhp.crackmonitor.controller.cv.TargetMeasurement
import icp_bhp.crackmonitor.controller.cv.initialiseOpenCV
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

class ScheduledMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val settings by lazy { Settings(this) }

    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(
            settings = this.settings,
            targetMeasurement = TargetMeasurement(
                focalLengthReal = this.settings.calibration.focalLength,
                settings = this.settings
            )
        )
    }

    /** Flag for if a value for distance has been measured */
    private var measured: Boolean = false

    private var failedAttempts = 0

    private val cameraFrameCallback = CameraFrameCallback { image ->
        val preview = image.clone()

        try {
            val measurement = this.calibratedImageProcessor.measure(image, preview)
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@ScheduledMeasurementActivity.measured) {
                    this@ScheduledMeasurementActivity.onDistanceMeasured(measurement)
                }
            }
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to measure distance (${e.message}")
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@ScheduledMeasurementActivity.measured) {
                    val failuresText = this@ScheduledMeasurementActivity.failedAttempts.let {
                        if (it < 10) {
                            ".".repeat(it / 2)
                        } else {
                            "\nFailed Attempts: $it"
                        }
                    }
                    val text = "Looking for target$failuresText"
                    this@ScheduledMeasurementActivity.views.readout.text = text
                    this@ScheduledMeasurementActivity.failedAttempts++
                }
            }
        }

        return@CameraFrameCallback preview
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_measurement)

        setFlags()

        this.title = "Measuring Distance"

        Log.d("ScheduledMeasurement", "Activity started")

        this.views.cameraBridgeViewBase.setCvCameraViewListener(this.cameraFrameCallback)
        this.views.cameraBridgeViewBase.enableView()
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this, TAG)
    }

    // Local helper functions ----------------------------------------------------------------------

    private fun setFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    private fun onDistanceMeasured(distance: Double) {
        this.views.cameraBridgeViewBase.disableView()

        if (!this.measured) {
            this.measured = true

            @SuppressLint("SetTextI18n")
            this.views.readout.text = "Measured value of ${"%.2f".format(distance)}m"

            Log.i(TAG, "Measured value of ${"%.2f".format(distance)}m")

            // Finish up and close
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            finish()
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraBridgeViewBase: CameraBridgeViewBase = findViewById(R.id.scheduledMeasurementActivityCameraView),
        val readout: TextView = findViewById(R.id.scheduledMeasurementActivityReadout)
    )

    companion object {
        private const val TAG = "ScheduledMeasurement"

        fun getIntent(c: Context) = Intent(c, ScheduledMeasurementActivity::class.java)
    }
}