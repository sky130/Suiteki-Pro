package com.github.sky130.suiteki.pro

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.sky130.suiteki.pro.basic.SuitekiActivity
import com.github.sky130.suiteki.pro.ui.screen.device.DeviceScreen
import com.github.sky130.suiteki.pro.ui.screen.home.HomeScreen
import com.github.sky130.suiteki.pro.ui.screen.more.MoreScreen
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

class MainActivity : SuitekiActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @Preview
    override fun Content() {
        SuitekiTheme {
            val navController = rememberNavController()
            var currentScreen by remember { mutableStateOf<SuitekiDestination>(Home) }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreen.label) }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        for (i in suitekiScreens) {
                            NavigationBarItem(
                                selected = i == currentScreen,
                                onClick = {
                                    if (currentScreen != i) {
                                        currentScreen = i
                                        navController.navigate(i.route)
                                    }
                                },
                                icon = { Icon(i.icon, i.label) },
                                label = { Text(i.label) }
                            )
                        }
                    }
                }) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Home.route,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = {
                        fadeIn(animationSpec = tween(400), initialAlpha = 0f)
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(400), targetAlpha = 0f)
                    }
                ) {
                    composable(route = Home.route) {
                        HomeScreen()
                    }
                    composable(route = Device.route) {
                        DeviceScreen()
                    }
                    composable(route = More.route) {
                        MoreScreen()
                    }
                }
            }
        }
    }

    override fun init() {

    }

}