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
        val imageOriented: Mat = fixOrientation(image)
        previewDest?.values = resizeWithBorder(imageOriented, image.size())

        // Find possible target
        val target: Contour = this.targetFinder.findTarget(imageOriented)
        previewDest?.values = resizeWithBorder(drawTarget(imageOriented, target), image.size())

        // Construct the TargetMeasurement object
        val targetMeasurement = TargetMeasurement(target, this.settings)

        // Wrap this class in the CalibratedImageProcessor interface
        return CalibratedImageProcessor(this.settings, this.targetFinder, targetMeasurement)
    }

    // Local image processing ----------------------------------------------------------------------

    /**
     * OpenCV's camera is disoriented by 90 degrees, this fixes it and warps the image if required.
     */
    protected fun fixOrientation(image: Mat): Mat {
        fun blankImage() = Mat(image.height(), image.width(), CV_TYPE)
        fun blankImageTransposed() = Mat(image.width(), image.height(), CV_TYPE)

        // Warp correctly if required
        if (this.settings.cameraPreProcessing.warp) {
            val original = image.clone()
            val transposed = blankImageTransposed()
            val resized = blankImage()
            val flipped = blankImage()

            Core.transpose(original, transposed)
            Imgproc.resize(transposed, resized, resized.size(), 0.0, 0.0, 0)
            Core.flip(resized, flipped, 1)
            return flipped
        } else {
            val original = image.clone()
            val transposed = blankImageTransposed()
            val flipped = blankImageTransposed()

            Core.transpose(original, transposed)
            Core.flip(transposed, flipped, 1)
            return flipped
        }
    }

    /**
     * Draws the given target onto the image and returns the result.
     */
    protected fun drawTarget(image: Mat, target: Contour): Mat {
        val drawnImage = image.clone()

        // Draw target outline
        Imgproc.drawContours(
            drawnImage,
            listOf(target.matOfPoint),
            0,
            Scalar(0.0, 125.0, 255.0),
            (target.edgeLength.toInt() / 32).coerceAtLeast(5)
        )

        // Draw 4 corners of rectangle
        target.pointArray.also { pointArray ->
            val size = (target.edgeLength.toInt() / 16).coerceAtLeast(10)
            pointArray.forEach { point ->
                Imgproc.circle(drawnImage, point, size, Scalar(0.0, 255.0, 127.0), Core.FILLED)
            }
        }

        return drawnImage
    }

    /**
     * Resizes the source image to the given size without distortion, adding a black border, and
     * returns the result.
     */
    protected fun resizeWithBorder(source: Mat, newSize: Size): Mat {
        val oldSize = source.size()

        // Helper extension function
        fun Size.ratio() = this.width / this.height

        // Determine borders to add
        val borderY = when {
            oldSize.ratio() >= newSize.ratio() -> {
                ((oldSize.ratio() * newSize.height) - newSize.width) / 2
            }
            else -> 0.0
        }.roundToInt()
        val borderX = when {
            oldSize.ratio() <= newSize.ratio() -> {
                (((1 / oldSize.ratio()) * newSize.width) - newSize.height) / 2
            }
            else -> 0.0
        }.roundToInt()

        // Add borders where necessary
        val bordered = Mat(
            Size(
                source.size().width + borderX * 2,
                source.size().height + borderY * 2
            ),
            CV_TYPE
        ).also { dest ->
            Core.copyMakeBorder(
                source.clone(),
                dest,
                borderY, borderY, // Vertical border
                borderX, borderX, // Horizontal border
                Core.BORDER_CONSTANT // Border type
            )
        }

        // Resize to desired size
        return Mat(newSize, CV_TYPE).also { dest ->
            Imgproc.resize(bordered, dest, dest.size())
        }
    }

    // Helper extension property
    protected var Mat.values
        get() = this
        set(value) = value.assignTo(this)


    companion object {
        /** OpenCV data type used */
        val CV_TYPE = CvType.CV_8UC4
    }
}
