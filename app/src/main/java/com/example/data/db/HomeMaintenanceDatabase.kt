package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Asset
import com.example.data.model.MaintenanceLog
import com.example.data.model.MaintenanceSchedule

@Database(
    entities = [
        Asset::class,
        MaintenanceSchedule::class,
        MaintenanceLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HomeMaintenanceDatabase : RoomDatabase() {
    abstract fun dao(): HomeMaintenanceDao

    companion object {
        @Volatile
        private var INSTANCE: HomeMaintenanceDatabase? = null

        fun getInstance(context: Context): HomeMaintenanceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeMaintenanceDatabase::class.java,
                    "home_maintenance.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
