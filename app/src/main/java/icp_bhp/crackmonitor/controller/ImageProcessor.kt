package icp_bhp.crackmonitor.controller

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

class ImageProcessor(
    /** Measured initial distance between the camera and target being measured in metres */
    private val initialDistanceM: Double,
    /**
     * Settings reference. If settings change this [ImageProcessor] should be reconstructed.
     */
    private val settings: Settings,
) {

    // Members -------------------------------------------------------------------------------------

    /** Measured focal length */
    private var focalLength: Double? = null

    private val targetFinder = TargetFinder(this.settings)

    // Public entry points -------------------------------------------------------------------------

    fun processMeasurement(image: Mat): Double? {
        var measurement: Double? = null

        try {
            // Orient image properly
            val img: Mat = fixOrientation(image)

            // Find possible target
            val imgEdges: Mat = findEdges(img)
            val targetContour: Contour = this.targetFinder.findTarget(imgEdges)

            // Measure target
            val targetSizePx = getPerceivedMeasurement(targetContour)

            // Calculate focal length if needed
            val localFocalLength = this.focalLength ?: run {
                // Set focal length
                return@run computeFocalLength(
                    this.initialDistanceM,
                    this.settings.target.targetSize,
                    targetSizePx
                ).also {
                    // Update member
                    this.focalLength = it
                }
            }

            // Compute distance to rectangle in metres
            measurement = computeDistance(
                localFocalLength,
                this.settings.target.targetSize,
                targetSizePx
            )

        } catch (e: Exception) {}

        return measurement
    }

    // Local image processing ----------------------------------------------------------------------

    /**
     * OpenCV's camera is disoriented by 90 degrees, this fixes it and warps the image if required.
     */
    private fun fixOrientation(img: Mat): Mat {
        fun portraitMat() = Mat(img.height(), img.width(), CV_TYPE)
        fun landscapeMat() = Mat(img.width(), img.height(), CV_TYPE)

        // Warp correctly if required
        if (this.settings.cameraPreProcessing.warp) {
            val transposed = landscapeMat()
            val resized = portraitMat()
            val flipped = portraitMat()

            Core.transpose(img, transposed)
            Imgproc.resize(transposed, resized, resized.size(), 0.0, 0.0, 0)
            Core.flip(resized, flipped, 1)
            return flipped
        } else {
            val transposed = landscapeMat()
            val flipped = landscapeMat()

            Core.transpose(img, transposed)
            Core.flip(transposed, flipped, 1)
            return flipped
        }
    }

    /**
     * Applies a find edges effect to the image
     */
    private fun findEdges(img: Mat): Mat {
        val edges = Mat(img.size(), CV_TYPE)
        val (cannyThreshold1, cannyThreshold2) = this.settings.targetFinding.let {
            it.cannyThreshold1 to it.cannyThreshold2
        }

        // Make greyscale
        Imgproc.cvtColor(img, edges, Imgproc.COLOR_BGR2GRAY)
        // Slight gaussian blur
        Imgproc.GaussianBlur(edges, edges, this.settings.targetFinding.blurSize, 0.0)
        // Find edges
        Imgproc.Canny(edges, edges, cannyThreshold1, cannyThreshold2)

        return edges
    }

    /**
     * Calculates the focal length of the camera using triangle similarity.
     * @param distanceM Distance between the camera and object in metres
     * @param widthM Measured width of the object in metres
     * @param widthPix Perceived width of object in pixels
     * @return Focal length in metres
     */
    private fun computeFocalLength(distanceM: Double, widthM: Double, widthPix: Double): Double {
        return (widthPix * distanceM) / widthM
    }

    /**
     * Calculates the distance to the camera using triangle similarity.
     * @param focalLengthM Focal length of camera in metres
     * @param widthM Real width of object in metres
     * @param widthPix Perceived width of object in pixels
     * @return Calculated distance in metres
     */
    private fun computeDistance(focalLengthM: Double, widthM: Double, widthPix: Double): Double {
        return (widthM * focalLengthM) / widthPix
    }

    /**
     * Calculates the perceived dimension of the presumed square contour.
     * @param targetContour The [Contour] to measure
     * @return Perceived dimension of the square target in pixels
     */
    private fun getPerceivedMeasurement(targetContour: Contour): Double {
        val pointArray = targetContour.pointArray

        // Join points to edges cyclically
        val edges = pointArray.mapIndexed { i, point ->
            if (i == pointArray.lastIndex) {
                // Join last to first
                point to pointArray[0]
            } else {
                // Join current to next
                point to pointArray[i + 1]
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

    companion object {
        /** OpenCV data type used */
        val CV_TYPE = CvType.CV_8UC4
    }
}