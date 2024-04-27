package com.github.sky130.suiteki.pro.device.miband8

import com.github.sky130.suiteki.pro.logic.ble.AbstractAuthService
import com.github.sky130.suiteki.pro.logic.ble.AbstractBleDevice
import com.github.sky130.suiteki.pro.logic.ble.AbstractBleService
import com.github.sky130.suiteki.pro.logic.ble.AuthStatus
import com.github.sky130.suiteki.pro.logic.ble.BleSupport
import com.github.sky130.suiteki.pro.logic.ble.UUIDS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MiBand8(name: String, mac: String, key: String, support: BleSupport) : AbstractBleDevice(
    name, mac, key, support
) {
    override val ble by lazy { XiaomiBleService() }
    override val auth by lazy { XiaomiAuthService(support) }
    override val version = MutableStateFlow("unknown")
    override val battery = MutableStateFlow("unknown")

    override fun onHandle(bytes: ByteArray, uuid: UUIDS) {
        auth.onHandle(bytes, uuid)
    }

    override fun onNotifyFailure(uuid: UUIDS) {

    }

    override val notifyUUIDs = listOf(
        XiaomiService.UUID_SERVICE to XiaomiService.UUID_CHARACTERISTIC_COMMAND_READ
    )
}