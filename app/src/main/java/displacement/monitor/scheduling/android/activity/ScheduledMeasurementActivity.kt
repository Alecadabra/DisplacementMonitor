package displacement.monitor.scheduling.android.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.cv.android.view.CustomCameraView
import displacement.monitor.cv.controller.*
import displacement.monitor.cv.controller.ImageOperations.measureCentroidBrightness
import displacement.monitor.database.local.MeasurementDatabase
import displacement.monitor.database.model.Measurement
import displacement.monitor.database.remote.RemoteDBController
import displacement.monitor.scheduling.controller.DeviceStateController
import displacement.monitor.scheduling.controller.DistanceAggregator
import displacement.monitor.settings.model.Settings
import kotlinx.coroutines.*
import org.opencv.core.Mat

class ScheduledMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val settings by lazy { Settings(this) }

    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(this.settings, TargetMeasurement(this.settings))
    }

    // Handles all the very specific ways you make android turn on/off the device
    private val deviceStateController by lazy {
        if (this.intent.getBooleanExtra(BUNDLE_USE_STATE_CONTROLLER, true)) {
            DeviceStateController(this)
        } else null
    }

    private val remoteDBController = RemoteDBController()

    private val distanceAggregator = DistanceAggregator(MAX) { onDistanceMeasured(it) }

    private var failedAttempts = 0

    private val cameraFrameCallback = CameraFrameCallback { image ->
        try {
            val distance = calibratedImageProcessor.measure(image)
            this.distanceAggregator.addDistance(distance)
        } catch (e: IllegalStateException) {
            CoroutineScope(Dispatchers.Main).launch {
                this@ScheduledMeasurementActivity.onMeasurementFailed(e, image)
            }
        }

        return@CameraFrameCallback image
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.deviceStateController?.start()

        setContentView(R.layout.activity_scheduled_measurement)

        this.views.cameraView.start(this.settings, this.cameraFrameCallback)
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this)
        this.views.cameraView.start(this.settings, this.cameraFrameCallback)
    }

    override fun onPause() {
        this.views.cameraView.stop()

        super.onPause()
    }

    override fun finish() {
        this.views.cameraView.stop()

        super.finish()
    }

    // Local helper functions ----------------------------------------------------------------------

    private fun onDistanceMeasured(distance: Double) {
        // Disable camera
        this.views.cameraView.stop()

        val measurement = Measurement(
            time = System.currentTimeMillis() / 1000,
            distance = distance,
            failedAttempts = this.failedAttempts,
            id = this.settings.periodicMeasurement.id
        )

        Log.i(TAG, "Measured value of ${"%.2f".format(measurement.distance)}m")

        // Log measurement to local database and send to remote database
        CoroutineScope(Dispatchers.IO).launch {
            val db = MeasurementDatabase { applicationContext }
            db.measurementDao().insert(measurement)
            this@ScheduledMeasurementActivity.remoteDBController.send { applicationContext }
            this@ScheduledMeasurementActivity.remoteDBController.close()
        }

        this.deviceStateController?.finish() ?: finish()
    }

    private fun onMeasurementFailed(e: IllegalStateException, image: Mat) {
        Log.i(TAG, "Image processing - Failed to measure distance (${e.message})")

        // Turn on the flash if it's needed
        if (this.views.cameraView.flashMode != CustomCameraView.FlashMode.ON) {
            val threshold = this.settings.camera.brightnessThreshold
            val lazyBrightness = { measureCentroidBrightness(image) }

            if (this.failedAttempts > 10 || lazyBrightness() < threshold) {
                this.views.cameraView.flashMode = CustomCameraView.FlashMode.ON
            }
        }

        this.failedAttempts++
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.scheduledMeasurementActivityCameraView),
    )

    companion object {
        private const val TAG = "ScheduledMeasurement"

        private const val MAX = 15

        private const val BUNDLE_USE_STATE_CONTROLLER = "$TAG:useStateController"

        fun getIntent(
            c: Context,
            useStateController: Boolean = true
        ) = Intent(c, ScheduledMeasurementActivity::class.java).also {
            it.putExtra(BUNDLE_USE_STATE_CONTROLLER, useStateController)
        }
    }
}