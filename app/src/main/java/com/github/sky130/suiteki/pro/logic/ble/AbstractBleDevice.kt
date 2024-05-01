package com.github.sky130.suiteki.pro.logic.ble

import kotlinx.coroutines.flow.StateFlow

abstract class AbstractSuitekiDevice(
    val name: String,
    val mac: String,
    val key: String,
) {
    abstract val version: StateFlow<String>
    abstract val battery: StateFlow<String>
    abstract val status: StateFlow<DeviceStatus>
    abstract fun onStart()
    abstract fun install(bytes: ByteArray)
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Suiteki(val pattern: String)


enum class DeviceStatus {
    Connected, Disconnect, Authing, Waiting, AuthFailure
}