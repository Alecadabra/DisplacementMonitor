package icp_bhp.crackmonitor.controller

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.toContour
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc

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
}
