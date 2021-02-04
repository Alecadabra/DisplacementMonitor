package icp_bhp.crackmonitor.controller.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Measurement::class], version = 1)
abstract class MeasurementDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        lateinit var instance: MeasurementDatabase

        fun get(applicationContext: Context) = Room.databaseBuilder(
            applicationContext,
            MeasurementDatabase::class.java,
            "MeasurementDatabase"
        ).build()
    }
}