package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

open class UncalibratedImageProcessor(
    protected val settings: Settings,
    protected val targetFinder: TargetFinder = TargetFinder(settings),
) {

    // Public entry points -------------------------------------------------------------------------

    /**
     * Measures the target in the image and uses it to generate and return a calibrated image
     * processor.
     * @param image Image with the target in view
     * @param previewDest Optional destination matrix for user-understandable image
     * @return The generated [CalibratedImageProcessor]
     * @throws CalibratedImageProcessor If the target is not found or cannot be measured
     */
    fun calibrated(image: Mat, previewDest: Mat? = null): CalibratedImageProcessor {
        check(!image.empty()) { "Image is empty" }

        // Orient image properly
        val imageOriented: Mat = fixOrientation(image, this.settings.cameraPreProcessing.warp)
        previewDest?.values = resizeWithBorder(imageOriented, image.size())

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)
        previewDest?.values = resizeWithBorder(drawTarget(imageOriented, target), image.size())

        // Construct the TargetMeasurement object
        val targetMeasurement = TargetMeasurement(target, this.settings)

        // Wrap this class in the CalibratedImageProcessor interface
        return CalibratedImageProcessor(this.settings, this.targetFinder, targetMeasurement)
    }
}
