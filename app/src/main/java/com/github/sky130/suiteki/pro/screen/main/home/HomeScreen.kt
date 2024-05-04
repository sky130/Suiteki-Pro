package com.github.sky130.suiteki.pro.screen.main.home

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.logic.ble.DeviceStatus
import com.github.sky130.suiteki.pro.logic.ble.InstallStatus
import com.github.sky130.suiteki.pro.logic.ble.InstallStatus.*
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager.waitForAuth
import com.github.sky130.suiteki.pro.screen.main.ExternalNavigator
import com.github.sky130.suiteki.pro.screen.main.MainGraph
import com.github.sky130.suiteki.pro.screen.main.more.AppCard
import com.github.sky130.suiteki.pro.ui.widget.DialogState
import com.github.sky130.suiteki.pro.ui.widget.SuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.github.sky130.suiteki.pro.ui.widget.rememberDialogState
import com.github.sky130.suiteki.pro.util.TextUtils.copyText
import com.hitanshudhawan.circularprogressbar.CircularProgressBar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AppScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FolderScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.onResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<MainGraph>(start = true)
fun HomeScreen(
    navigator: ExternalNavigator,
    resultRecipient: ResultRecipient<FolderScreenDestination, File>
) {
    val scope = rememberCoroutineScope()
    val installDialogState = rememberDialogState()
    resultRecipient.onResult { resultValue ->
        Log.d("TAG", "got result ${resultValue.name}")
        installDialogState.show()
        scope.launch(Dispatchers.IO) {
            val bytes = resultValue.readBytes()
            withContext(Dispatchers.Main) {
                SuitekiManager.bleDevice.value?.install(bytes)
            }
        }
    }
    val visible = rememberDialogState()
    DeviceDialog(visible)
    InstallDialog(state = installDialogState, visible)
    SuitekiScaffold(
        topBar = {
            SuitekiTopBar(title = "主页")
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxSize()
        ) {
            val ble by SuitekiManager.bleDevice.collectAsState(null)
            if (ble == null) {
                DisconnectScreen()
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


                    Spacer(modifier = Modifier.height(10.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                "安装",
                                Icons.Default.FolderOpen
                            ) {
                                waitForAuth() ?: return@AppCard
                                navigator.navigator.navigate(FolderScreenDestination)
                            }
                        }
                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                "调试",
                                Icons.Default.Code
                            ) {
                                visible.show()
                            }
                        }
                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                "应用管理",
                                Icons.Default.AppRegistration
                            ) {
                                waitForAuth() ?: return@AppCard
                                navigator.navigator.navigate(AppScreenDestination)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisconnectScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Block, null, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text("请先选择设备", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun InstallDialog(state: DialogState, log: DialogState) {
    SuitekiDialog(state = state, title = "安装文件", onDismissRequest = { }) {
        LaunchedEffect(Unit) {
            SuitekiManager.installStatus.value = Nope
        }
        val status by SuitekiManager.installStatus
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressBar(
                modifier = Modifier.size(130.dp),
                progress = status.progress.toFloat(),
                progressMax = 100f,
                progressBarColor = MaterialTheme.colorScheme.primary,
                progressBarWidth = 15.dp,
                backgroundProgressBarColor = MaterialTheme.colorScheme.surfaceVariant,
                backgroundProgressBarWidth = 15.dp,
                roundBorder = true,
                startAngle = 0f,
            )
            Spacer(modifier = Modifier.height(15.dp))
            when (status) {
                is InstallSuccess -> {
                    Text(text = "安装完成", style = MaterialTheme.typography.titleLarge)
                }

                is InstallFailure -> {
                    Text(text = "安装失败", style = MaterialTheme.typography.titleLarge)
                    Text(text = (status as InstallFailure).message, style = MaterialTheme.typography.titleMedium)

                }

                is Installing -> {
                    Text(text = "${status.progress}%", style = MaterialTheme.typography.titleLarge)
                }

                Nope -> {}
            }
            if (status is InstallSuccess || status is InstallFailure) {
                Button(onClick = { state.dismiss() }, modifier = Modifier.padding(top = 15.dp)) {
                    Text(text = "返回")
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
fun DeviceDialog(state: DialogState) {
    val list = remember {
        SuitekiManager.logList
    }
    SuitekiDialog(
        state = state,
        onDismissRequest = {},
        button = {
            OutlinedButton(onClick = { state.dismiss() }) {
                Text(text = "取消")
            }
            OutlinedButton(onClick = { list.joinToString("\n\n").copyText() }) {
                Text(text = "复制")
            }
        },
        icon = Icons.Filled.Watch,
        title = "设备日志",
    ) {


        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(list) {
                Text(text = it)
            }
        }
    }
}
