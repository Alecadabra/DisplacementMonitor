package displacement.monitor.controller.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measurement(
    /** Unix timestamp when measurement taken (Seconds). */
    @PrimaryKey val time: Long,
    /** Measured distance (Metres) */
    @ColumnInfo(name = "distance") val distance: Double,
)