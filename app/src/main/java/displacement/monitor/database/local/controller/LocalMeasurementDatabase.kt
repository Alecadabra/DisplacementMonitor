package displacement.monitor.database.local.controller

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import displacement.monitor.database.model.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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