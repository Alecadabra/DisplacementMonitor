package displacement.monitor.database.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import displacement.monitor.database.model.Measurement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@Database(entities = [Measurement::class], version = 3)
abstract class MeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        private var instance: MeasurementDatabase? = null

        operator fun invoke() = instance ?: error("Could not get MeasurementDatabase instance")

        operator fun invoke(lazyApplicationContext: () -> Context) = instance ?: runBlocking(Dispatchers.IO) {
            val localInstance = Room.databaseBuilder(
                lazyApplicationContext(),
                MeasurementDatabase::class.java,
                "MeasurementDatabase"
            ).also { builder ->
                builder.fallbackToDestructiveMigration()
            }.build()
            instance = localInstance
            return@runBlocking localInstance
        }
    }
}
