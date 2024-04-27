package com.github.sky130.suiteki.pro.logic.ble

import android.bluetooth.BluetoothGatt
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BleSupport(private val mac: String) : BleGattCallback() {
    lateinit var bleDevice: BleDevice
    private val manager get() = BleManager.getInstance()
    private lateinit var device: AbstractBleDevice
    private val job = Job()
    private val scope = CoroutineScope(job)

    init {
        manager.connect(mac,this)
    }

    override fun onStartConnect() {}

    override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {}

    override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
        scope.launch {
            this@BleSupport.bleDevice = bleDevice
            for ((service, characteristic) in device.notifyUUIDs) {
                manager.notify(bleDevice, service, characteristic, object : BleNotifyCallback() {
                    override fun onNotifySuccess() {
                        Log.d("TAG", "$service $characteristic onNotifySuccess")
                    }

                    override fun onNotifyFailure(exception: BleException) {
                        Log.d("TAG", "$service $characteristic onNotifyFailure")
                        device.onNotifyFailure(service to characteristic)
                    }

                    override fun onCharacteristicChanged(data: ByteArray) {
                        device.onHandle(data, service to characteristic)
                    }

                })
                delay(250)
            }
            device.auth.startAuth(device.key)
            device.auth.status.onEach {
                Log.d("TAG",it.name)
            }.launchIn(scope)
        }
    }

    override fun onDisConnected(
        isActiveDisConnected: Boolean, device: BleDevice, gatt: BluetoothGatt, status: Int
    ) {

    }

    fun initNotify(device: AbstractBleDevice) {
        this.device = device
    }

    fun write(data: ByteArray, uuids: UUIDS) {
        manager.write(bleDevice, uuids.first, uuids.second, data, object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {

            }

            override fun onWriteFailure(exception: BleException?) {

            }
        })
    }

    fun disconnect() {
        manager.disconnect(bleDevice)
    }

}