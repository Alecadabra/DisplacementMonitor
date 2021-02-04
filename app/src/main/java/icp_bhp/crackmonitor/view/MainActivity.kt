package icp_bhp.crackmonitor.view

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.PermissionHandler
import icp_bhp.crackmonitor.controller.SchedulingManager
import icp_bhp.crackmonitor.controller.cv.CameraFrameCallback
import icp_bhp.crackmonitor.model.Settings

class MainActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val scheduleManager by lazy {
        SchedulingManager(this, Settings(this), ScheduledMeasurementActivity.getIntent(this))
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if any permissions are needed
        val permissionHandler = PermissionHandler(this)
        val requiredPerms = PermissionHandler.Permission.values().none { perm ->
            permissionHandler.hasPermission(perm)
        }
        if (requiredPerms) {
            startActivity(PermissionHandlerActivity.getIntent(this))
        }

        // Set up views

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }

        this.views.calibrateButton.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(this))
        }

        this.views.testButton.setOnClickListener {
            startActivity(RealTimeMeasureActivity.getIntent(this))
        }

        this.views.scheduleButton.setOnClickListener {
            this.scheduleManager.start()
        }

        this.views.cancelButton.setOnClickListener {
            this.scheduleManager.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        this.scheduleManager.cancel()
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val settingsButton: Button = findViewById(R.id.mainActivitySettingsButton),
        val calibrateButton: Button = findViewById(R.id.mainActivityCalibrateButton),
        val testButton: Button = findViewById(R.id.mainActivityTestButton),
        val scheduleButton: Button = findViewById(R.id.mainActivityScheduleButton),
        val cancelButton: Button = findViewById(R.id.mainActivityCancelButton),
    )

    companion object {
        private const val TAG = "MainActivity"
    }
}
