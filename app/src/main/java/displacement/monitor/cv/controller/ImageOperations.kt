package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

/**
 * Holds commonly used operations performed on OpenCV images - [Mats][Mat]
 */
object ImageOperations {
    /**
     * OpenCV's camera is disoriented by 90 degrees, this fixes it and warps the image if required.
     * @param image Image matrix source, of type [CV_TYPE]
     * @param warp Flag to do an extra warping step to the image.
     * @return The oriented image matrix, of type [CV_TYPE]
     */
    fun fixOrientation(image: Mat, warp: Boolean): Mat {
        fun blankImage() = Mat(image.height(), image.width(), CV_TYPE)
        fun blankImageTransposed() = Mat(image.width(), image.height(), CV_TYPE)

        // Warp correctly if required
        if (warp) {
            val original = image.clone()
            val transposed = blankImageTransposed()
            val flipped = blankImageTransposed()

            Core.transpose(original, transposed)
            Core.flip(transposed, flipped, 1)
            return flipped
        } else {
            val original = image.clone()
            val transposed = blankImageTransposed()
            val resized = blankImage()
            val flipped = blankImage()

            Core.transpose(original, transposed)
            Imgproc.resize(transposed, resized, resized.size(), 0.0, 0.0, 0)
            Core.flip(resized, flipped, 1)
            return flipped
        }
    }

    /**
     * Draws the given target onto the image and returns the result. The target will have a blue
     * outline with green circles in the corners.
     * @param image Image matrix source
     * @param target Target [contour][Contour] to draw
     * @return Image matrix with [target] drawn on
     */
    fun drawTarget(image: Mat, target: Contour): Mat {
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
     * @param source Image matrix to resize
     * @param newSize The [Size] to resize [source] to
     * @return Image matrix resized
     */
    fun resizeWithBorder(source: Mat, newSize: Size): Mat {
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

    /**
     * Approximates the brightness of the image by measuring the brightness dimension of the
     * image centroid converted to the HSV colour space.
     * @param image Image matrix, of type [CV_TYPE]
     * @return Brightness value, between 0 and 1
     */
    fun measureCentroidBrightness(image: Mat): Float {
        val centroidMat = Mat(1, 1, CV_TYPE).also { dest ->
            val middleRow = image.rows() / 2
            val middleCol = image.cols() / 2
            Imgproc.cvtColor(
                image.submat(
                    Range(middleRow, middleRow + 1),
                    Range(middleCol, middleCol + 1)
                ),
                dest,
                Imgproc.COLOR_RGB2HSV
            )
        }
        return (centroidMat[0, 0][2] / 255).toFloat()
    }
}


