package displacement.monitor.database.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import androidx.room.ColumnInfo as RoomColumnInfo
import androidx.room.Entity as RoomEntity
import androidx.room.PrimaryKey as RoomPrimaryKey

/**
 * Holds a single distance measurement. Can be used as a [Room Entity][RoomEntity] directly and a
 * [InfluxDB Point][Point] through the [Measurement.toPoint] extension function.
 */
@RoomEntity(primaryKeys = ["time", "id"])
data class Measurement(
    /** Unix timestamp when measurement taken (Seconds). */
    val time: Long,

    /** Measured distance (Metres). */
    val distance: Double,

    /** Unique device ID. */
    val id: String,

    /** Number of camera frames processed unsuccessfully before a measurement could be taken. */
    val failedAttempts: Int,
)

/**
 * Converts a [Measurement] to an [InfluxDB Point][Point]. Names used directly match the property
 * names in [Measurement].
 */
fun Measurement.toPoint() = Point("measurement").also {
    it.time(this.time, WritePrecision.S)
    it.addField("distance", this.distance)
    it.addTag("id", this.id)
    it.addField("failedAttempts", this.failedAttempts)
}
