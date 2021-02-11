package displacement.monitor.scheduling.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

class DistanceAggregator(
    val size: Int,
    private val doneCallback: suspend (Double) -> Unit
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private val array: DoubleArray = DoubleArray(size)

    private var idx = 0

    private val mutex = Mutex()

    private var done: Boolean = false

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

    private fun aggregate(): Double = this.array.also { it.sort() }[this.size / 2]
}