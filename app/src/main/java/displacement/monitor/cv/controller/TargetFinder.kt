package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import displacement.monitor.cv.model.toContour
import displacement.monitor.settings.model.Settings
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

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
 * Contains the logic with which a target [Contour] can be found from a source image [Mat].
 */
class TargetFinder(private val settings: Settings) {

    /**
     * Finds a target [Contour] from an image matrix.
     * @param image Image Mat
     * @return Contour most likely to be the target
     * @throws IllegalStateException If target was not found
     */
    fun findTarget(image: Mat): Contour {
        // Apply find edges
        val imageEdges = findEdges(image)

        // List of all contours
        val allContours: List<Contour> = mutableListOf<MatOfPoint>().also { matOfPointList ->
            // Find contours
            Imgproc.findContours(
                imageEdges, // Source image
                matOfPointList, // Dest list of contours
                Mat(), // Dest hierarchy matrix - unused
                Imgproc.RETR_LIST, // Hierarchy mode
                Imgproc.CHAIN_APPROX_SIMPLE // Contour approximation method
            )
        }.map { it.toContour() }

        val filteredList = allContours
            // Filter out contours with num edges far from rectangular
            .filter(this@TargetFinder::isRoughlyRectangular)
            // Filter out contours not roughly square
            .filter(this@TargetFinder::isSquareEnough)
            // Filter out contours not large enough
            .filter(this@TargetFinder::isLargeEnough)
            // Filter out contours with low solidity
            .filter(this@TargetFinder::isSolidEnough)

        // Return the approx curve of the largest area contour, assuming that is the target
        return filteredList.map { contour ->
            contour.approxCurve
        }.maxByOrNull { contour ->
            contour.area
        } ?: error("Target not found")
    }

    /**
     * Applies a find edges effect to the image
     */
    private fun findEdges(image: Mat): Mat {
        val imageEdges = image.clone()
        val (cannyThreshold1, cannyThreshold2) = this.settings.targetFinding.let {
            it.cannyThreshold1 to it.cannyThreshold2
        }

        // Make greyscale
        Imgproc.cvtColor(image, imageEdges, Imgproc.COLOR_BGR2GRAY)
        // Slight gaussian blur
        Imgproc.GaussianBlur(imageEdges, imageEdges, this.settings.targetFinding.blurSize, 0.0)
        // Find edges
        Imgproc.Canny(imageEdges, imageEdges, cannyThreshold1, cannyThreshold2)

        return imageEdges
    }

    /**
     * Determines if a contour is roughly rectangular based on it's number of dimensions.
     */
    private fun isRoughlyRectangular(contour: Contour): Boolean {
        val acceptableRange = this.settings.targetFinding.let {
            it.targetMinEdges..it.targetMaxEdges
        }

        return contour.approxCurve.matOfPoint2f.height() in acceptableRange
    }

    /**
     * Determines if a contour is square based on it's aspect ratio.
     */
    private fun isSquareEnough(contour: Contour): Boolean {
        val boundingRec = contour.approxCurve.boundingRect
        val aspectRatio = boundingRec.width.toFloat() / boundingRec.height.toFloat()
        val acceptableRange = this.settings.targetFinding.let {
            it.targetMinAspectRatio..it.targetMaxAspectRatio
        }

        return aspectRatio in acceptableRange
    }

    /**
     * Determines if a contour is sufficiently large based on it's bounding rectangle's length and
     * width.
     */
    private fun isLargeEnough(contour: Contour): Boolean {
        val boundingRec = contour.approxCurve.boundingRect
        val minSize = this.settings.targetFinding.minTargetSize

        return boundingRec.width >= minSize && boundingRec.height >= minSize
    }

    /**
     * Determines if a contour is sufficiently solid based on how closely it matches it's convex
     * hull.
     */
    private fun isSolidEnough(contour: Contour): Boolean {
        val solidity = contour.area / contour.convexHull.area

        return solidity > this.settings.targetFinding.minTargetSolidity
    }

    /**
     * Helper extension property that leverages on a contour's approx curve epsilon value being
     * consistent within this class.
     */
    private val Contour.approxCurve: Contour
        get() {
            val epsilon = this@TargetFinder.settings.targetFinding.curveApproximationEpsilon
            return this.getApproxCurve(epsilon)
        }
}