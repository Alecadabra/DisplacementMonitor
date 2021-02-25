package displacement.monitor.cv.controller

import displacement.monitor.cv.controller.ImageOperations.drawTarget
import displacement.monitor.cv.controller.ImageOperations.fixOrientation
import displacement.monitor.cv.controller.ImageOperations.resizeWithBorder
import displacement.monitor.cv.model.Contour
import displacement.monitor.settings.model.Settings
import org.opencv.core.Mat

/**
 * Encapsulates the logic and parameters required to measure the distance to a target in a given
 * image.
 */
class CalibratedImageProcessor(
    private val settings: Settings,
    private val targetMeasurement: TargetMeasurement,
    private val targetFinder: TargetFinder = TargetFinder(settings),
) {

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
        val imageOriented: Mat = fixOrientation(image, this.settings.camera.warp)
        previewDest?.values = resizeWithBorder(imageOriented, image.size())

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)
        previewDest?.values = resizeWithBorder(drawTarget(imageOriented, target), image.size())

        // Measure the distance
        return this.targetMeasurement.measureDistance(target)
    }


    // Helper extension property
    private var Mat.values
        get() = this
        set(value) = value.assignTo(this)
}