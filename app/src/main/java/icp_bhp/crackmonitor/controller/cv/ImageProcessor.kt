package icp_bhp.crackmonitor.controller.cv

import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ImageProcessor(
    /**
     * Settings reference. If settings change this [ImageProcessor] should be reconstructed.
     */
    private val settings: Settings,
) {

    // Members -------------------------------------------------------------------------------------

    private val targetFinder = TargetFinder(this.settings)

    private lateinit var targetMeasurement: TargetMeasurement

    // Public entry points -------------------------------------------------------------------------

    val isCalibrated: Boolean
        get() = this::targetMeasurement.isInitialized

    /**
     * Calibrates this ImageProcessor with an image of the target at the configured initial
     * distance.
     * @param image Image with the target in view
     * @throws IllegalStateException If the target is not found or cannot be measured
     */
    fun calibrate(image: Mat) {
        // Orient image properly
        val imageOriented: Mat = fixOrientation(image)

        // Find possible target
        val initialTarget: Contour = this.targetFinder.findTarget(imageOriented)

        this.targetMeasurement = TargetMeasurement(initialTarget, this.settings)
    }

    /**
     * Measures the distance to the target in the image.
     * @param image Image with the target in view
     * @throws IllegalStateException If the target is not found or cannot be measured, or if
     * [calibrate] has not been called
     */
    fun processMeasurement(image: Mat): Double {
        check(this::targetMeasurement.isInitialized) {
            "ImageProcessor must be calibrated before use with ${this::calibrate.name}"
        }

        // Orient image properly
        val imageOriented: Mat = fixOrientation(image)

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)

        return this.targetMeasurement.measureDistance(target)
    }

    // Local image processing ----------------------------------------------------------------------

    /**
     * OpenCV's camera is disoriented by 90 degrees, this fixes it and warps the image if required.
     */
    private fun fixOrientation(img: Mat): Mat {
        fun portraitMat() = Mat(img.height(), img.width(), CV_TYPE)
        fun landscapeMat() = Mat(img.width(), img.height(), CV_TYPE)

        // Warp correctly if required
        if (this.settings.cameraPreProcessing.warp) {
            val transposed = landscapeMat()
            val resized = portraitMat()
            val flipped = portraitMat()

            Core.transpose(img, transposed)
            Imgproc.resize(transposed, resized, resized.size(), 0.0, 0.0, 0)
            Core.flip(resized, flipped, 1)
            return flipped
        } else {
            val transposed = landscapeMat()
            val flipped = landscapeMat()

            Core.transpose(img, transposed)
            Core.flip(transposed, flipped, 1)
            return flipped
        }
    }

    companion object {
        /** OpenCV data type used */
        val CV_TYPE = CvType.CV_8UC4
    }
}