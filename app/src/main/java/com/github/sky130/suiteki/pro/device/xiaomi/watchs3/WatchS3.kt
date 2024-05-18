package com.github.sky130.suiteki.pro.device.xiaomi.watchs3

import com.github.sky130.suiteki.pro.logic.ble.Suiteki
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiDevice
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiSppSupport

@Suiteki(pattern = "^Xiaomi Watch S3( eSIM)? [0-9A-F]{4}\$")
class WatchS3(name: String, mac: String, key: String) : XiaomiDevice(
    name, mac, key,
) {
    override val isEncrypted = true
    override val support = XiaomiSppSupport(this)
}