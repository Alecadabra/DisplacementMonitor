package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import icp_bhp.crackmonitor.model.toContour
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

class TargetFinder(private val settings: Settings) {

    /**
     * Finds all the target from an input of an image run through [UncalibratedImageProcessor.findEdges]
     * @param image Image Mat ran through [UncalibratedImageProcessor.findEdges]
     * @return Contour most likely to be the target
     * @throws IllegalStateException If target was not found
     */
    fun findTarget(image: Mat): Contour {
        // Matrix where each element gives info on the contour at that index
        //val hierarchy = Mat()

        // Apply find edges effect
        val imageEdges = findEdges(image)

        // List of all contours
        val allContours: List<Contour> = mutableListOf<MatOfPoint>().also { matOfPointList ->
            // Find contours
            Imgproc.findContours(
                imageEdges, // Source image
                matOfPointList, // Dest list of contours
                Mat(), // Dest hierarchy matrix
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

    private fun isRoughlyRectangular(contour: Contour): Boolean {
        val acceptableRange = this.settings.targetFinding.let {
            it.targetMinEdges..it.targetMaxEdges
        }

        return contour.approxCurve.matOfPoint2f.height() in acceptableRange
    }

    private fun isSquareEnough(contour: Contour): Boolean {
        val boundingRec = contour.approxCurve.boundingRect
        val aspectRatio = boundingRec.width.toFloat() / boundingRec.height.toFloat()
        val acceptableRange = this.settings.targetFinding.let {
            it.targetMinAspectRatio..it.targetMaxAspectRatio
        }

        return aspectRatio in acceptableRange
    }

    private fun isLargeEnough(contour: Contour): Boolean {
        val boundingRec = contour.approxCurve.boundingRect
        val minSize = this.settings.targetFinding.minTargetSize

        return boundingRec.width >= minSize && boundingRec.height >= minSize
    }

    private fun isSolidEnough(contour: Contour): Boolean {
        val solidity = contour.area / contour.convexHull.area

        return solidity > this.settings.targetFinding.minTargetSolidity
    }

    private val Contour.approxCurve: Contour
        get() {
            val epsilon = this@TargetFinder.settings.targetFinding.curveApproximationEpsilon
            return this.getApproxCurve(epsilon)
        }
}