package com.github.sky130.suiteki.pro.screen.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.sky130.suiteki.pro.ui.theme.SuitekiTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalDestination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.DeviceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FolderScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MoreScreenDestination
import com.ramcosta.composedestinations.generated.navgraphs.MainNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState

@Composable
@Destination<RootGraph>(start = true)
fun MainScreen(navigator: DestinationsNavigator) {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        BottomBar(navController = navController)
    }) { innerPadding ->
        DestinationsNavHost(
            navGraph = NavGraphs.main,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            navController = navController,
            defaultTransitions = BasicTransitions(enterTransition = {
                fadeIn(animationSpec = tween(400), initialAlpha = 0f)
            },
                exitTransition = {
                    fadeOut(animationSpec = tween(400), targetAlpha = 0f)
                }),
            dependenciesContainerBuilder = {
                dependency(createExternalNavigator(navigator))
            }
        )
    }
}

class BasicTransitions(
    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
) : NavHostAnimatedDestinationStyle()

@Composable
fun BottomBar(
    navController: NavHostController
) {
    NavigationBar {
        MainItem.entries.forEach { destination ->
            val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        return@NavigationBarItem
                    }
                    navController.navigate(destination.direction) {
                        popUpTo(NavGraphs.main) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        destination.icon, contentDescription = destination.title
                    )
                },
                label = { Text(destination.title) },
            )
        }
    }
}

enum class MainItem(
    val direction: DirectionDestinationSpec, val title: String, val icon: ImageVector
) {
    Home(HomeScreenDestination, "主页", Icons.Default.Home),

    Device(DeviceScreenDestination, "设备", Icons.Default.Watch),

    More(MoreScreenDestination, "更多", Icons.Default.Apps),
}

@NavHostGraph(route = "main")
annotation class MainGraph

data class ExternalNavigator(val navigator: DestinationsNavigator)

fun createExternalNavigator(navController: DestinationsNavigator) = ExternalNavigator(navController)