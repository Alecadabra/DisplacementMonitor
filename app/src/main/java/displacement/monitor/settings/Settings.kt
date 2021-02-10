package displacement.monitor.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.opencv.core.Size
import java.util.*

/**
 * Holds a snapshot of all settings values. If settings change, make a new instance of this object.
 */
class Settings(private val preferences: SharedPreferences) {

    // Secondary constructor -----------------------------------------------------------------------

    constructor(context: Context) : this(
        PreferenceManager.getDefaultSharedPreferences(context)
    )

    init {
        // Test generate all settings holder classes so exceptions are thrown eagerly
        Calibration()
        Camera()
        PeriodicMeasurement()
        TargetFinding()
    }

    // Settings holder class references ------------------------------------------------------------

    val calibration: Calibration
        get() = Calibration()

    val camera: Camera
        get() = Camera()

    val periodicMeasurement: PeriodicMeasurement
        get() = PeriodicMeasurement()

    val targetFinding: TargetFinding
        get() = TargetFinding()

    // Helper functions ----------------------------------------------------------------------------

    private fun getDouble(key: String, predicate: (Double) -> Boolean = { true }): Double? {
        return this.preferences.getString(key, null)?.toDoubleOrNull()?.takeIf(predicate)
    }

    private fun getInt(key: String, predicate: (Int) -> Boolean  = { true }): Int? {
        return this.preferences.getString(key, null)?.toIntOrNull()?.takeIf(predicate)
    }

    private fun getBoolean(key: String, predicate: (Boolean) -> Boolean  = { true }): Boolean? {
        return this.preferences.getBoolean(key, true).takeIf(predicate)?.takeIf {
            this.preferences.contains(key)
        }
    }

    // Settings holder classes ---------------------------------------------------------------------

    inner class Calibration {
        val targetSize = getDouble("calibration_targetSize") { it > 0 }
            ?: error("Target size must be a number greater than 0")

        val initialDistance = getDouble("calibration_initialDistance") { it > 0 }
            ?: error("Target size must be a number greater than 0")

        val focalLength: Double = getDouble("calibration_focalLength") { it >= 0 }
            ?: error("Focal length must be a number greater than or equal to zero")
    }

    inner class Camera {
        val warp = getBoolean("camera_warp")
            ?: error("Internal error getting camera pre-processing warp flag")

        val camIdx = getInt("camera_camIdx")
            ?: error("Internal error getting camera index")
    }

    inner class PeriodicMeasurement {
        val period: Int = getInt("periodicMeasurement_period") { it >= 1 }
            ?: error("Period must be a whole number greater than or equal to one")
    }

    inner class TargetFinding {
        /**
         * Blur size for Gaussian blur algorithm used to help with find edges algorithm (Canny)
         */
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