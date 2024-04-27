package com.github.sky130.suiteki.pro.logic.ble

import kotlinx.coroutines.flow.Flow

typealias UUIDS = Pair<String, String>

abstract class AbstractBleDevice(
    val name: String,
    val mac: String,
    val key: String,
    val support: BleSupport
) {

    abstract val ble: AbstractBleService
    abstract val auth: AbstractAuthService
    abstract val version: Flow<String>
    abstract val battery: Flow<String>
    abstract fun onHandle(bytes: ByteArray, uuid: UUIDS)
    abstract fun onNotifyFailure(uuid: UUIDS)
    abstract val notifyUUIDs: List<UUIDS>
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SuitekiDevice(val pattern: String, val mtu: Int = 512, val splitByte: Int = -1)

abstract class AbstractAuthService {

    abstract val status: Flow<AuthStatus>

    abstract fun startAuth(key: String)

    abstract fun write(bytes: ByteArray, uuid: UUIDS)

    abstract fun onHandle(bytes: ByteArray, uuid: UUIDS)

}

abstract class AbstractBleService {

    abstract fun installFile(
        bytes: ByteArray,
        onProgress: (Int) -> Unit,
        onAuthSuccess: () -> Unit,
        onAuthFailure: () -> Unit
    )

    abstract fun write(bytes: ByteArray, uuid: UUIDS)

    abstract fun onHandle(bytes: ByteArray, uuid: UUIDS)

}

enum class FileType {
    Watchface, App, Firmware
}

enum class AuthStatus {
    Success, Failure, Authing
}