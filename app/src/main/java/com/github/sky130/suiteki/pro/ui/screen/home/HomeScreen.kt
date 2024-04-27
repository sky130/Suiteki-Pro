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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.ble.AuthStatus
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
                    val authStatus by it.auth.status.collectAsState(AuthStatus.Authing)
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
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(2),modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            AppCard(modifier = Modifier.fillMaxWidth(),"安装",Icons.Default.FolderOpen){}
                        }
                        item {
                            AppCard(modifier = Modifier.fillMaxWidth(),"调试",Icons.Default.Code){}
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