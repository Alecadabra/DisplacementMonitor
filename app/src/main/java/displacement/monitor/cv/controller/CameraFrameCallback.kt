package displacement.monitor.cv.controller

import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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