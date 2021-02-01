package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.cv.ImageProcessor
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*
import org.opencv.core.Mat
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var imageProcessor: ImageProcessor

    private val views by lazy { Views() }

    private val cameraListener = object : CameraBridgeViewBase.CvCameraViewListener2 {
        override fun onCameraViewStarted(width: Int, height: Int) {}

        override fun onCameraViewStopped() {}

        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
            val img = inputFrame.rgba()

            CoroutineScope(Dispatchers.Main).launch {
                if (this@MainActivity.imageProcessor.isCalibrated) {
                    try {
                        val measurement = this@MainActivity.imageProcessor.processMeasurement(img)
                        this@MainActivity.views.cameraBridgeViewBase.disableView()
                        this@MainActivity.onNewMeasurement(measurement)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception trying to measure", e)
                    }

                } else {
                    try {
                        this@MainActivity.imageProcessor.calibrate(img)
                        this@MainActivity.views.cameraBridgeViewBase.disableView()
                        this@MainActivity.views.measureButton.also {
                            it.isClickable = true
                            it.isEnabled = true
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception trying to calibrate", e)
                        this@MainActivity.views.measureButton.also {
                            it.isClickable = false
                            it.isEnabled = false
                        }
                    }
                }
            }

            return img
        }
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
            camera.setCvCameraViewListener(this.cameraListener)
        }

        this.views.measureButton.also { btn ->
            btn.setOnClickListener {
                this.views.cameraBridgeViewBase.enableView()
            }
            btn.isClickable = this::imageProcessor.isInitialized
            btn.isEnabled = this::imageProcessor.isInitialized
        }
        this.views.measureButton.setOnClickListener {
            this.views.cameraBridgeViewBase.enableView()
        }

        this.views.calibrateButton.setOnClickListener {
            this.imageProcessor = ImageProcessor(
                Settings(PreferenceManager.getDefaultSharedPreferences(this))
            )
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

    private fun onNewMeasurement(measurement: Double) {
        @SuppressLint("SetTextI18n")
        this.views.measurement.text = "Measurement: ${measurement}m"
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
    }
}