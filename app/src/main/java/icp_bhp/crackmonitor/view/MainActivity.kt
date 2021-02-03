package icp_bhp.crackmonitor.view

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.AlarmReceiver
import icp_bhp.crackmonitor.controller.CameraFrameCallback
import icp_bhp.crackmonitor.controller.PermissionHandler
import icp_bhp.crackmonitor.controller.SchedulingManager
import icp_bhp.crackmonitor.controller.cv.CalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.UncalibratedImageProcessor
import icp_bhp.crackmonitor.controller.cv.initialiseOpenCV
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.*

class MainActivity : AppCompatActivity() {

    private val views by lazy { Views() }



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

        this.views.calibrateButton.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(this))
        }

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }

        this.views.scheduleButton.setOnClickListener {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = AlarmReceiver.getPendingIntent(this)
            val settings = Settings(this)
            val scheduleManager = SchedulingManager(alarmManager, alarmIntent, settings)
            scheduleManager.start()
            Log.i(TAG, "Started scheduling")
        }

        this.views.testButton.setOnClickListener {
            startActivity(RealTimeMeasureActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        initialiseOpenCV(this)
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val calibrateButton: Button = findViewById(R.id.mainActivityCalibrateButton),
        val settingsButton: Button = findViewById(R.id.mainActivitySettingsButton),
        val scheduleButton: Button = findViewById(R.id.mainActivityScheduleButton),
        val testButton: Button = findViewById(R.id.mainActivityTestButton),
    )

    companion object {
        const val TAG = "MainActivity"
    }
}

/*
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

 */