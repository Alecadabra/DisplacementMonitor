package displacement.monitor.setup.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.database.local.view.activity.DatabaseViewActivity
import displacement.monitor.scheduling.controller.DeviceStateController
import displacement.monitor.scheduling.controller.SchedulingManager
import displacement.monitor.scheduling.view.activity.ScheduledMeasurementActivity
import displacement.monitor.settings.model.Settings
import displacement.monitor.settings.view.activity.SettingsActivity
import displacement.monitor.setup.view.activity.SetupActivity.Companion.construct
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy(this::Views)

    /** Access to app settings. */
    private val settings: Settings by lazy { Settings(this) }

    /** Handles scheduling the measurements. */
    private val schedulingManager: SchedulingManager by lazy {
        SchedulingManager(this, this.settings, ScheduledMeasurementActivity.getIntent(this))
    }

    /** Handles locking the screen once scheduling starts. */
    private val deviceStateController: DeviceStateController by lazy {
        DeviceStateController(this)
    }

    /**
     * Exit conformation dialog to show when back is pressed.
     */
    private val backDialog: AlertDialog by lazy {
        AlertDialog.Builder(this).also { builder ->
            builder.setTitle("Exit app")
            builder.setMessage(
                "Do you want to exit the app? If any measurements are scheduled they will be cancelled"
            )
            builder.setNegativeButton("No, Cancel") { dialog, _ -> dialog.dismiss() }
            builder.setPositiveButton("Yes, exit") { _, _ -> super.onBackPressed() }
        }.create()
    }

    private val scheduleStartDialog: AlertDialog by lazy {
        AlertDialog.Builder(this).also { builder ->
            builder.setTitle("Scheduling started")
            builder.setMessage(
                "Do not close this app. You may now lock the device and leave it to take measurements"
            )
            builder.setPositiveButton("Lock device now") { _, _ ->
                this.deviceStateController.lockScreen()
            }
            builder.setNeutralButton("Dismiss") { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up views
        this.views.scheduleStartBtn.setOnClickListener {
            this.schedulingManager.start()
            updateReadouts()
            this.scheduleStartDialog.show()
        }
        this.views.scheduleStopBtn.setOnClickListener {
            this.schedulingManager.cancel()
            updateReadouts()
        }
        this.views.setupBtn.setOnClickListener {
            startActivity(SetupActivity.getIntent(this))
        }
        this.views.settingsBtn.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
        this.views.dataBtn.setOnClickListener {
            startActivity(DatabaseViewActivity.getIntent(this))
        }
        this.views.singleMeasurementBtn.setOnClickListener {
            startActivity(ScheduledMeasurementActivity.getIntent(this, useStateController = false))
        }
        this.views.realTimeBtn.setOnClickListener {
            startActivity(RealTimeMeasurementActivity.getIntent(this))
        }
        this.views.calibrateBtn.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        // Start setup if needed
        startSetup()

        updateReadouts()
    }

    override fun finish() {
        // Cancel any scheduling
        try {
            this.schedulingManager.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cancel scheduling", e)
        }

        super.finish()
    }

    override fun onBackPressed() {
        // Show exit conformation dialog
        this.backDialog.show()
    }

    // Local logic ---------------------------------------------------------------------------------

    /**
     * Starts the setup screens if required.
     */
    private fun startSetup() {
        // Setup is complete if all pages return true on canAdvance
        val needSetup = SetupActivity.PAGE_CLASSES.any { pageClass ->
            !pageClass.construct().canAdvance(this)
        }

        if (needSetup) {
            startActivity(SetupActivity.getIntent(this))
        }
    }

    /** Update the text shown in readout text views to be up to date with new data */
    private fun updateReadouts() {
        try {
            @SuppressLint("SetTextI18n")
            this.views.scheduleReadout.text = "Handle the periodic measuring here. Measurements are ${
                if (this.schedulingManager.isScheduled) "" else "not"
            } currently scheduled"

            @SuppressLint("SetTextI18n")
            this.views.settingsReadout.text = """
            Current Settings
            Device ID: ${this.settings.periodicMeasurement.id}
            Measurement Period: ${this.settings.periodicMeasurement.period} minute(s)
            Target size: ${this.settings.calibration.targetSize}m
            Remote Database: ${if (this.settings.remoteDB.enabled) "Enabled" else "Disabled"}
        """.trimIndent()
        } catch (e: Exception) {
            Log.e(TAG, "Could not update readouts", e)
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val scheduleStartBtn: Button = findViewById(R.id.mainActivitySchedulingStartButton),
        val scheduleStopBtn: Button = findViewById(R.id.mainActivitySchedulingStopButton),
        val scheduleReadout: TextView = findViewById(R.id.mainActivitySchedulingReadout),
        val setupBtn: Button = findViewById(R.id.mainActivitySetupLaunchButton),
        val settingsBtn: Button = findViewById(R.id.mainActivitySettingsButton),
        val settingsReadout: TextView = findViewById(R.id.mainActivitySettingsReadout),
        val dataBtn: Button = findViewById(R.id.mainActivityDataButton),
        val singleMeasurementBtn: Button = findViewById(R.id.mainActivitySingleMeasurementButton),
        val realTimeBtn: Button = findViewById(R.id.mainActivityRealTimeButton),
        val calibrateBtn: Button = findViewById(R.id.mainActivityCalibrateButton),
    )

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, MainActivity::class.java)
    }
}