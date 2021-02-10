package displacement.monitor.database.local

import androidx.room.*
import displacement.monitor.database.model.Measurement

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM `measurement`")
    fun getAll(): List<Measurement>

    @Insert
    fun insert(measurement: Measurement)

    @Query("DELETE FROM `measurement`")
    fun clear()
}