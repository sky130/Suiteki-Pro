package com.github.sky130.suiteki.pro

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

val suitekiScreens = listOf(Home, Device, More)

interface SuitekiDestination {
    val icon: ImageVector
    val label: String
    val route: String
}

object Home : SuitekiDestination {
    override val icon = Icons.Filled.Home
    override val label = "主页"
    override val route = "home"
}

object Device : SuitekiDestination {
    override val icon = Icons.Filled.Watch
    override val label = "设备"
    override val route = "device"
}

object More : SuitekiDestination {
    override val icon = Icons.Filled.Apps
    override val label = "更多"
    override val route = "more"
}

