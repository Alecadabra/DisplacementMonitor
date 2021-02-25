package displacement.monitor.database.local.controller

import androidx.room.*
import displacement.monitor.database.model.Measurement

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
 * [Dao] to access the [Measurements][Measurement] from the [LocalMeasurementDatabase].
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