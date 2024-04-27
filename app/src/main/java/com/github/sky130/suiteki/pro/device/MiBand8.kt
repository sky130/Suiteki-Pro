package com.github.sky130.suiteki.pro.device

import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiAuthService
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiBleService
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService
import com.github.sky130.suiteki.pro.logic.ble.AbstractBleDevice
import com.github.sky130.suiteki.pro.logic.ble.BleSupport
import com.github.sky130.suiteki.pro.logic.ble.SuitekiDevice
import com.github.sky130.suiteki.pro.logic.ble.UUIDS
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("LeakingThis")
@SuitekiDevice(pattern = "^Xiaomi Smart Band 8 [A-Z0-9]{4}$")
open class MiBand8(name: String, mac: String, key: String, support: BleSupport) : AbstractBleDevice(
    name, mac, key, support
) {

    init {
        support.initNotify(this)
    }

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