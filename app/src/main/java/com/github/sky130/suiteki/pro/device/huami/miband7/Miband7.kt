package com.github.sky130.suiteki.pro.device.huami.miband7

import com.github.sky130.suiteki.pro.logic.ble.Suiteki
import com.github.sky130.suiteki.pro.device.huami.HuamiDevice

@Suiteki(pattern = "^Xiaomi Smart Band 7 [A-Z0-9]{4}$")
class Miband7(name: String, mac: String, key: String) : HuamiDevice(name, mac, key)