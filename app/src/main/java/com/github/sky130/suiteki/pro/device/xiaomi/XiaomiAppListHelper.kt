package com.github.sky130.suiteki.pro.device.xiaomi

import androidx.compose.runtime.mutableStateListOf
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_RPK_DELETE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_RPK_LIST
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.RPK_COMMAND_TYPE
import com.github.sky130.suiteki.pro.logic.ble.AppInfo
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.Command
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.RpkInfoList
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.RpkList
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.RpkMessage
import com.google.protobuf.ByteString

class XiaomiAppListHelper(val device: XiaomiDevice) {

    val list = mutableStateListOf<AppInfo>()
    private val shaMap = hashMapOf<String, ByteString>()

    fun handleCommand(command: Command) {
        if (command.type != RPK_COMMAND_TYPE) return
        when (command.subtype) {
            CMD_RPK_LIST -> {
                handleRpkList(command.rpkMessage.rpkList)
            }
        }
    }

    fun delete(id: String) {
        device.support.sendCommand(
            Command.newBuilder().setType(RPK_COMMAND_TYPE).setSubtype(
                CMD_RPK_DELETE
            ).setRpkMessage(
                RpkMessage.newBuilder().setRpkDel(
                    RpkInfoList.newBuilder().setId(id)
                        .setSha(ByteString.copyFrom(shaMap[id]!!.toByteArray())).build()
                ).build()
            ).build()
        )
        list.removeAt(list.indexOfFirst { it.id == id })
        shaMap.remove(id)
    }

    fun requestRpkList() {
        device.support.sendCommand(RPK_COMMAND_TYPE, CMD_RPK_LIST)
    }

    private fun handleRpkList(rpkList: RpkList) {
        list.clear()
        for (i in rpkList.rpkInfoList) {
            list.add(AppInfo(i.id, i.name))
            shaMap[i.id] = i.sha
        }
    }

}