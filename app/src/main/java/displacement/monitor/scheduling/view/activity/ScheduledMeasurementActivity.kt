package displacement.monitor.scheduling.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.cv.controller.CustomCameraView
import displacement.monitor.cv.controller.*
import displacement.monitor.cv.controller.ImageOperations.measureCentroidBrightness
import displacement.monitor.database.local.controller.MeasurementDatabase
import displacement.monitor.database.model.Measurement
import displacement.monitor.database.remote.RemoteDBController
import displacement.monitor.scheduling.controller.DeviceStateController
import displacement.monitor.scheduling.controller.DistanceAggregator
import displacement.monitor.settings.model.Settings
import kotlinx.coroutines.*
import org.opencv.core.Mat

/**
 * SetupActivity that takes a single measurement using a [CalibratedImageProcessor] and sends it
 * to the remote database using a [RemoteDBController]. By default, uses a [DeviceStateController]
 * to wake the device when starting and lock it when done, to override this behaviour, pass the
 * `useStateController` to [getIntent] function.
 */
class ScheduledMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views */
    private val views by lazy { Views() }

    /** Access to app settings */
    private val settings by lazy { Settings(this) }

    /**
     * Handles taking the distance measurements from the images taken by the camera.
     */
    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(this.settings, TargetMeasurement(this.settings))
    }

    /**
     * Handles all the very specific ways you make android turn on/off the device, or null if
     * the intent was configured to not use it.
     */
    private val deviceStateController: DeviceStateController? by lazy {
        if (this.intent.getBooleanExtra(BUNDLE_USE_STATE_CONTROLLER, true)) {
            DeviceStateController(this)
        } else null
    }

    /**
     * Handles communication with the remote database. Null if the app is configured to not use it.
     */
    private val remoteDBController: RemoteDBController? by lazy {
        if (this.settings.remoteDB.enabled) {
            RemoteDBController(this.settings)
        } else null
    }

    /**
     * Takes in all measurements and activates the callback when a full measurement is taken.
     * This helps the measurements be consistent and account for the time taken for the camera
     * to focus.
     */
    private val distanceAggregator = DistanceAggregator(DISTANCE_COUNT) { onDistanceMeasured(it) }

    /**
     * Number of frames in which [CalibratedImageProcessor.measure] throws an exception.
     */
    private var failedAttempts = 0

    /**
     * Processing for each frame taken by the camera.
     */
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

        // Wake screen etc.
        this.deviceStateController?.start()

        setContentView(R.layout.activity_scheduled_measurement)

        // Initialise the lazy remote DB controller
        this.remoteDBController

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

    /**
     * Callback when a distance measurement has been taken.
     * @param distance The distance measured, in metres
     */
    private fun onDistanceMeasured(distance: Double) {
        // Disable camera
        this.views.cameraView.stop()

        // Record into full measurement object
        val measurement = Measurement(
            time = System.currentTimeMillis() / 1000,
            distance = distance,
            failedAttempts = this.failedAttempts,
            id = this.settings.periodicMeasurement.id
        )

        Log.i(TAG, "Measured value of ${"%.2f".format(measurement.distance)}m")

        // Log measurement to local database and send to remote database if enabled
        CoroutineScope(Dispatchers.IO).launch {
            val db = MeasurementDatabase { applicationContext }
            db.measurementDao().insert(measurement)
            this@ScheduledMeasurementActivity.remoteDBController?.send { applicationContext }
            this@ScheduledMeasurementActivity.remoteDBController?.close()
        }

        // Finish the activity, either through the state controller or directly
        this.deviceStateController?.finish() ?: finish()
    }

    /**
     * Callback when a distance measurement failed, due to [CalibratedImageProcessor.measure]
     * throwing an [IllegalStateException].
     * @param e The exception thrown by [CalibratedImageProcessor.measure]
     * @param image The image matrix being processed
     */
    private fun onMeasurementFailed(e: IllegalStateException, image: Mat) {
        Log.i(TAG, "Image processing - Failed to measure distance (${e.message})")

        // Turn on the flash if it's needed
        if (this.views.cameraView.flashMode != CustomCameraView.FlashMode.ON) {
            val threshold = this.settings.camera.brightnessThreshold
            val lazyBrightness = { measureCentroidBrightness(image) }

            /* Turn on if there has been lots of failed attempts as a last ditch attempt, or if
            the brightness of the image is measured to be low enough to warrant using the flash */
            if (this.failedAttempts > 10 || lazyBrightness() < threshold) {
                this.views.cameraView.flashMode = CustomCameraView.FlashMode.ON
            }
        }

        this.failedAttempts++
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.scheduledMeasurementActivityCameraView),
    )

    companion object {
        private const val TAG = "ScheduledMeasurement"

        /** Size value for the [distanceAggregator]. */
        private const val DISTANCE_COUNT = 15

        private const val BUNDLE_USE_STATE_CONTROLLER = "$TAG::useStateController"

        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @param useStateController Whether the activity should use a [DeviceStateController] to
         * wake/lock the device before/after a measurement is taken.
         * @return New intent to start this activity with
         */
        fun getIntent(
            c: Context,
            useStateController: Boolean = true
        ) = Intent(c, ScheduledMeasurementActivity::class.java).also {
            it.putExtra(BUNDLE_USE_STATE_CONTROLLER, useStateController)
        }
    }
}