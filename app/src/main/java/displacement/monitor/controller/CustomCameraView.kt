package displacement.monitor.controller

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import displacement.monitor.model.Settings
import org.opencv.android.JavaCameraView
import java.lang.RuntimeException

class CustomCameraView(
    context: Context,
    attributeSet: AttributeSet,
) : JavaCameraView(context, attributeSet) {

    var cameraIdx: Int
        get() = this.mCameraIndex
        set(value) { this.mCameraIndex = value }

    fun start(settings: Settings, callback: CvCameraViewListener2) {
        cameraIdx = settings.camera.camIdx
        setCvCameraViewListener(callback)
        enableView()
        this.visibility = VISIBLE
    }

    fun stop() {
        flashOff()
        disableView()
        this.visibility = GONE
    }

    fun flashOn() {
        try {
            @Suppress("DEPRECATION")
            this.mCamera.parameters = this.mCamera.parameters.also {
                it.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Could not turn on flash (${e.message})")
        }
    }

    fun flashOff() {
        try {
            @Suppress("DEPRECATION")
            this.mCamera.parameters = this.mCamera.parameters.also {
                it.flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Could not turn off flash (${e.message})")
        }
    }

    companion object {
        private const val TAG = "CustomCameraView"
    }
}

// New API version, never worked due to wrong camera user, can't be fixed without re-implementing
// JavaCamera2View to expose private fields
/*
@RequiresApi(Build.VERSION_CODES.M)
class CustomCameraView2(
    context: Context,
    attributeSet: AttributeSet
) : JavaCamera2View(context, attributeSet) {

    private val cameraManager: CameraManager = this.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val cameraId: String
        get() {
            val idx = this.mCameraIndex.coerceAtLeast(0)
            return cameraManager.cameraIdList[idx]
        }

    fun flashOn() {
        try {
            cameraManager.setTorchMode(this.cameraId, true)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Could not turn on flash (Camera Access Exception)", e)
        }
    }

    fun flashOff() {
        try {
            cameraManager.setTorchMode(this.cameraId, false)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Could not turn off flash (Camera Access Exception)", e)
        }
    }

    companion object {
        private const val TAG = "FlashController"
    }
}*/
