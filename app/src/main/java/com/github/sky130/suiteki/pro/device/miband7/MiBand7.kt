package com.github.sky130.suiteki.pro.device.miband7

import com.github.sky130.suiteki.pro.logic.ble.AbstractAuthService
import com.github.sky130.suiteki.pro.logic.ble.AbstractBleDevice
import com.github.sky130.suiteki.pro.logic.ble.AbstractBleService
import com.github.sky130.suiteki.pro.logic.ble.BleSupport
import com.github.sky130.suiteki.pro.logic.ble.SuitekiDevice
import com.github.sky130.suiteki.pro.logic.ble.UUIDS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@SuitekiDevice(pattern = "^Xiaomi Smart Band 7 [A-Z0-9]{4}$")
class MiBand7(
    name: String, mac: String, key: String, support: BleSupport
) : AbstractBleDevice(
    name, mac, key, support
) {
    override val ble by lazy { HuamiBleService() }
    override val auth by lazy { HuamiAuthService(support) }
    override val version = MutableStateFlow("unknown")
    override val battery = MutableStateFlow("unknown")
    override val notifyUUIDs = listOf(
        HuamiService.UUID_SERVICE_MIBAND_SERVICE to HuamiService.UUID_CHARACTERISTIC_AUTH_NOTIFY,
        HuamiService.UUID_SERVICE_FIRMWARE to HuamiService.UUID_CHARACTERISTIC_FIRMWARE_NOTIFY
    )

    init {
        support.initNotify(this)
    }

    override fun onHandle(bytes: ByteArray, uuid: UUIDS) {
        auth.onHandle(bytes,uuid)
    }

    override fun onNotifyFailure(uuid: UUIDS) {

    }

}