package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.cv.*
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.CameraBridgeViewBase

class RealTimeMeasureActivity : AppCompatActivity() {

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

    private val cameraFrameCallback = CameraFrameCallback { image ->
        val preview = image.clone()

        try {
            val measurement = this.calibratedImageProcessor.measure(image, preview)
            val text = "Measured value: ${"%.4f".format(measurement)}m"
            CoroutineScope(Dispatchers.Main).launch {
                this@RealTimeMeasureActivity.views.measurement.text = text
            }
        } catch (e: IllegalStateException) {
            CoroutineScope(Dispatchers.Main).launch {
                @SuppressLint("SetTextI18n")
                this@RealTimeMeasureActivity.views.measurement.text = "Looking for target..."
            }
        }

        return@CameraFrameCallback preview
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time_measure)

        this.title = "Real-Time Measurement"

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        this.views.cameraBridgeViewBase.setCvCameraViewListener(this.cameraFrameCallback)
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this, TAG)
        this.views.cameraBridgeViewBase.enableView()
    }

    override fun onStop() {
        super.onStop()

        this.views.cameraBridgeViewBase.disableView()
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraBridgeViewBase: CameraBridgeViewBase = findViewById(R.id.realTimeActivityCameraView),
        val measurement: TextView = findViewById(R.id.realTimeActivityMeasurement)
    )

    companion object {
        private const val TAG = "RealTimeMeasureActivity"

        fun getIntent(context: Context) = Intent(context, RealTimeMeasureActivity::class.java)
    }
}