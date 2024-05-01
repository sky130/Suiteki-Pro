/*  Copyright (C) 2023 Yoran Vulker

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.github.sky130.suiteki.pro.device.xiaomi

import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger


class XiaomiSppPacket {
    var payload: ByteArray? = null
        private set
    private var flag = false
    private var needsResponse = false
    var channel: Int = 0
        private set
    private var opCode = 0
    private var frameSerial = 0
    var dataType: Int = 0
        private set

    class Builder {
        private var payload: ByteArray? = null
        private var flag = false
        private var needsResponse = false
        private var channel = -1
        private var opCode = -1
        private var frameSerial = -1
        private var dataType = -1

        fun build(): XiaomiSppPacket {
            val result = XiaomiSppPacket()

            result.channel = channel
            result.flag = flag
            result.needsResponse = needsResponse
            result.opCode = opCode
            result.frameSerial = frameSerial
            result.dataType = dataType
            result.payload = payload

            return result
        }

        fun channel(channel: Int): Builder {
            this.channel = channel
            return this
        }

        fun flag(flag: Boolean): Builder {
            this.flag = flag
            return this
        }

        fun needsResponse(needsResponse: Boolean): Builder {
            this.needsResponse = needsResponse
            return this
        }

        fun opCode(opCode: Int): Builder {
            this.opCode = opCode
            return this
        }

        fun frameSerial(frameSerial: Int): Builder {
            this.frameSerial = frameSerial
            return this
        }

        fun dataType(dataType: Int): Builder {
            this.dataType = dataType
            return this
        }

        fun payload(payload: ByteArray?): Builder {
            this.payload = payload
            return this
        }
    }

    fun needsResponse(): Boolean {
        return needsResponse
    }

    fun hasFlag(): Boolean {
        return this.flag
    }

    override fun toString(): String {
        return String.format(
            Locale.ROOT,
            "SppPacket{ channel=0x%x, flag=%b, needsResponse=%b, opCode=0x%x, frameSerial=0x%x, dataType=0x%x, payloadSize=%d }",
            channel, flag, needsResponse, opCode, frameSerial, dataType, payload!!.size
        )
    }

    fun encode(authService: XiaomiAuthService, encryptionCounter: AtomicInteger): ByteArray {
        var payload = this.payload

        if (dataType == DATA_TYPE_ENCRYPTED && channel == CHANNEL_PROTO_TX) {
            val packetCounter = encryptionCounter.incrementAndGet()
            payload = authService.encrypt(payload!!, packetCounter)
            payload = ByteBuffer.allocate(payload.size + 2).order(ByteOrder.LITTLE_ENDIAN)
                .putShort(packetCounter.toShort()).put(payload).array()
        } else if (dataType == DATA_TYPE_ENCRYPTED) {
            payload = authService.encrypt(payload!!, 0.toShort().toInt())
        }

        val buffer = ByteBuffer.allocate(11 + payload!!.size).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(PACKET_PREAMBLE)

        buffer.put((channel and 0xf).toByte())
        buffer.put(((if (flag) 0x80 else 0) or (if (needsResponse) 0x40 else 0)).toByte())
        buffer.putShort((payload.size + 3).toShort())

        buffer.put((opCode and 0xff).toByte())
        buffer.put((frameSerial and 0xff).toByte())
        buffer.put((dataType and 0xff).toByte())

        buffer.put(payload)

        buffer.put(PACKET_EPILOGUE)
        return buffer.array()
    }

    companion object {
        val PACKET_PREAMBLE: ByteArray = byteArrayOf(0xba.toByte(), 0xdc.toByte(), 0xfe.toByte())
        val PACKET_EPILOGUE: ByteArray = byteArrayOf(0xef.toByte())

        const val CHANNEL_VERSION: Int = 0

        /**
         * Channel ID for PROTO messages received from device
         */
        const val CHANNEL_PROTO_RX: Int = 1

        /**
         * Channel ID for PROTO messages sent to device
         */
        const val CHANNEL_PROTO_TX: Int = 2
        const val CHANNEL_FITNESS: Int = 3
        const val CHANNEL_VOICE: Int = 4
        const val CHANNEL_MASS: Int = 5
        const val CHANNEL_OTA: Int = 7

        const val DATA_TYPE_PLAIN: Int = 0
        const val DATA_TYPE_ENCRYPTED: Int = 1
        const val DATA_TYPE_AUTH: Int = 2

        fun fromXiaomiCommand(
            command: XiaomiProto.Command,
            frameCounter: Int,
            needsResponse: Boolean
        ): XiaomiSppPacket {
            return newBuilder().channel(CHANNEL_PROTO_TX).flag(true).needsResponse(needsResponse)
                .dataType(
                    if (command.type == XiaomiAuthService.COMMAND_TYPE && command.subtype >= 17) DATA_TYPE_AUTH else DATA_TYPE_ENCRYPTED
                ).frameSerial(frameCounter).opCode(2).payload(command.toByteArray()).build()
        }

        fun newBuilder(): Builder {
            return Builder()
        }

        fun decode(packet: ByteArray): XiaomiSppPacket? {
            if (packet.size < 11) {
                return null
            }

            val buffer = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
            val preamble = ByteArray(PACKET_PREAMBLE.size)
            buffer[preamble]

            if (!PACKET_PREAMBLE.contentEquals(preamble)) {
                return null
            }

            var channel = buffer.get()

            if ((channel.toInt() and 0xf0) != 0) {
                channel = 0x0f
            }

            val flags = buffer.get()
            val flag = (flags.toInt() and 0x80) != 0
            val needsResponse = (flags.toInt() and 0x40) != 0

            if ((flags.toInt() and 0x0f) != 0) {
            }

            // payload header is included in size
            val payloadLength = (buffer.getShort().toInt() and 0xffff) - 3

            if (payloadLength + 11 > packet.size) {
                return null
            }

            val opCode = buffer.get().toInt() and 0xff
            val frameSerial = buffer.get().toInt() and 0xff
            val dataType = buffer.get().toInt() and 0xff
            val payload = ByteArray(payloadLength)
            buffer[payload]

            val epilogue = ByteArray(PACKET_EPILOGUE.size)
            buffer[epilogue]

            if (!PACKET_EPILOGUE.contentEquals(epilogue)) {
                return null
            }

            val result = XiaomiSppPacket()
            result.channel = channel.toInt()
            result.flag = flag
            result.needsResponse = needsResponse
            result.opCode = opCode
            result.frameSerial = frameSerial
            result.dataType = dataType
            result.payload = payload

            return result
        }
    }
}
