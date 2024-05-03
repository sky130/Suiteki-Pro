package com.github.sky130.suiteki.pro.logic.ble

sealed class InstallStatus(open val progress: Int) {

    data object Nope : InstallStatus(0)

    data class Installing(override val progress: Int) : InstallStatus(progress)

    data class InstallFailure(override val progress: Int, val message: String) :
        InstallStatus(progress)

    data class InstallSuccess(override val progress: Int) : InstallStatus(progress)

}