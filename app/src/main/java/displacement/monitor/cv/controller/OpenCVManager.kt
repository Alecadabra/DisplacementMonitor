package displacement.monitor.cv.controller

import android.content.Context
import android.util.Log
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType

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