package com.github.sky130.suiteki.pro.logic.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.sky130.suiteki.pro.logic.database.model.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Device)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<Device>)

    @Delete
    suspend fun delete(item: Device)

    @Query("select * from suiteki_device ORDER BY `index`")
    fun getList(): Flow<List<Device>>

    @Query("delete from suiteki_device")
    suspend fun deleteAll()
}