package com.github.sky130.suiteki.pro.device.huami

import androidx.compose.runtime.mutableStateListOf
import com.github.sky130.suiteki.pro.logic.ble.AppInfo
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.util.BytesUtils
import org.apache.commons.lang3.ArrayUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HuamiAppListHelper(val device: HuamiDevice) {

    companion object {
        const val CMD_RESPONSE: Byte = 0x04

        const val CMD_APPS: Byte = 0x02
        const val CMD_INCOMING: Byte = 0x00
        const val CMD_OUTGOING: Byte = 0x01

        const val CMD_APPS_LIST: Byte = 0x01
        const val CMD_APPS_DELETE: Byte = 0x03
        const val CMD_APPS_DELETING: Byte = 0x04
        const val CMD_APPS_LAUNCH: Byte = 0x06
        const val CMD_APPS_API_LEVEL: Byte = 0x05

        val displayItemNameLookup: HashMap<String, String> = object : HashMap<String, String>() {
            init {
                put("00000001", "personal_activity_intelligence")
                put("00000002", "hr")
                put("00000003", "workout")
                put("00000004", "weather")
                put("00000009", "alarm")
                put("00000010", "wallet")
                put("0000000A", "takephoto")
                put("0000000B", "music")
                put("0000000C", "stopwatch")
                put("0000000D", "countdown")
                put("0000000E", "findphone")
                put("0000000F", "mutephone")
                put("00000011", "alipay")
                put("00000013", "settings")
                put("00000014", "workout_history")
                put("00000015", "eventreminder")
                put("00000016", "compass")
                put("00000019", "pai")
                put("00000031", "wechat_pay")
                put("0000001A", "worldclock")
                put("0000001C", "stress")
                put("0000001D", "female_health")
                put("0000001E", "workout_status")
                put("00000020", "calendar")
                put("00000023", "sleep")
                put("00000024", "spo2")
                put("00000025", "phone")
                put("00000026", "events")
                put("00000033", "breathing")
                put("00000038", "pomodoro")
                put("0000003E", "todo")
                put("0000003F", "mi_ai")
                put("00000041", "barometer")
                put("00000042", "voice_memos")
                put("00000044", "sun_moon")
                put("00000045", "one_tap_measuring")
                put("00000047", "membership_cards")
                put("00000100", "alexa")
                put("00000101", "offline_voice")
                put("00000102", "flashlight")
            }
        }
    }

    val list = mutableStateListOf<AppInfo>()

    fun handlePayload(payload: ByteArray) {
        when (payload[0]) {
            CMD_RESPONSE -> {
                decodeAndUpdateDisplayItems(payload)
            }
        }
    }

    fun launch(appId: String) {
        ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(0x07)
            putInt(BytesUtils.hexToInt(appId))
            device.write(0x0023.toShort(), array(), extendedFlags = true, encrypt = true)
        }
    }

    fun uninstall(appId: String) {
        ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN).apply {

            put(CMD_APPS)
            put(CMD_OUTGOING)
            put(CMD_APPS_DELETE)
            put(0x00.toByte())
            putInt(0x00)
            putInt(0x00)
            putInt(0x00)
            putInt(BytesUtils.hexToInt(appId))

            device.write(0x00a0, array(), extendedFlags = true, encrypt = false)
        }
        list.removeAt(list.indexOfFirst { it.id == appId })
    }


    private fun decodeAndUpdateDisplayItems(payload: ByteArray) {
        if (payload.isEmpty()) return
        if (payload[1] != 0x01.toByte()) return

        val numberScreens = payload[2].toInt()
        if (payload.size != 4 + numberScreens * 12) return

        list.clear()
        for (i in 0 until numberScreens) {
            val screenId = String(ArrayUtils.subarray(payload, 4 + i * 12, 4 + i * 12 + 8))
            if (screenId in displayItemNameLookup) continue
            list.add(AppInfo(screenId, "unknown"))
        }
    }

    fun requestAppItems() {
        SuitekiManager.log("requestAppItems")
        device.write(
            0x0026.toShort(),
            byteArrayOf(CMD_APPS_DELETE, CMD_OUTGOING),
            extendedFlags = true,
            encrypt = true
        )
    }
}