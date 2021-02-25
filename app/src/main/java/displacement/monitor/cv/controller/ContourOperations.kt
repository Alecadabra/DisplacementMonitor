package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import displacement.monitor.cv.model.toContour
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.hypot

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * Holds commonly used operations done on [Contours][Contour].
 */
object ContourOperations {

    /**
     * Calculates the area of the contour in square pixels.
     */
    fun area(contour: Contour): Double = Imgproc.contourArea(contour.matOfPoint)

    /**
     * Calculates the convex hull of a contour - the contour modified to have no concave points.
     */
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

    /**
     * Calculates the bounding rectangle of a contour.
     */
    fun boundingRect(contour: Contour): Rect = Imgproc.boundingRect(contour.matOfPoint)

    /**
     * Calculates the approximate polygonal curve of a contour using the Douglas-Pecker algorithm
     * with the given accuracy epsilon.
     */
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
