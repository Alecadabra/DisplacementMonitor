package displacement.monitor.cv.controller

import displacement.monitor.cv.model.Contour
import displacement.monitor.settings.model.Settings

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
 * Contains the mathematical logic used to measure the distance to a target contour.
 */
class TargetMeasurement(private val settings: Settings) {

    // Public entry points -------------------------------------------------------------------------

    /**
     * Calculates the distance between the camera and the target. Uses [distanceReal] with
     * values pulled from this class's [Settings] instance.
     * @param target Target [Contour] to measure distance to
     * @return Computed distance (Real)
     */
    fun measureDistance(target: Contour): Double = distanceReal(
        focalLengthReal = this.settings.calibration.focalLength,
        lengthReal = this.settings.calibration.targetSize,
        lengthPx = target.edgeLength
    )

    companion object {

        // Measurement logic -----------------------------------------------------------------------

        /**
         * Calculates the focal length of the camera using triangle similarity.
         * @param distanceReal Distance between the camera and target (Real)
         * @param lengthReal Length of the target (Real)
         * @param lengthPx Length of the target (Perceived)
         * @return Computed focal length (Real)
         */
        fun focalLengthReal(distanceReal: Double, lengthReal: Double, lengthPx: Double): Double {
            return (lengthPx * distanceReal) / lengthReal
        }

        /**
         * Calculates the distance between the camera and target using triangle similarity.
         * @param focalLengthReal Focal length of camera (Real)
         * @param lengthReal Length of the target (Real)
         * @param lengthPx Length of the target (Perceived)
         * @return Calculated distance (Real)
         */
        fun distanceReal(focalLengthReal: Double, lengthReal: Double, lengthPx: Double): Double {
            return (lengthReal * focalLengthReal) / lengthPx
        }
    }
}