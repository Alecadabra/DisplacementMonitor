package icp_bhp.crackmonitor.controller

import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import org.opencv.android.JavaCamera2View
import org.opencv.android.JavaCameraView

@Suppress("DEPRECATION")
class CameraViewWithFlash(
    context: Context,
    attributeSet: AttributeSet,
) : JavaCameraView(context, attributeSet) {

    fun flashOn() {
        this.mCamera.parameters = this.mCamera.parameters.also {
            it.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        }
    }

    fun flashOff() {
        this.mCamera.parameters = this.mCamera.parameters.also {
            it.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
    }
}

// New API version, never worked due to wrong camera user
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
}
 */