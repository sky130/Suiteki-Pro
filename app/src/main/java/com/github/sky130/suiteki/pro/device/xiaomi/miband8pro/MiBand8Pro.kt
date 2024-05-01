package com.github.sky130.suiteki.pro.device.xiaomi.miband8pro

import com.github.sky130.suiteki.pro.logic.ble.Suiteki
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiDevice
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiSppSupport

@Suiteki(pattern = "^Xiaomi Smart Band 8 Pro [0-9A-F]{4}$")
class MiBand8Pro(name: String, mac: String, key: String) : XiaomiDevice(
    name, mac, key,
) {
    override val isEncrypted = true
    override val support = XiaomiSppSupport(this)
}