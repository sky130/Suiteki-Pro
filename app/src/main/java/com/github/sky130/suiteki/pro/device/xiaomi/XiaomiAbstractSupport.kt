package com.github.sky130.suiteki.pro.device.xiaomi

import android.bluetooth.BluetoothGatt
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.github.sky130.suiteki.pro.proto.xiaomi.XiaomiProto.Command

abstract class XiaomiAbstractSupport(val device: XiaomiDevice) : BleGattCallback() {
    abstract fun start()

    abstract fun sendCommand(command: Command)

    override fun onStartConnect() {}

    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {}

    override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {}

    override fun onDisConnected(
        isActiveDisConnected: Boolean, device: BleDevice, gatt: BluetoothGatt, status: Int
    ) {
    }
}