package com.github.sky130.suiteki.pro.logic.ble

import android.util.ArrayMap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.github.sky130.suiteki.pro.MainApplication.Companion.context
import com.github.sky130.suiteki.pro.logic.database.model.Device
import com.github.sky130.suiteki.pro.util.BytesUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

object SuitekiManager {
    private val classMap = ArrayMap<String, Pair<Suiteki, Class<out AbstractSuitekiDevice>>>()
    private val flow = MutableStateFlow<AbstractSuitekiDevice?>(null)
    val bleDevice: StateFlow<AbstractSuitekiDevice?> get() = flow
    val logList = mutableListOf<String>()
    val installStatus = mutableStateOf<InstallStatus>(InstallStatus.Nope)

    fun log(vararg str: Any) {
        logList.add(str.joinToString("\n") {
            if (it is ByteArray) {
                BytesUtils.bytesToHexStr(it).toString()
            } else {
                it.toString()
            }
        })
    }

    fun waitForAuth() = if (bleDevice.value?.status?.value == DeviceStatus.Connected){
        Unit
    }else{
        null
    }

    fun connect(device: Device) {
        for ((i, p) in classMap) {
            if (i.toRegex().matches(device.name)) {
                flow.value = p.second.getConstructor(
                    String::class.java,
                    String::class.java,
                    String::class.java,
                ).newInstance(device.name, device.mac, device.key).apply {
                    onStart()
                }
                break
            }
        }
    }

    fun init() {
        ClassesReader.reader("com.github.sky130.suiteki.pro", context)
            .filter { !it.name.contains("$") }.forEach {
                for (i in it.annotations) {
                    if (i is Suiteki) {
                        Log.d("TAG", i.pattern)
                        classMap[i.pattern] = i to (it as Class<AbstractSuitekiDevice>)
                    }

                }
            }
    }

}