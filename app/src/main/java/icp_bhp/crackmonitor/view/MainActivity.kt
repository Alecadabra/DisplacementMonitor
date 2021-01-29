package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.ImageProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*
import org.opencv.core.Mat

class MainActivity : AppCompatActivity() {

    private var imageProcessor: ImageProcessor? = null

    private val views by lazy {
        Views(
                cameraBridgeViewBase = findViewById(R.id.mainActivityCameraView),
                shutterButton = findViewById(R.id.mainActivityShutterButton),
                measurement = findViewById(R.id.mainActivityMeasurement),
        )
    }

    private val cameraListener = object : CameraBridgeViewBase.CvCameraViewListener2 {
        override fun onCameraViewStarted(width: Int, height: Int) {}

        override fun onCameraViewStopped() {}

        override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
            val img = inputFrame.rgba()

            CoroutineScope(Dispatchers.Main).launch {
                this@MainActivity.imageProcessor?.processMeasurement(img)?.also { measurement ->
                    this@MainActivity.views.cameraBridgeViewBase.disableView()
                    onNewMeasurement(measurement)
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

        // TODO Initialise image processor, set initial distance and calibrate functionality

        this.views.cameraBridgeViewBase.also { camera ->
            camera.disableView()
            camera.setCvCameraViewListener(this.cameraListener)
        }

        this.views.shutterButton.setOnClickListener {
            this.views.cameraBridgeViewBase.enableView()
        }
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV()
    }

    private fun onNewMeasurement(measurement: Double) {
        @SuppressLint("SetTextI18n")
        this.views.measurement.text = "${measurement}m"
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

    private class Views(
            val cameraBridgeViewBase: CameraBridgeViewBase,
            val shutterButton: Button,
            val measurement: TextView,
    )

    companion object {
        const val TAG = "MainActivity"
    }
}