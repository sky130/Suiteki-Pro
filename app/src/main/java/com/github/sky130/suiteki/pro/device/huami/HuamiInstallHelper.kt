package com.github.sky130.suiteki.pro.device.huami

import android.util.Log
import com.clj.fastble.BleManager
import com.github.sky130.suiteki.pro.logic.ble.InstallStatus
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.util.BytesUtils

class HuamiInstallHelper(val device: HuamiDevice, val bytes: ByteArray) {
    companion object {
        val d0 = byteArrayOf(-48)
        val d1 = byteArrayOf(-47)
        val d3 = byteArrayOf(-45, 1)
        val d5 = byteArrayOf(-43)
        val d6 = byteArrayOf(-42)
    }

    lateinit var d2: ByteArray
    private lateinit var splitBytes: ArrayList<ByteArray>
    private val performList by lazy { listOf(d0, d1, d2, d3, d5, d6) }


    private var performCount = 0
    private var installCount = 0
    private val progress get() = ((installCount.toFloat() / splitBytes.size.toFloat()) * 100).toInt()

    fun start() {
        SuitekiManager.installStatus.value = InstallStatus.Nope
        d2 = BytesUtils.getD2(bytes, HuamiService.TYPE_D2_WATCHFACE)
        splitBytes = BytesUtils.splitBytes(bytes)
        doPerform()
    }

    private fun doPerform() {
        write(performList[performCount])
        performCount++
    }

    private fun install() {
        BleManager.getInstance().splitWriteNum = 244
        write(splitBytes[installCount], notify = false)
        installCount++
        updateProgress()
    }

    private fun write(data: ByteArray, notify: Boolean = true) {
        device.write(
            data,
            HuamiService.UUID_SERVICE_FIRMWARE,
            if (notify) HuamiService.UUID_CHARACTERISTIC_FIRMWARE_NOTIFY
            else HuamiService.UUID_CHARACTERISTIC_FIRMWARE_WRITE
        )
        Log.d("TAG", "< ${BytesUtils.bytesToHexStr(data).toString()}")
    }

    private fun updateProgress() {
        SuitekiManager.installStatus.value = InstallStatus.Installing(progress)
    }

    private fun installFailure(messages: String) {
        SuitekiManager.installStatus.value = InstallStatus.InstallFailure(progress, messages)
    }

    private fun installSuccess() {
        SuitekiManager.installStatus.value = InstallStatus.InstallSuccess(progress)
    }

    fun handleBytes(bytes: ByteArray) {
        Log.d("TAG", "> ${BytesUtils.bytesToHexStr(bytes).toString()}")
        when (BytesUtils.bytesToHexStr(bytes)) {
            "10D001050020", "10D00105002001", "10D10100", "10D2010000000000000000" -> doPerform()
            "10D203" -> {
                installFailure("验证失败")
                return
            }

            "10D347" -> {
                installFailure("空间不足")
                //空间不足
                return
            }

            "10D656" -> {
                installFailure("不支持的文件")
                //空间不足
                return
            }

            "10D301" -> {
                install()
                return
            }

            "10D501" -> {
                doPerform()
                return
            }

            "10D601" -> {
                installSuccess()
                device.installFinish()
                //安装完毕
                return
            }

            else -> {
                bytes.let {
                    if (it[0].toInt() == 16 && it[1].toInt() == -44) { //10D4
                        return if (installCount >= splitBytes.size) doPerform() else install()

                    }
                    if (it[0].toInt() == 16 && it[1].toInt() == -46 && it[2].toInt() == 1) {
                        return doPerform()
                    }
                }
                installFailure("未知原因\n${BytesUtils.bytesToHexStr(bytes)}")
                // 安装失败
                return

            }
        }
    }
}