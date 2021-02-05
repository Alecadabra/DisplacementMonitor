package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.DeviceStateController
import icp_bhp.crackmonitor.controller.cv.CalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.CameraFrameCallback
import icp_bhp.crackmonitor.controller.cv.TargetMeasurement
import icp_bhp.crackmonitor.controller.cv.initialiseOpenCV
import icp_bhp.crackmonitor.controller.database.Measurement
import icp_bhp.crackmonitor.controller.database.MeasurementDatabase
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.*
import org.opencv.android.CameraBridgeViewBase

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

    // Handles all the very specific ways you make android turn on/off the device
    private val deviceStateController by lazy { DeviceStateController(this) }

    /** Flag for if a value for distance has been measured */
    private var measured: Boolean = false

    private var failedAttempts = 0

    private val cameraFrameCallback = CameraFrameCallback { image ->
        val preview = image.clone()

        try {
            val measurement = this.calibratedImageProcessor.measure(image, preview)
            val unixTimestamp = System.currentTimeMillis() / 1000L
            CoroutineScope(Dispatchers.Main).launch {
                if (!this@ScheduledMeasurementActivity.measured) {
                    this@ScheduledMeasurementActivity.onDistanceMeasured(unixTimestamp, measurement)
                }
            }
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to measure distance (${e.message})")
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

        this.deviceStateController.start()

        setContentView(R.layout.activity_scheduled_measurement)
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

    private fun onDistanceMeasured(unixTimestamp: Long, distance: Double) {
        this.views.cameraBridgeViewBase.disableView()
        this.views.cameraBridgeViewBase.visibility = View.GONE

        if (!this.measured) {
            this.measured = true

            @SuppressLint("SetTextI18n")
            this.views.readout.text = "Measured value of ${"%.2f".format(distance)}m"

            Log.i(TAG, "Measured value of ${"%.2f".format(distance)}m")

            CoroutineScope(Dispatchers.IO).launch {
                val db = MeasurementDatabase.get { applicationContext }
                val measurement = Measurement(unixTimestamp, distance)
                db.measurementDao().insert(measurement)
            }

            this.deviceStateController.finish()
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