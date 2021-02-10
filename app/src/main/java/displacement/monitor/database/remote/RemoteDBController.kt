package displacement.monitor.database.remote

import android.util.Log
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import displacement.monitor.BuildConfig
import displacement.monitor.database.local.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class RemoteDBController {

    /*
     * The InfluxDB access token and domain must have write access and be stored in a file called
     * secret.properties in the project root (Same level as gradle.properties) and must contain
     * INFLUX_DB_TOKEN="YOUR TOKEN HERE"
     */

    /**
     * InfluxDB client, or null if not initialised. To suspend until it is initialised, use
     * [getClient].
     */
    private var nullableClient: InfluxDBClient? = null

    /** Launches a coroutine to construct the InfluxDB client */
    private val clientInitJob = CoroutineScope(Dispatchers.IO).launch {
        val client = InfluxDBClientFactory.create(INFLUX_OPTIONS)
        this@RemoteDBController.nullableClient = client
    }

    /**
     * Gets the InfluxDB client. If not yet initialised, this will suspend until it is initialised.
     */
    private suspend fun getClient(): InfluxDBClient = this.nullableClient ?: run {
        this.clientInitJob.join()
        getClient()
    }

    suspend fun writeMeasurement(measurement: Measurement)  {
        Log.d(TAG, "Writing measurement")

        val client = getClient()

        val data = Point("measurement").also {
            it.addField("distance", measurement.distance)
            it.time(measurement.time, WritePrecision.S)
        }

        try {
            withContext(Dispatchers.IO) {
                client.writeApiBlocking.writePoint(data)
            }
            Log.i(TAG, "Wrote measurement successfully")
        } catch (e: InfluxException) {
            Log.e(TAG, "Could not write measurement", e)
        }
    }

    suspend fun close() {
        this.clientInitJob.cancel(CancellationException("Client is being closed"))
        this.nullableClient?.also { client ->
            withContext(Dispatchers.IO) {
                client.close()
            }
        }
    }

    companion object {
        private const val TAG = "RemoteDBController"

        private const val URL = "https://intern-am-db.icentralau.com.au/"

        private val INFLUX_OPTIONS: InfluxDBClientOptions = InfluxDBClientOptions.builder().also {
            it.connectionString(URL)
            it.authenticateToken(BuildConfig.INFLUX_DB_TOKEN.toCharArray())
            it.org("285149cba97a4105")
            it.bucket("4fb58502069a630e")
        }.build()
    }
}