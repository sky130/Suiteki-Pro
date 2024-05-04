package com.github.sky130.suiteki.pro.device.xiaomi

import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_FIRMWARE_INSTALL
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_RPK_INSTALL
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_UPLOAD_START
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.CMD_WATCHFACE_INSTALL
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.RPK_COMMAND_TYPE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.UPLOAD_COMMAND_TYPE
import com.github.sky130.suiteki.pro.device.xiaomi.XiaomiService.WATCHFACE_COMMAND_TYPE
import com.github.sky130.suiteki.pro.logic.ble.InstallStatus
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.Command
import com.github.sky130.suiteki.pro.util.CheckSums
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil
import kotlin.math.min

class XiaomiInstallHelper(val device: XiaomiDevice, private val fw: ByteArray) {
    private val helper by lazy { XiaomiFWHelper(fw) }
    private val support get() = device.support
    private var chunkSize = 2048


    fun install() {
        device.scope.launch(Dispatchers.IO) {
            helper.init()
            support.sendCommand(
                XiaomiProto.Command.newBuilder()
                    .setType(helper.type)
                    .setSubtype(helper.subType).apply {
                        when (helper.type) {
                            WATCHFACE_COMMAND_TYPE -> {
                                setWatchface(
                                    XiaomiProto.Watchface.newBuilder().setWatchfaceInstallStart(
                                        XiaomiProto.WatchfaceInstallStart.newBuilder()
                                            .setId(helper.id)
                                            .setSize(helper.bytes!!.size)
                                    )
                                )
                            }

                            RPK_COMMAND_TYPE -> {
                                setRpkMessage(
                                    XiaomiProto.RpkMessage.newBuilder().setRpkInfo(
                                        XiaomiProto.RpkInfo.newBuilder().setId(helper.id)
                                            .setSize(helper.bytes!!.size).setUnknown2(3)
                                    )
                                )
                            }
                        }
                    }.build()
            )
//            Command.newBuilder().rpkI
        }
    }

    private fun requestUpload() {
        support.sendCommand(
            XiaomiProto.Command.newBuilder()
                .setType(UPLOAD_COMMAND_TYPE)
                .setSubtype(CMD_UPLOAD_START)
                .setDataUpload(
                    XiaomiProto.DataUpload.newBuilder()
                        .setDataUploadRequest(
                            XiaomiProto.DataUploadRequest.newBuilder()
                                .setType(helper.fileType)
                                .setMd5Sum(
                                    ByteString.copyFrom(
                                        CheckSums.md5(
                                            fw
                                        )
                                    )
                                ).setSize(fw.size)
                        )
                ).build()
        )
    }

    fun handleCommand(cmd: XiaomiProto.Command) {
        if (cmd.type !in listOf(
                WATCHFACE_COMMAND_TYPE,
                RPK_COMMAND_TYPE,
                UPLOAD_COMMAND_TYPE
            )
        ) return
        when (cmd.subtype) {
            CMD_WATCHFACE_INSTALL, CMD_FIRMWARE_INSTALL, CMD_RPK_INSTALL -> {
                requestUpload()
            }

            CMD_UPLOAD_START -> {
                val dataUploadAck = cmd.dataUpload.dataUploadAck

                if (dataUploadAck.unknown2 != 0 || dataUploadAck.resumePosition != 0) {
                    installFailure(0, "Unknown Error")
                    return
                }

                chunkSize = if (dataUploadAck.hasChunkSize()) {
                    dataUploadAck.chunkSize
                } else {
                    2048
                }
                doUpload()
            }
        }
    }

    private fun doUpload() {
        SuitekiManager.log("doUpload")
        // type + md5 + size + bytes + crc32
        val buf1 = ByteBuffer.allocate(2 + 16 + 4 + fw.size).order(ByteOrder.LITTLE_ENDIAN)
        val md5 = CheckSums.md5(fw)
        if (md5 == null) {
            installFailure(0, "MD5 Missing")
            return
        }

        buf1.put(0.toByte())
        buf1.put(helper.subType.toByte())
        buf1.put(md5)
        buf1.putInt(fw.size)
        buf1.put(fw)

        val buf2 = ByteBuffer.allocate(buf1.capacity() + 4).order(ByteOrder.LITTLE_ENDIAN)
        buf2.put(buf1.array())
        buf2.putInt(CheckSums.getCRC32(buf1.array()))

        val payload = buf2.array()
        val partSize = chunkSize - 4 // 2 + 2 at beginning of each for total and progress
        val totalParts = ceil((payload.size / partSize.toFloat()).toDouble()).toInt()

        var i = 0
        while (i * partSize < payload.size) {
            val currentPart = i + 1
            val startIndex = i * partSize
            val endIndex = min((currentPart * partSize).toDouble(), payload.size.toDouble())
            val chunkToSend = ByteArray((4 + endIndex - startIndex).toInt())
            writeUint16(chunkToSend, 0, totalParts)
            writeUint16(chunkToSend, 2, currentPart)
            System.arraycopy(payload, startIndex, chunkToSend, 4, (endIndex - startIndex).toInt())

            support.sendDataChunk(
                chunkToSend
            ) {
                val progress = ((currentPart.toFloat() / totalParts.toFloat()) * 100).toInt()
                SuitekiManager.log("doUploadProgress", progress)

                if (currentPart >= totalParts) {
                    installSuccess(100)
                } else {
                    updateProgress(progress)
                }
            }
            i++
        }

    }

    private fun updateProgress(progress: Int) {
        SuitekiManager.installStatus.value = InstallStatus.Installing(progress)
    }

    private fun installFailure(progress: Int, messages: String) {
        SuitekiManager.installStatus.value = InstallStatus.InstallFailure(progress, messages)
        device.onInstallFinish()
    }

    private fun installSuccess(progress: Int) {
        SuitekiManager.installStatus.value = InstallStatus.InstallSuccess(progress)
        device.onInstallFinish()
    }

    fun writeUint16(array: ByteArray, offset: Int, value: Int) {
        array[offset] = value.toByte()
        array[offset + 1] = (value shr 8).toByte()
    }

}