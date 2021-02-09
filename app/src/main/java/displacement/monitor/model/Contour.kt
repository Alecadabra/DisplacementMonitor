package displacement.monitor.model

import displacement.monitor.controller.cv.ContourOperations
import org.opencv.core.*

/**
 * Wrapper class for different representations of a contour in OpenCV. This class assumes the
 * underlying representation is immutable.
 */
sealed class Contour {

    abstract val matOfPoint: MatOfPoint

    abstract val matOfPoint2f: MatOfPoint2f

    abstract val pointArray: Array<Point>

    val area: Double by lazy { ContourOperations.area(this) }

    val convexHull: Contour by lazy { ContourOperations.convexHull(this) }

    val boundingRect: Rect by lazy { ContourOperations.boundingRect(this) }

    val edgeLength: Double by lazy { ContourOperations.edgeLength(this) }

    fun getApproxCurve(epsilon: Double): Contour {
        if (epsilon !in this.approxCurveMap) {
            this.approxCurveMap[epsilon] = ContourOperations.approxCurve(this, epsilon)
        }
        return this.approxCurveMap.getValue(epsilon)
    }

    private val approxCurveMap = mutableMapOf<Double, Contour>()
}

private class ContourMatOfPoint(override val matOfPoint: MatOfPoint) : Contour() {
    override val matOfPoint2f
        get() = MatOfPoint2f(*this.pointArray)

    override val pointArray: Array<Point>
        get() = this.matOfPoint.toArray()
}

private class ContourMatOfPoint2f(override val matOfPoint2f: MatOfPoint2f) : Contour() {
    override val matOfPoint: MatOfPoint
        get() = MatOfPoint(*this.pointArray)

    override val pointArray: Array<Point>
        get() = this.matOfPoint2f.toArray()
}

private class ContourPointArray(override val pointArray: Array<Point>) : Contour() {
    override val matOfPoint: MatOfPoint
        get() = MatOfPoint(*this.pointArray)

    override val matOfPoint2f: MatOfPoint2f
        get() = MatOfPoint2f(*this.pointArray)
}

fun MatOfPoint.toContour(): Contour = ContourMatOfPoint(this)

fun MatOfPoint2f.toContour(): Contour = ContourMatOfPoint2f(this)

fun Array<Point>.toContour(): Contour = ContourPointArray(this)
