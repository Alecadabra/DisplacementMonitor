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
            computeFocalLengthReal(
                    settings.calibration.initialDistance,
                    settings.calibration.targetSize,
                    computeSizePx(initialTarget)
            ),
            settings
    )

    // Public entry points -------------------------------------------------------------------------

    fun measureDistance(target: Contour): Double = computeDistanceReal(
            focalLengthReal = this.focalLengthReal,
            widthReal = this.settings.calibration.targetSize,
            widthPx = computeSizePx(target)
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
        fun computeFocalLengthReal(distanceReal: Double, widthReal: Double, widthPx: Double): Double {
            return (widthPx * distanceReal) / widthReal
        }

        /**
         * Calculates the distance to the camera using triangle similarity.
         * @param focalLengthReal Focal length of camera in metres
         * @param widthReal Real width of object in metres
         * @param widthPx Perceived width of object in pixels
         * @return Calculated distance in metres
         */
        fun computeDistanceReal(focalLengthReal: Double, widthReal: Double, widthPx: Double): Double {
            return (widthReal * focalLengthReal) / widthPx
        }

        /**
         * Calculates the perceived dimension of the presumed square contour.
         */
        fun computeSizePx(contour: Contour): Double {
            // Join points to edges cyclically
            val edges = contour.pointArray.mapIndexed { i, point ->
                if (i == contour.pointArray.lastIndex) {
                    // Join last to first
                    point to contour.pointArray[0]
                } else {
                    // Join current to next
                    point to contour.pointArray[i + 1]
                }
            }

            // Measure lengths with pythagoras
            val lengths = edges.map { (pt1, pt2) ->
                val a = abs(pt1.x - pt2.x)
                val b = abs(pt1.y - pt2.y)
                hypot(a, b)
            }

            // Use value of largest edge
            return lengths.maxOrNull() ?: error("Target edges not found")
        }
    }
}