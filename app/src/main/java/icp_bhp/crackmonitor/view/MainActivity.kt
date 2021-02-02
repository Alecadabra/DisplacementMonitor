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
import icp_bhp.crackmonitor.controller.cv.CalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.UncalibratedImageProcessor
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    private val uncalibratedImageProcessor by lazy {
        UncalibratedImageProcessor(Settings(PreferenceManager.getDefaultSharedPreferences(this)))
    }

    private var calibratedImageProcessor: CalibratedImageProcessor? = null

    private val views by lazy { Views() }

    private val measurementCamera = CameraFrameCallback { image ->
        val calibrated = this@MainActivity.calibratedImageProcessor
            ?: error("Measurement camera used without calibration")

        val preview = image.clone()

        try {
            val measurement = calibrated.measure(image, preview)
            // TODO Look into withContext()
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

        startActivity(PermissionHandlerActivity.getIntent(this))

        initialiseOpenCV()

        this.views.cameraBridgeViewBase.also { camera ->
            camera.disableView()
        }

        this.views.measureButton.also { btn ->
            btn.setOnClickListener {
                this.views.cameraBridgeViewBase.setCvCameraViewListener(this.measurementCamera)
                this.views.cameraBridgeViewBase.enableView()
            }
            btn.isClickable = this.calibratedImageProcessor != null
            btn.isEnabled = this.calibratedImageProcessor != null
        }

        this.views.calibrateButton.setOnClickListener {
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
        this.calibratedImageProcessor = calibrated
        this.views.measureButton.also { btn ->
            btn.isClickable = this.calibratedImageProcessor != null
            btn.isEnabled = this.calibratedImageProcessor != null
        }
        this.views.cameraBridgeViewBase.disableView()
    }

    private fun onNewMeasurement(measurement: Double) {
        @SuppressLint("SetTextI18n")
        this.views.measurement.text = "Measurement: ${"%.4f".format(measurement)}m"
        this.views.cameraBridgeViewBase.disableView()
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