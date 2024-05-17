package com.github.sky130.suiteki.pro.screen.main.device

import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.MainApplication.Companion.context
import com.github.sky130.suiteki.pro.MainApplication.Companion.toast
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager
import com.github.sky130.suiteki.pro.logic.ble.SuitekiManager.waitForAuth
import com.github.sky130.suiteki.pro.logic.database.AppDatabase
import com.github.sky130.suiteki.pro.logic.database.model.Device
import com.github.sky130.suiteki.pro.screen.main.MainGraph
import com.github.sky130.suiteki.pro.screen.main.more.AppCard
import com.github.sky130.suiteki.pro.ui.widget.BaseSuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.DialogState
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiDialog
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.github.sky130.suiteki.pro.ui.widget.rememberDialogState
import com.github.sky130.suiteki.pro.util.TextUtils
import com.github.sky130.suiteki.pro.util.ZipUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.FolderScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<MainGraph>
fun DeviceScreen() {
    val scope = rememberCoroutineScope()
    val addDialogState = rememberDialogState()
    val scanDialogState = rememberDialogState()

    SuitekiScaffold(fab = {
        FloatingActionButton(onClick = { addDialogState.show() }) {
            Icon(Icons.Filled.Add, null)
        }
    }, topBar = {
        SuitekiTopBar(title = "设备")
    }) {
        ScanLogDialog(state = scanDialogState)
        DeviceAddDialog(addDialogState)
        Column {
            val list by AppDatabase.instance.device().getList().collectAsState(initial = listOf())
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 10.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        AppCard(
                            modifier = Modifier.weight(1f),
                            "小米运动健康添加",
                            Icons.Outlined.Circle
                        ) {
                            scanDialogState.show()
                        }

                        AppCard(
                            modifier = Modifier.weight(1f), "ZeppLife 添加", Icons.Outlined.Terrain
                        ) {
                            scanDialogState.show()
                        }
                    }
                }
                items(list) {
                    val editDialogState = rememberDialogState()
                    DeviceEditDialog(editDialogState, device = it)
                    ElevatedCard(
                        onClick = {
                            SuitekiManager.connect(it)
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Column {
                                Text(it.name)
                                Spacer(Modifier.height(5.dp))
                                Text(it.mac)
                            }
                            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                                IconButton(onClick = { editDialogState.show() }) {
                                    Icon(Icons.Default.Edit, null)
                                }
                                Spacer(Modifier.width(5.dp))
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        AppDatabase.instance.device().delete(it)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ScanLogDialog(state: DialogState) {
    if (!state.visible.value) return
    val scope = rememberCoroutineScope()
    BaseSuitekiDialog(state = state, onDismissRequest = {
        state.dismiss()
    }, confirmButton = {
        TextButton(onClick = {
            "开始扫描".toast()
            scope.launch(Dispatchers.IO) {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "wearablelog"
                ).let {
                    var time = 0L
                    it.listFiles { _, name ->
                       name.endsWith("log.zip")
                    }?.forEach { file ->
                        if (!file.isFile) return@forEach
                        file.nameWithoutExtension.replace("log", "").toLong().let { let ->
                            if (time < let) {
                                time = let
                            }
                        }
                    }
                    if (time == 0L) {
                        return@launch withContext(Dispatchers.Main) { "加载失败，日志压缩包不存在".toast() }
                    }
                    val latestLogFile = File(it, "${time}log.zip")
                    latestLogFile?.let {
                        val text = ZipUtils.extractFrom(
                            it, "XiaomiFit.device.log"
                        )
                            ?: return@launch withContext(Dispatchers.Main) { "加载失败，日志文件不存在".toast() }
                        val deviceList = arrayListOf<Device>()
                        for (i in text.split("\n")) {
                            val mac = TextUtils.getRegexMatchText(i, "macAddress=", ", ")
                            if (mac.isEmpty()||mac.length < 15) continue
                            val authKey = TextUtils.getRegexMatchText(i, "authKey=", ", ")
                            val model = TextUtils.getRegexMatchText(i, "model='", "', ")
                            val encryptKey = TextUtils.getRegexMatchText(i, "encryptKey=", ", ")
                            val end = mac.substring(12, 14) + mac.substring(15)
                            when (model) {
                                "miwear.watch.m66",
                                "miwear.watch.m66nfc",
                                "miwear.watch.m66tc",
                                "miwear.watch.m66dsn",
                                "miwear.watch.m66gl",
                                "miwear.watch.m66gln" -> {
                                    deviceList.add(
                                        Device(
                                            "Xiaomi Smart Band 8 $end",
                                            mac,
                                            encryptKey
                                        )
                                    )
                                }

                                "lchz.watch.m67",
                                "lchz.watch.m67gl",
                                "lchz.watch.m67ys" -> {
                                    deviceList.add(
                                        Device(
                                            "Xiaomi Smart Band 8 Pro $end",
                                            mac,
                                            encryptKey
                                        )
                                    )
                                }

                                "hmpace.watch.v7",
                                "hmpace.watch.v7nfc" -> {
                                    deviceList.add(
                                        Device(
                                            "Xiaomi Smart Band 7 $end",
                                            mac,
                                            authKey
                                        )
                                    )
                                }

                                "lchz.watch.n65",
                                "lchz.watch.n65gl" -> {
                                    deviceList.add(
                                        Device(
                                            "Redmi Watch 4 $end",
                                            mac,
                                            encryptKey
                                        )
                                    )
                                }


                                "lchz.watch.n65acn",
                                "lchz.watch.n65agl",
                                "lchz.watch.n65ain" -> {
                                    deviceList.add(
                                        Device(
                                            "Redmi Watch 4 $end Active",
                                            mac,
                                            encryptKey
                                        )
                                    )
                                }

                            }
                        }
                        AppDatabase.instance.device().insert(deviceList)
                    }
                }
                withContext(Dispatchers.Main) {
                    "扫描完成".toast()
                }
            }
        }) {
            Text(text = "扫描")
        }

    }, dismissButton = {
        TextButton(onClick = { state.dismiss() }) {
            Text(text = "退出")
        }
    }, icon = { Icon(imageVector = Icons.Default.Watch, contentDescription = null) }, title = {
        Text(
            text = "通过小米运动健康添加"
        )
    }, text = {

        val about = remember {
            """
            在开始扫描日志文件之前, 需要您完成下面的操作:
            1.将小米运动健康结束后台运行(如果实在不了解"后台运行"是什么,可以选择将手机重启)
            2.进入小米运动健康
            3.点击右下角的[我的],滑动到底部点击[关于]
            4.连续点击那个[橙色圆圈](小米应用图标)
            5.提示对话框出来时,请点击确认
            6.对话框消失后,进入Suiteki Pro
            7.点击下面的[扫描]按钮
        """.trimIndent()
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for (i in about.split("\n")) {
                Text(text = i)
            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAddDialog(dialogState: DialogState) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    SuitekiDialog(
        state = dialogState,
        onDismissRequest = {},
        button = {
            Button(onClick = {
                scope.launch {
                    AppDatabase.instance.device().insert(Device(name, mac, key))
                }
                dialogState.dismiss()
            }) {
                Text(text = "确定")
            }
            OutlinedButton(onClick = { dialogState.dismiss() }) {
                Text(text = "取消")
            }
        },
        icon = Icons.Filled.Watch,
        title = "添加设备",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("设备名") })
            TextField(value = mac,
                onValueChange = { mac = it.replace("：", ":") },
                label = { Text("MAC地址") })
            TextField(value = key, onValueChange = { key = it }, label = { Text("密钥") })
        }
    }
}


@Composable
fun DeviceEditDialog(dialogState: DialogState, device: Device) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(device.name) }
    var mac by remember { mutableStateOf(device.mac) }
    var key by remember { mutableStateOf(device.key) }
    SuitekiDialog(
        state = dialogState,
        onDismissRequest = {},
        button = {
            Button(onClick = {
                scope.launch {
                    AppDatabase.instance.device().insert(Device(name, mac, key))
                }
                dialogState.dismiss()
            }) {
                Text(text = "确定")
            }
            OutlinedButton(onClick = { dialogState.dismiss() }) {
                Text(text = "取消")
            }
        },
        icon = Icons.Filled.Watch,
        title = "修改设备",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("设备名") })
            TextField(value = mac,
                onValueChange = { mac = it.replace("：", ":") },
                label = { Text("MAC地址") })
            TextField(value = key, onValueChange = { key = it }, label = { Text("密钥") })
        }
    }

}
