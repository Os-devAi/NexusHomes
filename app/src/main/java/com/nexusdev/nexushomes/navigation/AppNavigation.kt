package com.nexusdev.nexushomes.navigation

import  androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexusdev.nexushomes.ui.screens.HomeScreen
import com.nexusdev.nexushomes.ui.screens.PublishScreen

@Composable
fun AppNavigation(
    modifier: Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(modifier = modifier, navController)
        }
        composable("addNew") {
            PublishScreen(navController, modifier)
        }
    }
}
