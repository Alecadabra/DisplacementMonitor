package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import displacement.monitor.cv.model.toContour
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.hypot

object ContourOperations {

    fun area(contour: Contour): Double = Imgproc.contourArea(contour.matOfPoint)

    fun convexHull(contour: Contour): Contour {
        // Get MatOfInt from convexHull function
        val hullIntMat = MatOfInt().also { Imgproc.convexHull(contour.matOfPoint, it) }
        val hullIndexes = hullIntMat.toList()
        val points = contour.pointArray
        val hullPoints = Array(hullIntMat.height()) { i ->
            points[hullIndexes[i]]
        }

        return hullPoints.toContour()
    }

    fun boundingRect(contour: Contour): Rect = Imgproc.boundingRect(contour.matOfPoint)

    fun approxCurve(contour: Contour, epsilon: Double): Contour {
        return MatOfPoint2f().also { mat ->
            val perimeter = Imgproc.arcLength(contour.matOfPoint2f, true)
            val fullEpsilon = epsilon * perimeter

            Imgproc.approxPolyDP(contour.matOfPoint2f, mat, fullEpsilon, true)
        }.toContour()
    }

    /**
     * Calculates the perceived dimension of the contour. Assumes the contour is square and
     * it's points are ordered in a consecutive fashion, eg. clockwise.
     */
    fun edgeLength(contour: Contour): Double {
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
