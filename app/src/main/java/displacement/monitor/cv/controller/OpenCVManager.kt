package displacement.monitor.cv.controller

import android.content.Context
import android.util.Log
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType

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

/** OpenCV data type used */
val CV_TYPE = CvType.CV_8UC4

/**
 * Handles initialisation of OpenCV. Requires a context.
 * @param context The [Android context][Context] within which to initialise OpenCV.
 */
fun initialiseOpenCV(context: Context) {
    val loaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.d(TAG, "OpenCV loaded successfully")
                }
                else -> super.onManagerConnected(status)
            }
        }
    }

    if (OpenCVLoader.initDebug()) {
        // Successful load
        Log.d(TAG, "OpenCV reloaded successfully")
        loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
    } else {
        // Unsuccessful internal load
        Log.d(TAG, "OpenCV failed to load internally - using OpenCV Manager")
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, context, loaderCallback)
    }
}

private const val TAG = "OpenCVInitialisation"