package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import displacement.monitor.settings.Settings

class TargetMeasurement(private val settings: Settings) {

    // Public entry points -------------------------------------------------------------------------

    fun measureDistance(target: Contour): Double = distanceReal(
        focalLengthReal = this.settings.calibration.focalLength,
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