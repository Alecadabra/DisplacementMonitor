package icp_bhp.crackmonitor.controller

import com.google.android.material.snackbar.Snackbar
import icp_bhp.crackmonitor.model.Contour
import icp_bhp.crackmonitor.model.Settings
import kotlinx.coroutines.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*

class ImageProcessor(
    /** Measured initial distance between the camera and target being measured in metres */
    private val initialDistanceM: Double,
    /** Callback to measurement listener */
    private val measurementCallback: MeasurementCallback,
    /**
     * Manager of [android.widget.Toast]s to send messages to, or null if silent.
     */
    private val toaster: Toaster?,
    /**
     * Settings reference. If settings change this [ImageProcessor] should be reconstructed.
     */
    private val settings: Settings,
) {

    // Members -------------------------------------------------------------------------------------

    /** Measured focal length */
    private var focalLength: Double? = null

    /** UI controller flag */
    private var errorShown = false

    private val targetFinder by lazy {
        TargetFinder(this.settings)
    }

    // Public entry points -------------------------------------------------------------------------

    fun processAndReturnPreview(input: Mat): Mat {
        // Run rgba image matrix through processing
        val processed = processImage(input)

        // Resize to original size without distortion
        return resizeWithBorder(processed, input.size())
    }

    // Local image processing ----------------------------------------------------------------------

    private fun resizeWithBorder(source: Mat, newSize: Size): Mat {
        val oldSize = source.size()

        // Helper extension function
        fun Size.ratio() = this.width / this.height

        // Determine borders to add
        val borderY = when {
            oldSize.ratio() >= newSize.ratio() -> {
                ((oldSize.ratio() * newSize.height) - newSize.width) / 2
            }
            else -> 0.0
        }.toInt()
        val borderX = when {
            oldSize.ratio() <= newSize.ratio() -> {
                (((1 / oldSize.ratio()) * newSize.width) - newSize.height) / 2
            }
            else -> 0.0
        }.toInt()

        // Add borders where necessary
        val bordered = Mat(
            Size(
                source.size().width + borderX * 2,
                source.size().height + borderY * 2
            ),
            TYPE
        ).also { dest ->
            Core.copyMakeBorder(
                source,
                dest,
                borderY, borderY, // Vertical border
                borderX, borderX, // Horizontal border
                Core.BORDER_CONSTANT // Border type
            )
        }

        // Resize to desired size
        return Mat(newSize, TYPE).also { dest ->
            Imgproc.resize(bordered, dest, dest.size())
        }
    }

    private fun processImage(image: Mat): Mat {
        // Orient image properly
        val img: Mat = fixOrientation(image)

        try {
            // Find possible target
            val imgEdges: Mat = findEdges(img)
            val targetContour: Contour = this.targetFinder.findTarget(imgEdges)

            // Measure target
            val targetSizePx = getPerceivedMeasurement(targetContour)

            // Calculate focal length if needed
            val localFocalLength = this.focalLength ?: run {
                // Set focal length
                return@run computeFocalLength(
                    this.initialDistanceM,
                    this.settings.target.targetSize,
                    targetSizePx
                ).also {
                    // Update member
                    this.focalLength = it
                }
            }

            // Compute distance to rectangle in metres
            val distance = computeDistance(
                localFocalLength,
                this.settings.target.targetSize,
                targetSizePx
            )

            // Draw target outline
            Imgproc.drawContours(
                img,
                listOf(targetContour.matOfPoint),
                0,
                Scalar(0.0, 125.0, 255.0),
                (targetSizePx.toInt() / 32).coerceAtLeast(5)
            )

            // Draw 4 corners of rectangle
            targetContour.pointArray.also { pointArray ->
                val size = (targetSizePx.toInt() / 16).coerceAtLeast(10)
                pointArray.forEach { point ->
                    Imgproc.circle(img, point, size, Scalar(0.0, 255.0, 127.0), Core.FILLED)
                }
            }

            // Use UI thread to communicate with the activity
            CoroutineScope(Dispatchers.Main).launch {
                this@ImageProcessor.also { imageProcessor ->
                    // Notify observers
                    imageProcessor.measurementCallback.onUpdateMeasurement(distance, localFocalLength)

                    // Hide error
                    imageProcessor.toaster?.also { toaster ->
                        if (imageProcessor.errorShown) {
                            toaster.hideMessage()
                            imageProcessor.errorShown = false
                        }
                    }
                }
            }

        } catch (e: Exception) {
            val message = e.message ?: "An error occurred"
            // Show error in UI thread
            CoroutineScope(Dispatchers.Main).launch {
                this@ImageProcessor.toaster?.also { toaster ->
                    toaster.showMessage(message, e)
                    this@ImageProcessor.errorShown = true
                }
            }
        }

        return img
    }

    /**
     * OpenCV's camera is disoriented by 90 degrees, this fixes it and warps the image if required.
     */
    private fun fixOrientation(img: Mat): Mat {
        fun portraitMat() = Mat(img.height(), img.width(), TYPE)
        fun landscapeMat() = Mat(img.width(), img.height(), TYPE)

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

    /**
     * Applies a find edges effect to the image
     */
    private fun findEdges(img: Mat): Mat {
        val edges = Mat(img.size(), TYPE)
        val (cannyThreshold1, cannyThreshold2) = this.settings.targetFinding.let {
            it.cannyThreshold1 to it.cannyThreshold2
        }

        // Make greyscale
        Imgproc.cvtColor(img, edges, Imgproc.COLOR_BGR2GRAY)
        // Slight gaussian blur
        Imgproc.GaussianBlur(edges, edges, this.settings.targetFinding.blurSize, 0.0)
        // Find edges
        Imgproc.Canny(edges, edges, cannyThreshold1, cannyThreshold2)

        return edges
    }

    /**
     * Calculates the focal length of the camera using triangle similarity.
     * @param distanceM Distance between the camera and object in metres
     * @param widthM Measured width of the object in metres
     * @param widthPix Perceived width of object in pixels
     * @return Focal length in metres
     */
    private fun computeFocalLength(distanceM: Double, widthM: Double, widthPix: Double): Double {
        return (widthPix * distanceM) / widthM
    }

    /**
     * Calculates the distance to the camera using triangle similarity.
     * @param focalLengthM Focal length of camera in metres
     * @param widthM Real width of object in metres
     * @param widthPix Perceived width of object in pixels
     * @return Calculated distance in metres
     */
    private fun computeDistance(focalLengthM: Double, widthM: Double, widthPix: Double): Double {
        return (widthM * focalLengthM) / widthPix
    }

    /**
     * Calculates the perceived dimension of the presumed square contour.
     * @param targetContour The [Contour] to measure
     * @return Perceived dimension of the square target in pixels
     */
    private fun getPerceivedMeasurement(targetContour: Contour): Double {
        val pointArray = targetContour.pointArray

        // Join points to edges cyclically
        val edges = pointArray.mapIndexed { i, point ->
            if (i == pointArray.lastIndex) {
                // Join last to first
                point to pointArray[0]
            } else {
                // Join current to next
                point to pointArray[i + 1]
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

    companion object {
        /** OpenCV data type used */
        private val TYPE = CvType.CV_8UC4
    }
}

fun interface MeasurementCallback {
    fun onUpdateMeasurement(distance: Double, focalLength: Double)
}

/**
 * Manages error message [android.widget.Toast]s.
 */
interface Toaster {
    fun showMessage(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT
    )

    fun showMessage(
        message: String,
        exception: Exception,
        duration: Int = Snackbar.LENGTH_SHORT
    )

    fun hideMessage()
}