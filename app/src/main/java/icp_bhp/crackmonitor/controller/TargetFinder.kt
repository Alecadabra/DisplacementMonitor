package icp_bhp.crackmonitor.controller

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import icp_bhp.crackmonitor.model.toContour
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

class TargetFinder(private val settings: Settings) {
    /**
     * Finds all the target from an input of an image run through [ImageProcessor.findEdges]
     * @param imgEdges Image Mat ran through [ImageProcessor.findEdges]
     * @return Contour most likely to be the target
     * @throws IllegalStateException If target was not found
     */
    fun findTarget(imgEdges: Mat): Contour {
        // Matrix where each element gives info on the contour at that index
        //val hierarchy = Mat()

        // List of all contours
        val allContours: List<Contour> = mutableListOf<MatOfPoint>().also { matOfPointList ->
            // Find contours
            Imgproc.findContours(
                imgEdges, // Source image
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

        // Return the approx curves, sorted by largest area first
        return filteredList.map { contour ->
            contour.approxCurve
        }.maxByOrNull { contour ->
            contour.area
        } ?: error("Target not found")
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