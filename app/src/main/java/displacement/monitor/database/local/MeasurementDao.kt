package displacement.monitor.database.local

import androidx.room.*
import displacement.monitor.database.model.Measurement

/**
 * [Dao] to access the [Measurements][Measurement] from the [MeasurementDatabase].
 */
@Dao
interface MeasurementDao {
    /**
     * Gets all measurements from the database.
     */
    @Query("SELECT * FROM `measurement`")
    fun getAll(): Array<Measurement>

    /**
     * Inserts a single measurement into the database.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(measurement: Measurement)

    /**
     * Deletes all given measurements from the database.
     */
    @Delete
    fun delete(vararg measurements: Measurement)

    /**
     * Deletes *all* measurements from the database.
     */
    @Query("DELETE FROM `measurement`")
    fun clear()
}
