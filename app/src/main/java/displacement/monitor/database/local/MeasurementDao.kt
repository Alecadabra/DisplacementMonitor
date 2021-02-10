package displacement.monitor.database.local

import androidx.room.*

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM `measurement`")
    fun getAll(): List<Measurement>

    @Insert
    fun insert(measurement: Measurement)

    @Query("DELETE FROM `measurement`")
    fun clear()
}