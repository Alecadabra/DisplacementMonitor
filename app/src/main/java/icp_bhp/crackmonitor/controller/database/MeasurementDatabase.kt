package icp_bhp.crackmonitor.controller.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Measurement::class], version = 1)
abstract class MeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        private var instance: MeasurementDatabase? = null

        fun get() = this.instance ?: error("Could not get MeasurementDatabase instance")

        fun get(lazyApplicationContext: () -> Context) = this.instance ?: run {
            val localInstance = Room.databaseBuilder(
                lazyApplicationContext(),
                MeasurementDatabase::class.java,
                "MeasurementDatabase"
            ).build()
            this.instance = localInstance
            return@run localInstance
        }
    }
}