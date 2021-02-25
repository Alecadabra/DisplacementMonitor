package displacement.monitor.cv.controller

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import displacement.monitor.settings.model.Settings
import org.opencv.android.JavaCameraView
import java.lang.Exception
import java.util.*

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
 * Subclass of [JavaCameraView] that provides extra functionality like flash.
 */
class CustomCameraView(
    context: Context,
    attributeSet: AttributeSet,
) : JavaCameraView(context, attributeSet) {

    // Members -------------------------------------------------------------------------------------

    var flashMode = FlashMode.AUTO
        set(value) {
            if (field != value) {
                @Suppress("DEPRECATION")
                this.mCamera.parameters = this.mCamera.parameters.also {
                    it.flashMode = value.parameterName
                }
                Log.i(TAG, "Changed flash mode to ${value.name.toLowerCase(Locale.ROOT)}")
                field = value
            }
        }

    // Overrides -----------------------------------------------------------------------------------

    override fun initializeCamera(width: Int, height: Int): Boolean {
        val result = super.initializeCamera(width, height)
        this.flashMode = FlashMode.AUTO
        return result
    }

    // Public functions ----------------------------------------------------------------------------

    fun start(settings: Settings, callback: CvCameraViewListener2) {
        try {
            this.mCameraIndex = settings.camera.camIdx
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

    // Local constructs ----------------------------------------------------------------------------

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