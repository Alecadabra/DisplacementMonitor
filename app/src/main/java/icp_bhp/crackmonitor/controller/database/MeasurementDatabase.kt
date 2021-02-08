package icp_bhp.crackmonitor.controller.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Database(entities = [Measurement::class], version = 1)
abstract class MeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        private var instance: MeasurementDatabase? = null

        operator fun invoke() = this.instance ?: error("Could not get MeasurementDatabase instance")

        operator fun invoke(lazyApplicationContext: () -> Context) = this.instance ?: runBlocking(Dispatchers.IO) {
            val localInstance = Room.databaseBuilder(
                lazyApplicationContext(),
                MeasurementDatabase::class.java,
                "MeasurementDatabase"
            ).build()
            this@Companion.instance = localInstance
            return@runBlocking localInstance
        }
    }
}