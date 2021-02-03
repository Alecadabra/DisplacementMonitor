package icp_bhp.crackmonitor.model

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
        // Test generate all settings holder classes
        Calibration()
        CameraPreProcessing()
        TargetFinding()
    }

    // Settings holder class references ------------------------------------------------------------

    val calibration: Calibration
        get() = Calibration()

    val cameraPreProcessing: CameraPreProcessing
        get() = CameraPreProcessing()

    val targetFinding: TargetFinding
        get() = TargetFinding()

    // Settings holder classes ---------------------------------------------------------------------

    inner class Calibration {
        val targetSize: Double = this@Settings.preferences.getString("calibration_targetSize", null)
            ?.toDoubleOrNull()?.takeIf { it > 0 }
            ?: error("Target size must be a number greater than 0")

        val initialDistance: Double = this@Settings.preferences.getString("calibration_initialDistance", null)
            ?.toDoubleOrNull()?.takeIf { it > 0 }
            ?: error("Target size must be a number greater than 0")

        val focalLength: Double = this@Settings.preferences.getString("calibration_focalLength", null)
            ?.toDoubleOrNull()?.takeIf { it >= 0 }
            ?: error("Focal length must be a number greater than or equal to zero")
    }

    inner class CameraPreProcessing {
        val warp: Boolean = this@Settings.preferences.getBoolean("cameraPreProcessing_warp", true)
            .takeIf { this@Settings.preferences.contains("cameraPreProcessing_warp") }
            ?: error("Internal error getting camera pre-processing warp flag")
    }

    inner class TargetFinding {
        /**
         * Blur size for Gaussian blur algorithm used to help with find edges algorithm (Canny)
         */
        val blurSize: Size = this@Settings.preferences.getInt("targetFinding_blurSize", -1)
            .takeIf { it > 0 && it % 2 != 0 }?.toDouble()?.let { Size(it, it) }
            ?: error("Blur amount must be positive and odd")

        val cannyThreshold1: Double = this@Settings.preferences.getString("targetFinding_cannyThreshold1", null)
            ?.toDoubleOrNull()
            ?: error("Canny threshold 1 must be a number")

        val cannyThreshold2: Double = this@Settings.preferences.getString("targetFinding_cannyThreshold2", null)
            ?.toDoubleOrNull()
            ?: error("Canny threshold 2 must be a number")

        val curveApproximationEpsilon: Double = this@Settings.preferences.getString("targetFinding_curveApproximationEpsilon", null)
            ?.toDoubleOrNull()
            ?: error("Curve approximation epsilon must be a number")

        val targetMinEdges: Int = this@Settings.preferences.getString("targetFinding_targetMinEdges", null)
            ?.toIntOrNull()
            ?: error("Target min number of edges must be a number")

        val targetMaxEdges: Int = this@Settings.preferences.getString("targetFinding_targetMaxEdges", null)
            ?.toIntOrNull()
            ?: error("Target max number of edges must be a number")

        val targetMinAspectRatio: Double = this@Settings.preferences.getString("targetFinding_minAspectRatio", null)
            ?.toDoubleOrNull()
            ?: error("Target min aspect ratio must be a number")

        val targetMaxAspectRatio: Double = this@Settings.preferences.getString("targetFinding_maxAspectRatio", null)
            ?.toDoubleOrNull()
            ?: error("Target max aspect ratio must be a number")

        val minTargetSize: Int = this@Settings.preferences.getString("targetFinding_minTargetSize", null)
            ?.toIntOrNull()
            ?: error("Minimum perceived target size must be a number")

        val minTargetSolidity: Double = this@Settings.preferences.getString("targetFinding_minSolidity", null)
            ?.toDoubleOrNull()
            ?: error("Minimum target solidity must be a number")
    }
}