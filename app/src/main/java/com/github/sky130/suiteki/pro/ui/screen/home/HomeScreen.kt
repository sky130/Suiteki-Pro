package com.github.sky130.suiteki.pro.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.ble.DeviceStatus
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.ui.screen.more.AppCard
import com.github.sky130.suiteki.pro.ui.widget.FabScaffold

@Composable
fun HomeScreen() {
    FabScaffold() {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxSize()
        ) {
            val ble by SuitekiManager.bleDevice.collectAsState(null)
            if (ble == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Block, null, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("请先选择设备", style = MaterialTheme.typography.headlineMedium)
                }
            } else {
                ble?.let {
                    val authStatus by it.status.collectAsState(DeviceStatus.Waiting)
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("当前连接", style = MaterialTheme.typography.titleLarge)
                            Text(it.name)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {

                                SimpleChip(
                                    onClick = { /*TODO*/ },
                                    icon = Icons.Default.Bluetooth,
                                    label = it.mac
                                )
                                SimpleChip(
                                    onClick = { /*TODO*/ },
                                    icon = Icons.Default.Link,
                                    label = authStatus.name
                                )

                            }

                        }
                    }

                    val visible = remember {
                        mutableStateOf(false)
                    }
                    DeviceDialog(visible)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(2),modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            AppCard(modifier = Modifier.fillMaxWidth(),"安装",Icons.Default.FolderOpen){}
                        }
                        item {
                            AppCard(modifier = Modifier.fillMaxWidth(),"调试",Icons.Default.Code){
                                visible.value = true
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SimpleChip(onClick: () -> Unit, icon: ImageVector, label: String) {
    OutlinedCard(onClick = onClick) {
        Row(modifier = Modifier.padding(9.dp)) {
            Icon(icon, null)
            Spacer(modifier = Modifier.width(5.dp))
            Text(label)
        }
    }
}

@Composable
fun DeviceDialog(visible: MutableState<Boolean>) {
    var enable by visible
    if (!enable) return
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {

            OutlinedButton(onClick = { enable = false }) {
                Text(text = "取消")
            }
        },
        icon = { Icon(Icons.Filled.Watch, null) },
        title = { Text(text = "设备日志") },
        text = {
            val list = remember {
                SuitekiManager.logList
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                items(list){
                    Text(text = it)
                }
            }
        }
    )
}