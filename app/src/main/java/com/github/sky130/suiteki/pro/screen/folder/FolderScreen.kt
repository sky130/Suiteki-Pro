package com.github.sky130.suiteki.pro.screen.folder

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.sky130.suiteki.pro.ui.widget.SuitekiScaffold
import com.github.sky130.suiteki.pro.ui.widget.SuitekiTopBar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import java.io.File

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun FolderScreen(
    resultNavigator: ResultBackNavigator<File>
) {
    val files = remember {
        mutableStateListOf(File("/sdcard"))
    }

    val fileList = remember {
        mutableStateListOf<File>()
    }


    fun File.refresh() {
        fileList.clear()
        fileList.addAll(listFiles().filter {
            it.isDirectory || it.extension.lowercase() in listOf(
                "rpk",
                "bin",
                "zip",
                "face"
            )
        }.sortedWith(compareBy<File> { it.isDirectory }.thenBy { it.name }))
    }

    LaunchedEffect(Unit) {
        files.last().refresh()
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "选择文件") }, navigationIcon = {
            IconButton(onClick = { resultNavigator.navigateBack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
        })
    }) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(files) { index, item ->
                        OutlinedCard(
                            onClick = {
                                if (index + 1 == files.size) return@OutlinedCard
                                for (i in index + 1..<files.size) {
                                    files.removeAt(index + 1)
                                }
                                item.refresh()
                            },
                            modifier = Modifier.width(IntrinsicSize.Min),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = item.name)
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(fileList) {
                            ElevatedCard(onClick = {
                                if (it.isDirectory) {
                                    files.add(it)
                                    it.refresh()
                                } else if (it.isFile) {
                                    Log.d("TAG", "put result ${it.name}")
                                    resultNavigator.navigateBack(result = it)
                                }
                            }, modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Icon(
                                        if (it.isDirectory) {
                                            Icons.Default.FolderOpen
                                        } else {
                                            Icons.Outlined.UploadFile
                                        }, null
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = it.name)
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}
