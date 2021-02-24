package displacement.monitor.settings.model

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.opencv.core.Size

/**
 * Gives access to all app settings values through [preferences]. Access the values through the
 * member class instances corresponding to each preference category; [periodicMeasurement],
 * [calibration], [camera] & [targetFinding].
 * @param preferences Instance of [SharedPreferences] that values are pulled from, probably
 * generated with [PreferenceManager.getDefaultSharedPreferences]
 */
class Settings(val preferences: SharedPreferences) {

    // Secondary constructor -----------------------------------------------------------------------

    /**
     * Constructs the [preferences] property with [PreferenceManager.getDefaultSharedPreferences].
     */
    constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context))

    init {
        // Test generate all settings holder classes so exceptions are thrown eagerly
        PeriodicMeasurement()
        Calibration()
        Camera()
        RemoteDB()
        TargetFinding()
    }

    // Settings holder class references ------------------------------------------------------------

    val periodicMeasurement: PeriodicMeasurement
        get() = PeriodicMeasurement()

    val calibration: Calibration
        get() = Calibration()

    val camera: Camera
        get() = Camera()

    val remoteDB: RemoteDB
        get() = RemoteDB()

    val targetFinding: TargetFinding
        get() = TargetFinding()

    // Helper functions to pull values from SharedPreferences --------------------------------------

    private fun getDouble(key: String, predicate: (Double) -> Boolean = { true }): Double? {
        return this.preferences.getString(key, null)?.toDoubleOrNull()?.takeIf(predicate)
    }

    private fun getInt(key: String, predicate: (Int) -> Boolean = { true }): Int? {
        return this.preferences.getString(key, null)?.toIntOrNull()?.takeIf(predicate)
    }

    private fun getBoolean(key: String, predicate: (Boolean) -> Boolean = { true }): Boolean? {
        return this.preferences.getBoolean(key, true).takeIf(predicate)?.takeIf {
            this.preferences.contains(key)
        }
    }

    private fun getString(key: String, predicate: (String) -> Boolean = { true }): String? {
        return this.preferences.getString(key, null)?.takeIf(predicate)
    }

    // Settings holder classes ---------------------------------------------------------------------

    inner class PeriodicMeasurement {
        val id: String = getString("periodicMeasurement_id") { it.isNotBlank() }
            ?: error("Device ID must not be blank")

        val period: Int = getInt("periodicMeasurement_period") { it >= 1 }
            ?: error("Period must be a whole number greater than or equal to one")
    }

    inner class Calibration {
        val targetSize = getDouble("calibration_targetSize") { it > 0 }
            ?: error("Target size must be a number greater than 0")

        val initialDistance = getDouble("calibration_initialDistance") { it > 0 }
            ?: error("Initial distance must be a number greater than 0")

        val focalLength: Double = getDouble("calibration_focalLength") { it >= 0 }
            ?: error("Focal length must be a number greater than zero, or zero to signify that it is not yet measured")
    }

    inner class Camera {
        val warp = getBoolean("camera_warp")
            ?: error("Internal error getting camera pre-processing warp flag")

        val camIdx = getInt("camera_camIdx")
            ?: 0

        val brightnessThreshold: Float = getDouble("camera_flashThreshold") { it in 0f..100f }
            ?.let { it / 100f }?.toFloat()
            ?: error("Flash brightness threshold must be a number between 0 and 100")
    }

    inner class RemoteDB {
        val enabled = getBoolean("remoteDB_enable")
            ?: error("Internal error getting remote db enabled flag")

        private val _url: String? = if (this.enabled) {
            getString("remoteDB_url")?.takeUnless { it.isBlank() }
                ?: error(
                    "Remote database URL must be set, or the remote database should be disabled"
                )
        } else null
        val url: String
            get() = _url ?: error("Cannot get remote URL token if the remote database is disabled")

        private val _token: String? = if (this.enabled) {
            getString("remoteDB_token")?.takeUnless { it.isBlank() }
                ?: error(
                    "Remote database URL must be set, or the remote database should be disabled"
                )
        } else null
        val token: String
            get() = _token
                ?: error("Cannot get remote database token if the remote database is disabled")

        private val _org: String? = if (this.enabled) {
            getString("remoteDB_org")?.takeUnless { it.isBlank() }
                ?: error(
                    "Remote database organisation must be set, or the remote database should be disabled"
                )
        } else null
        val org: String
            get() = _org
                ?: error(
                    "Cannot get remote database organisation if the remote database is disabled"
                )

        private val _bucket: String? = if (this.enabled) {
            getString("remoteDB_bucket")?.takeUnless { it.isBlank() }
                ?: error(
                    "Remote database bucket must be set, or the remote database should be disabled"
                )
        } else null
        val bucket: String
            get() = _bucket
                ?: error(
                    "Cannot get remote database bucket if the remote database is disabled"
                )
    }

    inner class TargetFinding {
        val blurSize: Size = this@Settings.preferences.getInt("targetFinding_blurSize", -1)
            .takeIf { it > 0 && it % 2 != 0 }?.toDouble()?.let { Size(it, it) }
            ?: error("Blur amount must be positive and odd")

        val cannyThreshold1 = getDouble("targetFinding_cannyThreshold1")
            ?: error("Canny threshold 1 must be a number")

        val cannyThreshold2 = getDouble("targetFinding_cannyThreshold2")
            ?: error("Canny threshold 2 must be a number")

        val curveApproximationEpsilon = getDouble("targetFinding_curveApproximationEpsilon")
            ?: error("Curve approximation epsilon must be a number")

        val targetMinEdges = getInt("targetFinding_targetMinEdges")
            ?: error("Target min number of edges must be a number")

        val targetMaxEdges = getInt("targetFinding_targetMaxEdges")
            ?: error("Target max number of edges must be a number")

        val targetMinAspectRatio = getDouble("targetFinding_minAspectRatio")
            ?: error("Target min aspect ratio must be a number")

        val targetMaxAspectRatio = getDouble("targetFinding_maxAspectRatio")
            ?: error("Target max aspect ratio must be a number")

        val minTargetSize = getInt("targetFinding_minTargetSize")
            ?: error("Minimum perceived target size must be a number")

        val minTargetSolidity: Double = getDouble("targetFinding_minSolidity")
            ?: error("Minimum target solidity must be a number")
    }
}