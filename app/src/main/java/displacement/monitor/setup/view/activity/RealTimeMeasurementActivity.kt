package displacement.monitor.setup.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.cv.controller.CustomCameraView
import displacement.monitor.cv.*
import displacement.monitor.cv.controller.*
import displacement.monitor.scheduling.controller.DeviceStateController
import displacement.monitor.settings.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * SetupActivity used to test the measurement algorithms and parameters in real time.
 */
class RealTimeMeasurementActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy(this::Views)

    /** Access to app settings. */
    private val settings by lazy { Settings(this) }

    /** Handles going to fullscreen mode. */
    private val deviceStateController by lazy { DeviceStateController(this) }

    /**
     * The calibrated image processor used to do all measurement.
     */
    private val calibratedImageProcessor by lazy {
        CalibratedImageProcessor(
            settings = this.settings,
            targetMeasurement = TargetMeasurement(this.settings)
        )
    }

    /**
     * Camera callback used to attempt to measure the image and set the text in the UI accordingly.
     */
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

        this.deviceStateController.goFullscreen()

        setContentView(R.layout.activity_real_time_measure)

        this.title = "Real-Time Measurement"

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val cameraView: CustomCameraView = findViewById(R.id.realTimeActivityCameraView),
        val measurement: TextView = findViewById(R.id.realTimeActivityMeasurement)
    )

    companion object {
        private const val TAG = "RealTimeMeasurement"

        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, RealTimeMeasurementActivity::class.java)
    }
}