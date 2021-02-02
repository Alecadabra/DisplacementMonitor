package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.PermissionHandler
import icp_bhp.crackmonitor.controller.cv.CalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.UncalibratedImageProcessor
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*
import org.opencv.core.Mat
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity() {

    private val views by lazy { Views() }

    private val uncalibratedImageProcessor by lazy {
        UncalibratedImageProcessor(Settings(PreferenceManager.getDefaultSharedPreferences(this)))
    }

    private var calibratedImageProcessor: CalibratedImageProcessor? = null

    private var cameraState = CameraState.DISABLED
        set(value) {
            field = value
            val camera = this.views.cameraBridgeViewBase
            when (value) {
                CameraState.CALIBRATING -> {
                    camera.setCvCameraViewListener(this.calibrationCamera)
                    camera.enableView()
                }
                CameraState.MEASURING -> {
                    camera.setCvCameraViewListener(this.measurementCamera)
                    camera.enableView()
                }
                CameraState.DISABLED -> {
                    camera.disableView()
                }
            }
        }

    private val measurementCamera = CameraFrameCallback { image ->
        val calibrated = this@MainActivity.calibratedImageProcessor
            ?: error("Measurement camera used without calibration")

        val preview = image.clone()

        try {
            val measurement = calibrated.measure(image, preview)
            CoroutineScope(Dispatchers.Main).launch {
                this@MainActivity.onNewMeasurement(measurement)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Could not measure image", e)
        }

        image.release()

        return@CameraFrameCallback preview
    }

    private val calibrationCamera = CameraFrameCallback { image ->
        val preview = image.clone()

        try {
            val uncalibrated = this@MainActivity.uncalibratedImageProcessor
            val calibrated = uncalibrated.calibrated(image, preview)
            CoroutineScope(Dispatchers.Main).launch {
                this@MainActivity.onCalibration(calibrated)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Could not calibrate", e)
        }

        image.release()

        return@CameraFrameCallback preview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Check if any permissions are needed
        run {
            val permissionHandler = PermissionHandler(this)
            val requiredPerms = PermissionHandler.Permission.values().filterNot { perm ->
                permissionHandler.hasPermission(perm)
            }
            if (requiredPerms.isNotEmpty()) {
                startActivity(PermissionHandlerActivity.getIntent(this))
            }
        }

        // Set up views

        this.views.measureButton.also { btn ->
            btn.setOnClickListener {
                this.cameraState = CameraState.MEASURING
                this.views.cameraBridgeViewBase.setCvCameraViewListener(this.measurementCamera)
                this.views.cameraBridgeViewBase.enableView()
            }
            btn.isClickable = this.calibratedImageProcessor != null
            btn.isEnabled = this.calibratedImageProcessor != null
        }

        this.views.calibrateButton.setOnClickListener {
            this.cameraState = CameraState.CALIBRATING
            this.views.cameraBridgeViewBase.setCvCameraViewListener(this.calibrationCamera)
            this.views.cameraBridgeViewBase.enableView()
        }

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsFragment.SettingsActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV()
    }

    private fun onCalibration(calibrated: CalibratedImageProcessor) {
        if (this.cameraState == CameraState.CALIBRATING) {
            this.cameraState = CameraState.DISABLED
            this.calibratedImageProcessor = calibrated
            this.views.measureButton.also { btn ->
                btn.isClickable = this.calibratedImageProcessor != null
                btn.isEnabled = this.calibratedImageProcessor != null
            }
        }
    }

    private fun onNewMeasurement(measurement: Double) {
        if (this.cameraState == CameraState.MEASURING) {
            this.cameraState = CameraState.DISABLED
            @SuppressLint("SetTextI18n")
            this.views.measurement.text = "Measurement: ${"%.4f".format(measurement)}m"
        }
    }

    private fun initialiseOpenCV() {
        val loaderCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> {
                        Log.i(TAG, "OpenCV loaded successfully")
                    }
                    else -> super.onManagerConnected(status)
                }
            }
        }

        if (OpenCVLoader.initDebug()) {
            // Successful load
            Log.d(TAG, "OpenCV reloaded successfully")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            // Unsuccessful internal load
            Log.d(TAG, "OpenCV failed to load internally - using OpenCV Manager")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCallback)
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraBridgeViewBase: CameraBridgeViewBase = findViewById(R.id.mainActivityCameraView),
        val measureButton: Button = findViewById(R.id.mainActivityMeasureButton),
        val calibrateButton: Button = findViewById(R.id.mainActivityCalibrateButton),
        val settingsButton: Button = findViewById(R.id.mainActivitySettingsButton),
        val measurement: TextView = findViewById(R.id.mainActivityMeasurement),
    )

    private enum class CameraState {
        CALIBRATING,
        MEASURING,
        DISABLED
    }

    companion object {
        const val TAG = "MainActivity"

        // Shortcut class for the OpenCV Camera to simplify implementation code
        private class CameraFrameCallback(
            val onFrame: (Mat) -> Mat
        ) : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {}

            override fun onCameraViewStopped() {}

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
                return this.onFrame(inputFrame.rgba())
            }
        }
    }
}