package displacement.monitor.cv.android.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import displacement.monitor.settings.model.Settings
import org.opencv.android.JavaCameraView
import java.lang.Exception
import java.util.*

class CustomCameraView(
    context: Context,
    attributeSet: AttributeSet,
) : JavaCameraView(context, attributeSet) {

    var cameraIdx: Int by this::mCameraIndex

    @Suppress("DEPRECATION")
    var flashMode = FlashMode.AUTO
        set(value) {
            if (field != value) {
                this.mCamera.parameters = this.mCamera.parameters.also {
                    it.flashMode = value.parameterName
                }
                Log.i(TAG, "Changed flash mode to ${value.name.toLowerCase(Locale.ROOT)}")
                field = value
            }
        }

    override fun initializeCamera(width: Int, height: Int): Boolean {
        val result = super.initializeCamera(width, height)
        this.flashMode = FlashMode.AUTO
        return result
    }

    fun start(settings: Settings, callback: CvCameraViewListener2) {
        try {
            cameraIdx = settings.camera.camIdx
        } catch (e: Exception) {
            Log.e(TAG, "Could not set camera idx", e)
        }
        setCvCameraViewListener(callback)
        enableView()
        this.visibility = VISIBLE
    }

    fun stop() {
        if (this.mCamera != null) {
            this.flashMode = FlashMode.OFF
        }
        disableView()
        this.visibility = GONE
    }

    @Suppress("DEPRECATION")
    enum class FlashMode(val parameterName: String) {
        OFF(Camera.Parameters.FLASH_MODE_OFF),
        ON(Camera.Parameters.FLASH_MODE_TORCH),
        AUTO(Camera.Parameters.FLASH_MODE_AUTO)
    }

    companion object {
        private const val TAG = "CustomCameraView"
    }
}