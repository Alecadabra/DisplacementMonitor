package displacement.monitor.android.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.android.view.CustomCameraView
import displacement.monitor.cv.*
import displacement.monitor.cv.controller.CalibratedImageProcessor
import displacement.monitor.cv.controller.CameraFrameCallback
import displacement.monitor.cv.controller.TargetMeasurement
import displacement.monitor.cv.controller.initialiseOpenCV
import displacement.monitor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RealTimeMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val settings by lazy { Settings(this) }

    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(
            settings = this.settings,
            targetMeasurement = TargetMeasurement(this.settings)
        )
    }

    private val cameraFrameCallback = CameraFrameCallback { image ->
        val preview = image.clone()

        try {
            val measurement = this.calibratedImageProcessor.measure(image, preview)
            val text = "Measured value: ${"%.4f".format(measurement)}m"
            CoroutineScope(Dispatchers.Main).launch {
                this@RealTimeMeasurementActivity.views.measurement.text = text
            }
        } catch (e: IllegalStateException) {
            CoroutineScope(Dispatchers.Main).launch {
                @SuppressLint("SetTextI18n")
                this@RealTimeMeasurementActivity.views.measurement.text = "Looking for target..."
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
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this, TAG)
        this.views.cameraView.start(this.settings, this.cameraFrameCallback)
    }

    override fun onPause() {
        this.views.cameraView.stop()

        super.onPause()
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.realTimeActivityCameraView),
        val measurement: TextView = findViewById(R.id.realTimeActivityMeasurement)
    )

    companion object {
        private const val TAG = "RealTimeMeasurementActivity"

        fun getIntent(context: Context) = Intent(context, RealTimeMeasurementActivity::class.java)
    }
}