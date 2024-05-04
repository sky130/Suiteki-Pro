@file:Suppress("LeakingThis")

package com.github.sky130.suiteki.pro.device.huami

import com.github.sky130.suiteki.pro.logic.ble.AbstractSuitekiDevice
import com.github.sky130.suiteki.pro.logic.ble.DeviceStatus
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.util.BytesUtils
import kotlinx.coroutines.flow.MutableStateFlow

open class HuamiDevice(name: String, mac: String, key: String) : AbstractSuitekiDevice(
    name, mac, key
) {
    override val version = MutableStateFlow("unknown")
    override val battery = MutableStateFlow("unknown")
    override val status = MutableStateFlow(DeviceStatus.Waiting)
    private val authService = HuamiAuthService(this)
    private val appListHelper = HuamiAppListHelper(this)
    private val bleSupport = HuamiBleSupport(this)
    private var installHelper: HuamiInstallHelper? = null
    override val appList get() = appListHelper.list
    val uuids = mapOf(
        HuamiService.UUID_SERVICE_MIBAND_SERVICE to listOf(
            HuamiService.UUID_CHARACTERISTIC_AUTH_NOTIFY,
            HuamiService.UUID_CHARACTERISTIC_APP
        ),
        HuamiService.UUID_SERVICE_FIRMWARE to listOf(HuamiService.UUID_CHARACTERISTIC_FIRMWARE_NOTIFY)
    )

    override fun onStart() {
        bleSupport.start()
    }

    fun auth() {
        authService.startAuth()
    }

    fun onAuth() {
        appListHelper.requestAppItems()
    }

    fun handleChannel(data: ByteArray, service: String, characteristics: String) {
        authService.handleData(data, service, characteristics)
        if (service == HuamiService.UUID_SERVICE_FIRMWARE && characteristics == HuamiService.UUID_CHARACTERISTIC_FIRMWARE_NOTIFY) {
            installHelper?.handleBytes(data)
        }
    }

    fun handlePayload(data: ByteArray) {
        appListHelper.handlePayload(data)
    }

    open fun onDisconnect() {

    }

    override fun install(bytes: ByteArray) {
        installHelper = HuamiInstallHelper(this, bytes)
        installHelper?.start()
    }

    override fun deleteApp(id: String) {
        appListHelper.uninstall(id)
    }

    override fun requestAppList() {
        appListHelper.requestAppItems()
    }

    override fun launchApp(id: String) {
        appListHelper.launch(id)
    }

    fun installFinish() {
        installHelper = null
    }

    fun write(data: ByteArray, service: String, characteristics: String) {
        SuitekiManager.log("onWrite",service,characteristics,BytesUtils.bytesToHexStr(data).toString())
        bleSupport.write(data, service, characteristics)
    }

    fun write(
        type: Short, data: ByteArray, extendedFlags: Boolean, encrypt: Boolean
    ) = authService.huamiWrite(type, data, extendedFlags, encrypt)


}