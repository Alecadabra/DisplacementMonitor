package displacement.monitor.android.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.android.controller.DeviceStateController
import displacement.monitor.android.view.CustomCameraView
import displacement.monitor.cv.controller.CalibratedImageProcessor
import displacement.monitor.cv.controller.CameraFrameCallback
import displacement.monitor.cv.controller.TargetMeasurement
import displacement.monitor.cv.controller.initialiseOpenCV
import displacement.monitor.database.model.Measurement
import displacement.monitor.database.local.MeasurementDatabase
import displacement.monitor.database.remote.RemoteDBController
import displacement.monitor.settings.Settings
import kotlinx.coroutines.*

class ScheduledMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val settings by lazy { Settings(this) }

    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(this.settings, TargetMeasurement(this.settings))
    }

    // Handles all the very specific ways you make android turn on/off the device
    private val deviceStateController by lazy { DeviceStateController(this) }

    private val remoteDBController = RemoteDBController()

    /** Flag for if a value for distance has been measured */
    private var measured: Boolean = false

    private var failedAttempts = 0

    private val cameraFrameCallback = CameraFrameCallback { image ->
        try {
            val distance = this.calibratedImageProcessor.measure(image)
            if (!this.measured) {
                val measurement = Measurement(
                    time = System.currentTimeMillis() / 1000L,
                    distance = distance,
                    failedAttempts = this.failedAttempts
                )
                CoroutineScope(Dispatchers.Main).launch {
                    this@ScheduledMeasurementActivity.onDistanceMeasured(measurement)
                }
            }
        } catch (e: IllegalStateException) {
            Log.i(TAG, "Image processing - Failed to measure distance (${e.message})")
            CoroutineScope(Dispatchers.Main).launch {
                val activity = this@ScheduledMeasurementActivity

                if (!activity.measured) {

                    // Turn on the flash if it's needed
                    if (activity.failedAttempts > MAX_FAILS) {
                        launch { activity.views.cameraView.flashOn() }
                    }

                    activity.failedAttempts++
                }
            }
        }

        return@CameraFrameCallback image
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.deviceStateController.start()

        setContentView(R.layout.activity_scheduled_measurement)

        this.views.cameraView.start(this.settings, this.cameraFrameCallback)
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this, TAG)
    }

    // Local helper functions ----------------------------------------------------------------------

    private fun onDistanceMeasured(measurement: Measurement) {
        // Disable camera
        this.views.cameraView.stop()

        if (!this.measured) {
            this.measured = true

            Log.i(TAG, "Measured value of ${"%.2f".format(measurement.distance)}m")

            // Log measurement to database
            CoroutineScope(Dispatchers.IO).launch {
                val db = MeasurementDatabase { applicationContext }
                db.measurementDao().insert(measurement)
            }

            // Send measurement to Influx
            CoroutineScope(Dispatchers.IO).launch {
                this@ScheduledMeasurementActivity.remoteDBController.writeMeasurement(measurement)
                this@ScheduledMeasurementActivity.remoteDBController.close()
            }

            this.deviceStateController.finish()
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.scheduledMeasurementActivityCameraView),
    )

    companion object {
        private const val TAG = "ScheduledMeasurement"

        private const val MAX_FAILS = 20

        fun getIntent(c: Context) = Intent(c, ScheduledMeasurementActivity::class.java)
    }
}