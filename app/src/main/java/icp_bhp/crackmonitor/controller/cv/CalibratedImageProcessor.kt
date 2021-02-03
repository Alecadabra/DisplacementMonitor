package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import org.opencv.core.Mat

class CalibratedImageProcessor(
    settings: Settings,
    targetFinder: TargetFinder = TargetFinder(settings),
    private val targetMeasurement: TargetMeasurement
) : UncalibratedImageProcessor(settings, targetFinder) {

    // Public entry points -------------------------------------------------------------------------

    /**
     * Measures the distance to the target in the image.
     * @param image Image with the target in view
     * @param previewDest Optional destination matrix for user-understandable image
     * @return The real measurement
     * @throws IllegalStateException If the target is not found or cannot be measured
     */
    fun measure(image: Mat, previewDest: Mat? = null): Double {
        check(!image.empty()) { "Image is empty" }

        // Orient image properly
        val imageOriented: Mat = fixOrientation(image, this.settings.cameraPreProcessing.warp)
        previewDest?.values = resizeWithBorder(imageOriented, image.size())

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)
        previewDest?.values = resizeWithBorder(drawTarget(imageOriented, target), image.size())

        // Measure the distance
        return this.targetMeasurement.measureDistance(target)
    }
}