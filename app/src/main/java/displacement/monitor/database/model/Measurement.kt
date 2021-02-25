package displacement.monitor.database.model

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import androidx.room.ColumnInfo as RoomColumnInfo
import androidx.room.Entity as RoomEntity
import androidx.room.PrimaryKey as RoomPrimaryKey

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
