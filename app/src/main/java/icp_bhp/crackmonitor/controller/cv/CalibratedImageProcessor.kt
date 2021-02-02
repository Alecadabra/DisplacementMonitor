package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import org.opencv.core.Mat

class CalibratedImageProcessor(
    settings: Settings,
    targetFinder: TargetFinder,
    private val targetMeasurement: TargetMeasurement
) : UncalibratedImageProcessor(settings, targetFinder) {

    // Public entry points -------------------------------------------------------------------------

    /**
     * Measures the distance to the target in the image.
     * @param image Image with the target in view
     * @return The real measurement
     * @throws IllegalStateException If the target is not found or cannot be measured
     */
    fun measure(image: Mat, previewDest: Mat? = null): Double {
        check(!image.empty()) { "Image is empty" }

        // Orient image properly
        val imageOriented: Mat = fixOrientation(image)
        previewDest?.setTo(resizeWithBorder(imageOriented, image.size()))

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)
        previewDest?.setTo(resizeWithBorder(drawTarget(imageOriented, target), image.size()))

        // Measure the distance
        return this.targetMeasurement.measureDistance(target)
    }
}