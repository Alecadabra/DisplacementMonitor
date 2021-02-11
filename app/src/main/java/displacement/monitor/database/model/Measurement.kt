package displacement.monitor.database.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import androidx.room.ColumnInfo as RoomColumnInfo
import androidx.room.Entity as RoomEntity
import androidx.room.PrimaryKey as RoomPrimaryKey

@RoomEntity(primaryKeys = ["time", "id"])
data class Measurement(
    /** Unix timestamp when measurement taken (Seconds). */
    val time: Long,

    /** Measured distance (Metres) */
    val distance: Double,

    /** Device ID */
    val id: String,

    /** Number of failed attempts before this measurement */
    val failedAttempts: Int,
)

fun Measurement.toPoint() = Point("measurement").also {
    it.time(this.time, WritePrecision.S)
    it.addField("distance", this.distance)
    it.addTag("id", this.id)
    it.addField("failedAttempts", this.failedAttempts)
}
