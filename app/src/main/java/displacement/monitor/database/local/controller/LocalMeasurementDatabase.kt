package displacement.monitor.database.local.controller

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import displacement.monitor.database.model.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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
 * [RoomDatabase] that stores calculated [Measurement] instances that are awaiting being sent to
 * the remote database. Insert into the database when a measurement is taken and delete the
 * measurement when it has been successfully sent to the remote database.
 */
@Database(entities = [Measurement::class], version = 3)
abstract class LocalMeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        private var instance: LocalMeasurementDatabase? = null

        /**
         * Gets the instance of this database.
         * @param lazyApplicationContext Way to retrieve the application [Context] if required for
         * initialisation
         * @return The [LocalMeasurementDatabase] instance to perform operations on
         */
        operator fun invoke(lazyApplicationContext: () -> Context) = instance ?: runBlocking(
            Dispatchers.IO) {
            val localInstance = Room.databaseBuilder(
                lazyApplicationContext(),
                LocalMeasurementDatabase::class.java,
                "LocalMeasurementDatabase"
            ).also { builder ->
                builder.fallbackToDestructiveMigration()
            }.build()
            instance = localInstance
            return@runBlocking localInstance
        }
    }
}