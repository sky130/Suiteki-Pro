package com.github.sky130.suiteki.pro.device.miband7

object HuamiService {
    const val BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb" // 基础UUID
    const val UUID_SERVICE_FIRMWARE = "00001530-0000-3512-2118-0009af100700" // 固件服务
    const val UUID_CHARACTERISTIC_FIRMWARE_NOTIFY =
        "00001531-0000-3512-2118-0009af100700" // 发送指令以及订阅通知特征
    const val UUID_CHARACTERISTIC_FIRMWARE_WRITE = "00001532-0000-3512-2118-0009af100700" // 发送固件特征
    const val UUID_SERVICE_MIBAND_SERVICE = "0000fee0-0000-1000-8000-00805f9b34fb" // 米环服务
    const val UUID_CHARACTERISTIC_APP = "00000016-0000-3512-2118-0009af100700" // 小程序特征
    const val UUID_CHARACTERISTIC_AUTH_WRITE = "00000016-0000-3512-2118-0009af100700" // 验证发送特征
    const val UUID_CHARACTERISTIC_AUTH_NOTIFY = "00000017-0000-3512-2118-0009af100700" // 验证订阅通知特征
    const val UUID_SERVICE_GENERIC_ACCESS =
        "00001800-0000-1000-8000-00805f9b34fb" // 通用访问配置文件,用于获取基础设备信息
    const val UUID_CHARACTERISTIC_DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb" // 获取设备名称特征
    const val UUID_SERVICE_DEVICE_INFORMATION =
        "0000180a-0000-1000-8000-00805f9b34fb" // 设备信息服务,用于获取设备信息
    const val UUID_CHARACTERISTIC_SERIAL_NUMBER = "00002a25-0000-1000-8000-00805f9b34fb" // 设备SN码查询
    const val UUID_CHARACTERISTIC_ZEPP_OS_VERSION =
        "00002a28-0000-1000-8000-00805f9b34fb" // 设备系统版本查询
    const val UUID_SERVICE_DEVICE_BATTERY =
        "0000180f-0000-1000-8000-00805f9b34fb" // 设备电量服务,用于获取设备信息
    const val UUID_CHARACTERISTIC_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb" // 设备电量查询
    const val UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb" //
    const val STATUS_BLE_NOPE = 0x00 // 未连接
    const val STATUS_BLE_CONNECTING = 0x01 // 正在连接
    const val STATUS_BLE_CONNECTED = 0x02 // 连接完毕
    const val STATUS_BLE_AUTHING = 0x03 // 正在验证
    const val STATUS_BLE_AUTHED = 0x04 // 验证完毕
    const val STATUS_BLE_NORMAL = 0x05 // 准备工作
    const val STATUS_BLE_INSTALLING = 0x06 // 正在安装表盘
    const val STATUS_BLE_CONNECT_FAILURE = 0x07 // 连接失败
    const val STATUS_BLE_DISCONNECT = 0x08 // 断开连接
    const val TYPE_D2_WATCHFACE: Byte = 8 // 08 表盘
    const val TYPE_D2_FIRMWARE: Byte = -3 // FD 固件
    const val TYPE_D2_APP: Byte = -96 // A0 小程序
}