package com.github.sky130.suiteki.pro.logic.database.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "suiteki_device", indices = [Index(value = ["mac"], unique = true)])
@Keep
data class Device(val name: String, val mac: String, val key: String){
    @PrimaryKey(autoGenerate = true)
    var index: Int = 0
}