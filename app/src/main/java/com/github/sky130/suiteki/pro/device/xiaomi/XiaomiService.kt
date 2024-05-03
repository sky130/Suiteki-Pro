package com.github.sky130.suiteki.pro.device.xiaomi

object XiaomiService {
    const val UUID_SERVICE = "0000fe95-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_COMMAND_READ = "00000051-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_COMMAND_WRITE = "00000052-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_ACTIVITY_DATA = "00000053-0000-1000-8000-00805f9b34fb"
    const val UUID_CHARACTERISTIC_DATA_UPLOAD = "00000055-0000-1000-8000-00805f9b34fb"

    const val WATCHFACE_COMMAND_TYPE = 4
    const val UPLOAD_COMMAND_TYPE = 22
    const val RPK_COMMAND_TYPE = 20

    const val CMD_WATCHFACE_LIST = 0
    const val CMD_WATCHFACE_SET = 1
    const val CMD_WATCHFACE_DELETE = 2
    const val CMD_WATCHFACE_INSTALL = 4

    const val CMD_RPK_LIST = 0
    const val CMD_RPK_INSTALL = 1
    const val CMD_RPK_INSTALL_FINISH = 2
    const val CMD_RPK_DELETE = 3


    const val TYPE_WATCHFACE = 16
    const val TYPE_FIRMWARE = 32
    const val TYPE_RPK = 64

    const val CMD_UPLOAD_START = 0


    const val SYSTEM_COMMAND_TYPE: Int = 2
    const val CMD_BATTERY: Int = 1
    const val CMD_DEVICE_INFO: Int = 2
    const val CMD_CLOCK: Int = 3
    const val CMD_FIRMWARE_INSTALL: Int = 5
    const val CMD_LANGUAGE: Int = 6
    const val CMD_CAMERA_REMOTE_GET: Int = 7
    const val CMD_CAMERA_REMOTE_SET: Int = 8
    const val CMD_PASSWORD_GET: Int = 9
    const val CMD_MISC_SETTING_GET: Int = 14
    const val CMD_MISC_SETTING_SET: Int = 15
    const val CMD_FIND_PHONE: Int = 17
    const val CMD_FIND_WATCH: Int = 18
    const val CMD_PASSWORD_SET: Int = 21
    const val CMD_DISPLAY_ITEMS_GET: Int = 29
    const val CMD_DISPLAY_ITEMS_SET: Int = 30
    const val CMD_WORKOUT_TYPES_GET: Int = 39
    const val CMD_MISC_SETTING_SET_FROM_BAND: Int = 42
    const val CMD_SILENT_MODE_GET: Int = 43
    const val CMD_SILENT_MODE_SET_FROM_PHONE: Int = 44
    const val CMD_SILENT_MODE_SET_FROM_WATCH: Int = 45
    const val CMD_WIDGET_SCREENS_GET: Int = 51
    const val CMD_WIDGET_SCREENS_SET: Int = 52
    const val CMD_WIDGET_PARTS_GET: Int = 53
    const val CMD_DEVICE_STATE_GET: Int = 78
    const val CMD_DEVICE_STATE: Int = 79
}