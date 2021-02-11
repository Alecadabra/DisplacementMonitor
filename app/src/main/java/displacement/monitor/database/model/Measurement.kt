package displacement.monitor.database.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import androidx.room.ColumnInfo as RoomColumnInfo
import androidx.room.Entity as RoomEntity
import androidx.room.PrimaryKey as RoomPrimaryKey

@RoomEntity
data class Measurement(
    /** Unix timestamp when measurement taken (Seconds). */
    @RoomPrimaryKey
    val time: Long,

    /** Measured distance (Metres) */
    @RoomColumnInfo(name = "distance")
    val distance: Double,

    @RoomColumnInfo(name = "failedAttempts")
    val failedAttempts: Int,
)

fun Measurement.toPoint() = Point("measurement").also {
    it.time(this.time, WritePrecision.S)
    it.addField("distance", this.distance)
    it.addField("failedAttempts", this.failedAttempts)
}
