package com.github.sky130.suiteki.pro.logic.ble

import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.StateFlow

abstract class AbstractSuitekiDevice(
    val name: String,
    val mac: String,
    val key: String,
) {
    abstract val version: StateFlow<String>
    abstract val battery: StateFlow<String>
    abstract val status: StateFlow<DeviceStatus>
    abstract val appList: SnapshotStateList<AppInfo>
    abstract fun onStart()
    abstract fun install(bytes: ByteArray)
    open fun deleteApp(id: String){}
    open fun launchApp(id:String){}
    open fun requestAppList(){}
}

data class AppInfo(val id: String,val name: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Suiteki(val pattern: String)


enum class DeviceStatus {
    Connected, Disconnect, Authing, Waiting, AuthFailure
}