package displacement.monitor.android.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.android.controller.SchedulingManager
import displacement.monitor.database.local.MeasurementDatabase
import displacement.monitor.android.controller.permissions.Permission
import displacement.monitor.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    private val views by lazy { Views() }

    private val scheduleManager by lazy {
        SchedulingManager(
            context = this,
            settings = Settings(this),
            scheduledIntent = ScheduledMeasurementActivity.getIntent(this)
        )
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialise database
        CoroutineScope(Dispatchers.IO).launch {
            MeasurementDatabase { applicationContext }
        }

        // Set up views

        this.views.settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }

        this.views.calibrateButton.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(this))
        }

        this.views.testButton.setOnClickListener {
            startActivity(RealTimeMeasurementActivity.getIntent(this))
        }

        this.views.scheduleButton.setOnClickListener {
            this.scheduleManager.start()
        }

        this.views.cancelButton.setOnClickListener {
            this.scheduleManager.cancel()
        }

        this.views.measurementButton.setOnClickListener {
            startActivity(ScheduledMeasurementActivity.getIntent(this))
        }

        this.views.clearDataButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val db = MeasurementDatabase { applicationContext }
                db.measurementDao().clear()
                launch(Dispatchers.Main) {
                    onResume()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if any permissions are needed
        if (Permission.allPerms.any { !it.isGrantedTo(this) }) {
            startActivity(PermissionHandlerActivity.getIntent(this))
        }

        // Update database readout
        CoroutineScope(Dispatchers.IO).launch {
            val db = MeasurementDatabase { applicationContext }
            val data = db.measurementDao().getAll()
            val text = data.joinToString(
                separator = "\n",
                limit = 10,
                truncated = "(${data.size - 10} more)"
            ) { (timestampSeconds, distance) ->
                val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ROOT)
                val dateTime = Date(timestampSeconds * 1000)

                "${format.format(dateTime)} - ${"%.2f".format(distance)}m"
            }
            withContext(Dispatchers.Main) {
                this@MainActivity.views.data.text = text
            }
        }
    }

    override fun finish() {
        this.scheduleManager.cancel()

        super.finish()
    }

    // Local constructs ----------------------------------------------------------------------------

    private inner class Views(
        val settingsButton: Button = findViewById(R.id.mainActivitySettingsButton),
        val calibrateButton: Button = findViewById(R.id.mainActivityCalibrateButton),
        val testButton: Button = findViewById(R.id.mainActivityTestButton),
        val scheduleButton: Button = findViewById(R.id.mainActivityScheduleButton),
        val cancelButton: Button = findViewById(R.id.mainActivityCancelButton),
        val measurementButton: Button = findViewById(R.id.mainActivityMeasurementButton),
        val data: TextView = findViewById(R.id.mainActivityData),
        val clearDataButton: Button = findViewById(R.id.mainActivityClearDataButton),
    )

    companion object {
        private const val TAG = "MainActivity"
    }
}
