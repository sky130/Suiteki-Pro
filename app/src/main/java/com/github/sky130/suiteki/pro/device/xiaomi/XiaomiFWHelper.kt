/*  Copyright (C) 2023-2024 José Rebelo

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package com.github.sky130.suiteki.pro.device.xiaomi

import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.RPK_COMMAND_TYPE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.TYPE_FIRMWARE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.TYPE_RPK
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.TYPE_WATCHFACE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.WATCHFACE_COMMAND_TYPE
import com.github.sky130.suiteki.pro.util.StringUtils
import com.github.sky130.suiteki.pro.util.ZipUtils
import org.json.JSONObject
import java.io.File


class XiaomiFWHelper(var bytes: ByteArray?) {
    var type = -1
        private set
    var commandType = -1
        private set
    var id: String? = null
        private set
    var packageName: String? = null
        private set
    var name: String? = null
        private set
    var versionName: String? = null
        private set
    var versionCode: Int? = null
        private set



    fun init(){
        parseBytes()
    }

    val details: String
        get() = if (name != null) name!! else (versionName ?: "UNKNOWN")

    fun unsetFwBytes() {
        this.bytes = null
    }

    private fun parseBytes() {
        if (parseAsWatchface()) {
            checkNotNull(id)
            type = TYPE_WATCHFACE
            commandType = WATCHFACE_COMMAND_TYPE

        } else if (parseAsFirmware()) {
            checkNotNull(versionName)
            type = TYPE_FIRMWARE
            // TODO _commandType = TYPE_FIRMWARE
        } else if (parseAsRpk()) {
            type = TYPE_RPK
            commandType = RPK_COMMAND_TYPE
        }
    }

    private fun parseAsWatchface(): Boolean {
        if (bytes!![0] != 0x5A.toByte() || bytes!![1] != 0xA5.toByte()) {
            return false
        }
        id = StringUtils.untilNullTerminator(bytes!!, 0x28)
        name = StringUtils.untilNullTerminator(bytes!!, 0x68)
        if (id == null) {
            return false
        }

        if (name == null) {
            return false
        }
        try {
            id!!.toInt()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun parseAsFirmware(): Boolean {
        // TODO parse and set version
        return false
    }

    private fun parseAsRpk(): Boolean {
        val tempFile = File.createTempFile("cache", "rpk")
        try {
            tempFile.writeBytes(bytes!!)
            JSONObject(ZipUtils.extractFrom(tempFile, "manifest.json")!!).apply {
                packageName = getString("package").isEmptyThrow()
                name = getString("name").isEmptyThrow()
                versionName = getString("versionName").isEmptyThrow()
                versionCode = getInt("versionCode")
                id = packageName
            }
        } catch (_: Exception) {
            return false
        } finally {
            tempFile.delete()
        }
        return true
    }

    private fun String.isEmptyThrow(e: Exception = Exception()) = apply {
        ifEmpty { throw e }
    }
}
