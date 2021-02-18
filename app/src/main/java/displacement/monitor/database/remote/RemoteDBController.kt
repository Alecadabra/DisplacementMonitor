package displacement.monitor.database.remote

import android.content.Context
import android.util.Log
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.exceptions.InfluxException
import displacement.monitor.BuildConfig
import displacement.monitor.database.local.MeasurementDatabase
import displacement.monitor.database.model.toPoint
import displacement.monitor.settings.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

/**
 * Handles communication with the InfluxDB remote database.
 */
class RemoteDBController(private val settings: Settings) {

    init {
        check(this.settings.remoteDB.enabled) { "Remote database is not enabled in settings" }
    }

    private val clientOptions = InfluxDBClientOptions.builder().also {
        it.connectionString(this.settings.remoteDB.url)
        it.authenticateToken(this.settings.remoteDB.token.toCharArray())
        it.org("285149cba97a4105")
        it.bucket("4fb58502069a630e")
    }.build()

    /**
     * InfluxDB client, or null if not initialised. To suspend until it is initialised, use
     * [getClient].
     */
    private var nullableClient: InfluxDBClient? = null

    /**
     * Launches a coroutine at construction time to construct the InfluxDB client.
     */
    private val clientInitJob = CoroutineScope(Dispatchers.IO).launch {
        val client = InfluxDBClientFactory.create(clientOptions)
        this@RemoteDBController.nullableClient = client
    }

    /**
     * Gets the InfluxDB client. If not yet initialised, this will suspend until it is initialised.
     */
    private suspend fun getClient(): InfluxDBClient = this.nullableClient ?: run {
        this.clientInitJob.join()
        getClient()
    }

    /**
     * Sends all pending measurements in the local database to the InfluxDB database, blocking
     * until sent or an error occurs.
     * @param lazyContext Way to retrieve the [application context][Context] if required for
     * local database initialisation.
     */
    suspend fun send(lazyContext: () -> Context) {

        // Get all pending measurements from local database
        val measurements = MeasurementDatabase(lazyContext).measurementDao().getAll()

        Log.d(TAG, "Writing ${measurements.size} measurement(s)")

        // Suspend initialising the InfluxDB client
        val client = getClient()

        try {
            withContext(Dispatchers.IO) {
                // Write data, blocking
                val points = measurements.map { it.toPoint() }
                client.writeApiBlocking.writePoints(points)
            }

            Log.i(TAG, "Wrote measurement(s) successfully")

            // Delete measurements from the local database
            val dao = MeasurementDatabase(lazyContext).measurementDao()
            dao.delete(*measurements)

        } catch (e: InfluxException) {
            Log.e(TAG, "Could not write measurement(s)", e)
        }
    }

    /**
     * Closes the connection to the InfluxDB client.
     */
    fun close() {
        this.clientInitJob.cancel(CancellationException("Client is being closed"))
        this.nullableClient?.also { client ->
            CoroutineScope(Dispatchers.IO).launch {
                client.close()
            }
        }
    }

    companion object {
        private const val TAG = "RemoteDBController"
    }
}
