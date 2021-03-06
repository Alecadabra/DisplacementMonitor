package displacement.monitor.scheduling.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

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
 * Takes in multiple distance values and activates the callback when a final value is reached.
 * Works by taking in [size] values and finding the median.
 * Uses coroutines and wraps a [DoubleArray] for the data and efficient sorting.
 */
class DistanceAggregator(
    /** Number of distance measurements to take in */
    val size: Int,
    /** Callback for when a final distance value has been reached. */
    private val doneCallback: suspend (Double) -> Unit
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    /** Stores distance values */
    private val array: DoubleArray = DoubleArray(size)

    /** Next index of [array] to insert into. */
    private var idx = 0

    /** Mutex to control access to [array] & [idx]. */
    private val mutex = Mutex()

    /** Flag for if a final value has been reached. */
    private var done: Boolean = false

    /**
     * Adds a new distance measurement.
     */
    fun addDistance(distance: Double) {
        if (!this.done) {
            launch {
                this@DistanceAggregator.mutex.withLock {
                    with(this@DistanceAggregator) {
                        if (this.idx >= size) {
                            launch(Dispatchers.Main) {
                                this@DistanceAggregator.doneCallback(aggregate())
                            }
                            this.done = true
                        } else {
                            this.array[idx] = distance
                            this.idx++
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a final aggregated distance value by taking the median of the data set.
     */
    private fun aggregate(): Double = this.array.also { it.sort() }[this.size / 2]
}