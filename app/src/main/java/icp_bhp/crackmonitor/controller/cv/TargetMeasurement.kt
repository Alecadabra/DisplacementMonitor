package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings

class TargetMeasurement(
    /** Measured focal length */
    private val focalLengthReal: Double,
    /** Settings reference */
    private val settings: Settings
) {

    // Secondary constructor -----------------------------------------------------------------------

    constructor(
        initialTarget: Contour,
        settings: Settings
    ) : this(
        focalLengthReal = focalLengthReal(
            distanceReal = settings.calibration.initialDistance,
            lengthReal = settings.calibration.targetSize,
            lengthPx = initialTarget.edgeLength
        ),
        settings = settings
    )

    // Public entry points -------------------------------------------------------------------------

    fun measureDistance(target: Contour): Double = distanceReal(
        focalLengthReal = this.focalLengthReal,
        lengthReal = this.settings.calibration.targetSize,
        lengthPx = target.edgeLength
    )

    companion object {

        // Measurement logic -----------------------------------------------------------------------

        /**
         * Calculates the focal length of the camera using triangle similarity.
         * @param distanceReal Distance between the camera and target (Real)
         * @param lengthReal Length of the target (Real)
         * @param lengthPx Length of the target (Perceived)
         * @return Computed focal length (Real)
         */
        fun focalLengthReal(distanceReal: Double, lengthReal: Double, lengthPx: Double): Double {
            return (lengthPx * distanceReal) / lengthReal
        }

        /**
         * Calculates the distance between the camera and target using triangle similarity.
         * @param focalLengthReal Focal length of camera (Real)
         * @param lengthReal Length of the target (Real)
         * @param lengthPx Length of the target (Perceived)
         * @return Calculated distance (Real)
         */
        fun distanceReal(focalLengthReal: Double, lengthReal: Double, lengthPx: Double): Double {
            return (lengthReal * focalLengthReal) / lengthPx
        }
    }
}