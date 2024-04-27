package com.github.sky130.suiteki.pro.logic.ble

import android.util.ArrayMap
import com.github.sky130.suiteki.pro.MainApplication.Companion.context
import com.github.sky130.suiteki.pro.logic.database.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow

object SuitekiManager {
    private val flow = MutableStateFlow<AbstractBleDevice?>(null)
    val bleDevice: Flow<AbstractBleDevice?> get() = flow
    private val classMap = ArrayMap<String, Pair<SuitekiDevice, Class<out AbstractBleDevice>>>()

    fun connect(device: Device) {
        for ((i, p) in classMap) {
            if (i.toRegex().matches(device.name)) {
                flow.value = p.second.getConstructor(
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    BleSupport::class.java
                ).newInstance(device.name, device.mac, device.key, BleSupport(device.mac))
                break
            }
        }
    }

    fun init() {
        ClassesReader.reader("com.github.sky130.suiteki.pro.device", context)
            .filter { !it.name.contains("$") }.forEach {
                for (i in it.annotations) {
                    if (i is SuitekiDevice) {
                        classMap[i.pattern] = i to (it as Class<AbstractBleDevice>)
                    }
                }

            }
    }

}