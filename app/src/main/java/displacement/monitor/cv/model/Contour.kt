package displacement.monitor.cv.model

import displacement.monitor.cv.controller.ContourOperations
import org.opencv.core.*

/**
 * Wrapper class for different representations of a contour in OpenCV. This class assumes the
 * underlying representation is immutable.
 */
sealed class Contour {

    // Underlying representations ------------------------------------------------------------------

    /** The contour represented as a [MatOfPoint]. */
    abstract val matOfPoint: MatOfPoint

    /** The contour represented as a [MatOfPoint2f]. */
    abstract val matOfPoint2f: MatOfPoint2f

    /** The contour represented as an [Array] of [Points][Point]. */
    abstract val pointArray: Array<Point>

    // Operations computed lazily ------------------------------------------------------------------

    /**
     * Calculates the area of the contour in square pixels.
     * @see [ContourOperations.area].
     */
    val area: Double by lazy { ContourOperations.area(this) }

    /**
     * Calculates the convex hull of a contour - the contour modified to have no concave points.
     * @see [ContourOperations.convexHull]
     */
    val convexHull: Contour by lazy { ContourOperations.convexHull(this) }

    /**
     * Calculates the bounding rectangle of a contour.
     * @see [ContourOperations.boundingRect]
     */
    val boundingRect: Rect by lazy { ContourOperations.boundingRect(this) }

    /**
     * Calculates the perceived dimension of the contour. Assumes the contour is square and it's
     * points are ordered in a consecutive fashion, eg. clockwise.
     * @see [ContourOperations.edgeLength]
     */
    val edgeLength: Double by lazy { ContourOperations.edgeLength(this) }

    /**
     * Calculates the approximate polygonal curve of a contour using the Douglas-Pecker algorithm
     * with the given accuracy epsilon.
     * @see [ContourOperations.approxCurve]
     */
    fun getApproxCurve(epsilon: Double): Contour {
        if (epsilon !in this.approxCurveMap) {
            this.approxCurveMap[epsilon] = ContourOperations.approxCurve(this, epsilon)
        }
        return this.approxCurveMap.getValue(epsilon)
    }

    // Maps values of approx curve epsilons with the calculated approx curve for that epsilon
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

/** Wraps this contour representation in a [Contour] object. */
fun MatOfPoint.toContour(): Contour = ContourMatOfPoint(this)

/** Wraps this contour representation in a [Contour] object. */
fun MatOfPoint2f.toContour(): Contour = ContourMatOfPoint2f(this)

/** Wraps this contour representation in a [Contour] object. */
fun Array<Point>.toContour(): Contour = ContourPointArray(this)
