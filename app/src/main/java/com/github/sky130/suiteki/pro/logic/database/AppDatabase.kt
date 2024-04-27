package com.github.sky130.suiteki.pro.logic.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.sky130.suiteki.pro.MainApplication.Companion.context
import com.github.sky130.suiteki.pro.logic.database.dao.DeviceDAO
import com.github.sky130.suiteki.pro.logic.database.model.Device

@Database(
    version = 11,
    entities = [Device::class],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun device(): DeviceDAO

    companion object {
        val instance by lazy {
            Room.databaseBuilder(
                context, AppDatabase::class.java, "app_database"
            ).apply {
                fallbackToDestructiveMigration()
            }.build()
        }
    }
}