package displacement.monitor.settings.view.activity

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import displacement.monitor.R
import displacement.monitor.settings.model.Settings

/**
 * SetupActivity to show the [SettingsActivity.SettingsFragment][SettingsFragment]
 * [Preference Fragment][PreferenceFragmentCompat] to allow the user to view and modify the
 * app settings.
 */
class SettingsActivity : AppCompatActivity() {

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        this.title = "Settings"

        // Place the Settings Fragment in the frame
        supportFragmentManager.beginTransaction().also { transaction ->
            transaction.replace(R.id.settingsActivityFrame, SettingsFragment())
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        // Make sure all settings are valid before leaving
        try {
            // Constructing a Settings object throws an exception if any preferences are invalid
            Settings(this)
            super.onBackPressed()
        } catch (e: IllegalStateException) {
            AlertDialog.Builder(this).also { builder ->
                builder.setTitle("Invalid setting")
                builder.setMessage(e.message)
                builder.setNeutralButton("Dismiss") { dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
        }
    }

    // Preference Fragment -------------------------------------------------------------------------

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Set EditText input types programmatically, as it doesn't seem to work in xml
            val inputDecimal = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            val inputInt = InputType.TYPE_CLASS_NUMBER
            // List preference keys by their input type
            listOf(
                "calibration_targetSize" to inputDecimal,
                "calibration_initialDistance" to inputDecimal,
                "calibration_focalLength" to inputDecimal,
                "periodicMeasurement_period" to inputInt,
                "camera_flashThreshold" to inputDecimal,
                "targetFinding_cannyThreshold1" to inputDecimal,
                "targetFinding_cannyThreshold2" to inputDecimal,
                "targetFinding_curveApproximationEpsilon" to inputDecimal,
                "targetFinding_targetMinEdges" to inputInt,
                "targetFinding_targetMaxEdges" to inputInt,
                "targetFinding_minAspectRatio" to inputDecimal,
                "targetFinding_maxAspectRatio" to inputDecimal,
                "targetFinding_minTargetSize" to inputInt,
                "targetFinding_minSolidity" to inputDecimal
            ).forEach { (key, inputType) ->
                findPreference<EditTextPreference>(key)?.setOnBindEditTextListener { editText ->
                    editText.inputType = inputType
                    editText.selectAll()
                } ?: error("Internal error, could not get $key")
            }

            // Set reset to default functionality
            findPreference<Preference>("reset_resetToDefault")?.setOnPreferenceClickListener {
                // Clear preferences
                PreferenceManager.getDefaultSharedPreferences(this.context).edit().also { editor ->
                    editor.clear()
                    editor.apply()
                }

                Toast.makeText(this.context, "Settings reset to defaults", Toast.LENGTH_LONG).show()

                // Put a new instance of this fragment in it's frame
                activity?.supportFragmentManager?.beginTransaction()?.also { transaction ->
                    transaction.replace(R.id.settingsActivityFrame, SettingsFragment())
                    transaction.commit()
                }

                return@setOnPreferenceClickListener true
            }

            // Camera index selection logic
            findPreference<ListPreference>("camera_camIdx")?.also { pref ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val cameraManager = context?.getSystemService(CAMERA_SERVICE) as CameraManager
                    val cameras = cameraManager.cameraIdList

                    // Set output values
                    pref.entryValues = cameras.indices.toList().map { it.toString() }.toTypedArray()

                    // Map camera facing characteristics to human readable description
                    val facingMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mapOf(
                            CameraCharacteristics.LENS_FACING_BACK to "Back",
                            CameraCharacteristics.LENS_FACING_FRONT to "Front",
                            // External only exists about API 23
                            CameraCharacteristics.LENS_FACING_EXTERNAL to "External",
                        )
                    } else {
                        mapOf(
                            CameraCharacteristics.LENS_FACING_BACK to "Back",
                            CameraCharacteristics.LENS_FACING_FRONT to "Front",
                        )
                    }.withDefault { "Unknown" }

                    // Set human readable values
                    pref.entries = cameras.map { cameraIdx ->
                        val characteristics = cameraManager.getCameraCharacteristics(cameraIdx)
                        // Key to the facingMap
                        val facingKey = characteristics.get(CameraCharacteristics.LENS_FACING) ?: -1
                        val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                        val flashString = if (hasFlash) "has" else "no"

                        "ID $cameraIdx - ${facingMap.getValue(facingKey)} camera, $flashString flash"
                    }.toTypedArray()

                    // Set default value
                    if (pref.value == null) {
                        pref.setValueIndex(0)
                    }
                } else {
                    // API level not high enough to select camera
                    pref.isVisible = false
                }
            }
        }
    }

    companion object {
        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, SettingsActivity::class.java)
    }
}