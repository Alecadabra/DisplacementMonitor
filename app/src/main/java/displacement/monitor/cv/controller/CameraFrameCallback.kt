package displacement.monitor.cv.controller

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

/**
 * Shortcut class for the OpenCV [CameraBridgeViewBase.CvCameraViewListener2] to simplify
 * implementation code.
 */
class CameraFrameCallback(
    /**
     * Called when a new frame is received from the camera. This will be called in multiple threads,
     * and not in the UI thread.
     */
    val onFrame: (Mat) -> Mat
) : CameraBridgeViewBase.CvCameraViewListener2 {

    // Shortcut implementation ---------------------------------------------------------------------

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        return this.onFrame(inputFrame.rgba())
    }

    // Unused overrides ----------------------------------------------------------------------------

    override fun onCameraViewStarted(width: Int, height: Int) = Unit

    override fun onCameraViewStopped() = Unit
}