package displacement.monitor.android.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import displacement.monitor.settings.Settings
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