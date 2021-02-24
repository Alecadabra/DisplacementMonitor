package displacement.monitor.setup.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
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

        // Start setup if needed
        startSetup()

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
        this.views.realTimeButton.setOnClickListener {
            startActivity(RealTimeMeasurementActivity.getIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()

        updateReadouts()
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
        val setupComplete = SetupActivity.PAGE_CLASSES.all { pageClass ->
            pageClass.construct().canAdvance(this)
        }

        if (!setupComplete) {
            startActivity(SetupActivity.getIntent(this))
        }
    }

    /** Update the text shown in readout text views to be up to date with new data */
    private fun updateReadouts() {
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
        val realTimeButton: Button = findViewById(R.id.mainActivityRealTimeButton),
    )
}