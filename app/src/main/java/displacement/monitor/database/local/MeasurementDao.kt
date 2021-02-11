package displacement.monitor.database.local

import androidx.room.*
import displacement.monitor.database.model.Measurement

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM `measurement`")
    fun getAll(): Array<Measurement>

    @Insert
    fun insert(measurement: Measurement)

    @Delete
    fun delete(vararg measurements: Measurement)

    @Query("DELETE FROM `measurement`")
    fun clear()
}
