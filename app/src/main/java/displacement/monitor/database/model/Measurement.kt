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
)

fun Measurement.toPoint() = Point("measurement").also {
    it.addField("distance", this.distance)
    it.time(this.time, WritePrecision.S)
}
