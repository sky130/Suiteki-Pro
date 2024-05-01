@file:Suppress("LeakingThis")

package com.github.sky130.suiteki.pro.device.xiaomi

import com.github.sky130.suiteki.pro.logic.ble.AbstractSuitekiDevice
import com.github.sky130.suiteki.pro.logic.ble.DeviceStatus
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow

abstract class XiaomiDevice(name: String, mac: String, key: String) : AbstractSuitekiDevice(
    name, mac, key
) {
    override val version = MutableStateFlow("unknown")
    override val battery = MutableStateFlow("unknown")
    override val status = MutableStateFlow(DeviceStatus.Waiting)
    abstract val isEncrypted: Boolean
    internal val authService = XiaomiAuthService(this)
    abstract val support: XiaomiAbstractSupport
    private val job = Job()
    val scope = CoroutineScope(job)

    open fun handleCommand(command: XiaomiProto.Command) {
        authService.handleCommand(command)
    }

    fun auth() {
        status.value = DeviceStatus.Authing
        if (isEncrypted) {
            authService.startEncryptedHandshake()
        } else {
            authService.startClearTextHandshake()
        }
    }

    open fun onDisconnect() {
        status.value = DeviceStatus.Disconnect
    }

    override fun onStart() {
        support.start()
    }

    override fun install(bytes: ByteArray) {
        TODO("Not yet implemented")
    }
}