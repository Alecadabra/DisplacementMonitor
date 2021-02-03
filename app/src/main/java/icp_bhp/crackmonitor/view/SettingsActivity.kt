package icp_bhp.crackmonitor.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import icp_bhp.crackmonitor.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "Settings"

        supportFragmentManager.beginTransaction().also { transaction ->
            transaction.replace(R.id.settingsActivityFrame, SettingsFragment())
            transaction.commit()
        }
    }

    // Preference Fragment -------------------------------------------------------------------------

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Set EditText input types
            val inputDecimal = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            val inputInt = InputType.TYPE_CLASS_NUMBER
            listOf(
                "calibration_targetSize" to inputDecimal,
                "calibration_initialDistance" to inputDecimal,
                "calibration_focalLength" to inputDecimal,
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

                activity?.supportFragmentManager?.beginTransaction()?.also { transaction ->
                    transaction.replace(R.id.settingsActivityFrame, SettingsFragment())
                    transaction.commit()
                }

                return@setOnPreferenceClickListener true
            }
        }
    }

    companion object {
        fun getIntent(c: Context) = Intent(c, SettingsActivity::class.java)
    }
}

