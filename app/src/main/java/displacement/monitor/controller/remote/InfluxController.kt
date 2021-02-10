package displacement.monitor.controller.remote

import android.util.Log
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import displacement.monitor.BuildConfig
import displacement.monitor.controller.database.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

class InfluxController {

    /*
     * The InfluxDB access token and domain must have write access and be stored in a file called
     * secret.properties in the project root (Same level as gradle.properties) and must contain
     * INFLUX_DB_TOKEN="YOUR TOKEN HERE"
     */

    private val influxOptions: InfluxDBClientOptions = InfluxDBClientOptions.builder().also {
        it.connectionString(URL)
        it.authenticateToken(BuildConfig.INFLUX_DB_TOKEN.toCharArray())
        it.org("285149cba97a4105")
        it.bucket("4fb58502069a630e")
    }.build()

    private var nullableClient: InfluxDBClient? = null
        set(value) {
            if (value != null) {
                field = value
            }
        }

    private val clientInitJob = CoroutineScope(Dispatchers.IO).launch {
        val client = InfluxDBClientFactory.create(this@InfluxController.influxOptions)
        this@InfluxController.nullableClient = client
    }

    private suspend fun getClient(): InfluxDBClient = this.nullableClient ?: run {
        this.clientInitJob.join()
        getClient()
    }

    suspend fun writeMeasurement(measurement: Measurement)  {
        Log.i(TAG, "Writing measurement")

        val client = getClient()

        val data = Point("measurement").also {
            it.addField("distance", measurement.distance)
            it.time(measurement.time, WritePrecision.S)
        }

        try {
            withContext(Dispatchers.IO) {
                client.writeApiBlocking.writePoint(data)
            }
        } catch (e: InfluxException) {
            Log.e(TAG, "Could not write measurement", e)
        }
    }

    suspend fun close() {
        this.clientInitJob.cancel(CancellationException("Client is being closed"))
        withContext(Dispatchers.IO) {
            this@InfluxController.nullableClient?.close()
        }
    }

    companion object {
        private const val PROTOCOL = "https"
        private const val DOMAIN = "10.0.12.36"
        private const val PORT = "8080"
        private const val URL = "$PROTOCOL://$DOMAIN:$PORT"

        private const val TAG = "InfluxController"
    }
}