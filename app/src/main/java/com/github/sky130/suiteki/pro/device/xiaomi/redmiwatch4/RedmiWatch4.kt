package com.github.sky130.suiteki.pro.device.xiaomi.redmiwatch4

import com.github.sky130.suiteki.pro.logic.ble.Suiteki
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiDevice
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiSppSupport

@Suiteki(pattern = "^Redmi Watch 4 [0-9A-F]{4}$")
class RedmiWatch4(name: String, mac: String, key: String) : XiaomiDevice(
    name, mac, key,
) {
    override val isEncrypted = true
    override val support = XiaomiSppSupport(this)
}