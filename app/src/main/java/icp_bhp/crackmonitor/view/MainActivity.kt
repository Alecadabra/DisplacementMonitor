package icp_bhp.crackmonitor.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import icp_bhp.crackmonitor.R
import icp_bhp.crackmonitor.controller.PermissionHandler
import icp_bhp.crackmonitor.controller.SchedulingManager
import icp_bhp.crackmonitor.controller.database.MeasurementDatabase
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

        // Initialise database
        CoroutineScope(Dispatchers.IO).launch {
            MeasurementDatabase.instance = MeasurementDatabase.get(applicationContext)
        }

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

        this.views.clearDataButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                MeasurementDatabase.instance.measurementDao().clear()
                withContext(Dispatchers.Main) {
                    onResume()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            val data = MeasurementDatabase.instance.measurementDao().getAll()
            val text = data.joinToString(separator = "\n") { (timestampSeconds, distance) ->
                val format = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ROOT)
                val dateTime = Date(timestampSeconds * 1000)

                "${format.format(dateTime)} - ${"%.2f".format(distance)}m"
            }
            withContext(Dispatchers.Main) {
                this@MainActivity.views.data.text = text
            }
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
        val data: TextView = findViewById(R.id.mainActivityData),
        val clearDataButton: Button = findViewById(R.id.mainActivityClearDataButton),
    )

    companion object {
        private const val TAG = "MainActivity"
    }
}
