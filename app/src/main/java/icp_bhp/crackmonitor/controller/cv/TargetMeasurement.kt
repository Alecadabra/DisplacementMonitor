package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import kotlin.math.abs
import kotlin.math.hypot

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
        focalLengthReal = computeFocalLengthReal(
            settings.calibration.initialDistance,
            settings.calibration.targetSize,
            initialTarget.edgeLength
        ),
        settings = settings
    )

    // Public entry points -------------------------------------------------------------------------

    fun measureDistance(target: Contour): Double = computeDistanceReal(
        focalLengthReal = this.focalLengthReal,
        widthReal = this.settings.calibration.targetSize,
        widthPx = target.edgeLength
    )

    companion object {

        // Measurement logic -----------------------------------------------------------------------

        /**
         * Calculates the focal length of the camera using triangle similarity.
         * @param distanceReal Distance between the camera and object in metres
         * @param widthReal Measured width of the object in metres
         * @param widthPx Perceived width of object in pixels
         * @return Focal length in metres
         */
        fun computeFocalLengthReal(
            distanceReal: Double,
            widthReal: Double,
            widthPx: Double
        ): Double = (widthPx * distanceReal) / widthReal

        /**
         * Calculates the distance to the camera using triangle similarity.
         * @param focalLengthReal Focal length of camera in metres
         * @param widthReal Real width of object in metres
         * @param widthPx Perceived width of object in pixels
         * @return Calculated distance in metres
         */
        fun computeDistanceReal(
            focalLengthReal: Double,
            widthReal: Double,
            widthPx: Double
        ): Double = (widthReal * focalLengthReal) / widthPx
    }
}