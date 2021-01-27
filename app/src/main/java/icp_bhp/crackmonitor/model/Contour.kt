package icp_bhp.crackmonitor.model

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Wrapper class for different representations of a contour in OpenCV.
 */
sealed class Contour {

    abstract val matOfPoint: MatOfPoint

    abstract val matOfPoint2f: MatOfPoint2f

    abstract val pointArray: Array<Point>

    open val pointList: List<Point>
        get() = pointArray.toList()

    val area: Double by lazy {
        Imgproc.contourArea(this.matOfPoint)
    }

    val convexHull: Contour by lazy {
        // Get MatOfInt from convexHull function
        val hullIntMat = MatOfInt().also { Imgproc.convexHull(this.matOfPoint, it) }
        val hullIndexes = hullIntMat.toList()
        val points = this.pointArray
        val hullPoints = Array(hullIntMat.height()) { i ->
            points[hullIndexes[i]]
        }

        return@lazy hullPoints.toContour()
    }

    val boundingRect: Rect by lazy {
        Imgproc.boundingRect(this.matOfPoint)
    }

    fun getApproxCurve(epsilon: Double): Contour = this.approxCurveMap[epsilon] ?: run {
        // Map epsilon to calculated approx curve
        this.approxCurveMap[epsilon] = MatOfPoint2f().also { mat ->
            val perimeter = Imgproc.arcLength(this.matOfPoint2f, true)
            val fullEpsilon = epsilon * perimeter

            Imgproc.approxPolyDP(this.matOfPoint2f, mat, fullEpsilon, true)
        }.toContour()
        // Get new value
        return@run this.approxCurveMap.getValue(epsilon)
    }

    private val approxCurveMap = mutableMapOf<Double, Contour>()
}

private class ContourMatOfPoint(override val matOfPoint: MatOfPoint) : Contour() {
    override val matOfPoint2f
        get() = MatOfPoint2f(*this.pointArray)

    override val pointArray: Array<Point>
        get() = this.matOfPoint.toArray()

    override fun hashCode() = this.matOfPoint.hashCode()

    override fun equals(other: Any?) = this.matOfPoint == other
}

private class ContourMatOfPoint2f(override val matOfPoint2f: MatOfPoint2f) : Contour() {
    override val matOfPoint: MatOfPoint
        get() = MatOfPoint(*this.pointArray)

    override val pointArray: Array<Point>
        get() = this.matOfPoint2f.toArray()

    override fun hashCode() = this.matOfPoint2f.hashCode()

    override fun equals(other: Any?) = this.matOfPoint2f == other
}

private class ContourPointArray(override val pointArray: Array<Point>) : Contour() {
    override val matOfPoint: MatOfPoint
        get() = MatOfPoint(*this.pointArray)

    override val matOfPoint2f: MatOfPoint2f
        get() = MatOfPoint2f(*this.pointArray)

    override fun hashCode() = this.pointArray.hashCode()

    override fun equals(other: Any?) = this.pointArray == other
}

fun MatOfPoint.toContour(): Contour = ContourMatOfPoint(this)

fun MatOfPoint2f.toContour(): Contour = ContourMatOfPoint2f(this)

fun Array<Point>.toContour(): Contour = ContourPointArray(this)
