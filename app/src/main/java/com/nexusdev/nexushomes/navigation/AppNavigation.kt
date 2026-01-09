package com.nexusdev.nexushomes.navigation

import  androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexusdev.nexushomes.ui.screens.HomeScreen
import com.nexusdev.nexushomes.ui.screens.HouseDetailScreen
import com.nexusdev.nexushomes.ui.screens.LoginScreen
import com.nexusdev.nexushomes.ui.screens.MuUpdates
import com.nexusdev.nexushomes.ui.screens.PhoneLoginScreen
import com.nexusdev.nexushomes.ui.screens.ProfileScreen
import com.nexusdev.nexushomes.ui.screens.PublishScreen
import com.nexusdev.nexushomes.ui.viewmodel.HomeDataViewModel

@Composable
fun AppNavigation(
    modifier: Modifier
) {
    val navController = rememberNavController()
    val homeViewModel: HomeDataViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(modifier = modifier, navController)
        }
        composable("detail/{houseId}") { backStack ->
            val id = backStack.arguments?.getString("houseId") ?: ""
            HouseDetailScreen(id, homeViewModel, navController)
        }
        composable("addNew") {
            PublishScreen(navController, modifier)
        }
        composable("login") {
            LoginScreen(
                viewModel = viewModel(),
                navController
            )
        }
        composable("profile") {
            ProfileScreen(
                modifier,
                navController
            )
        }
        composable("phoneLogin") {
            PhoneLoginScreen()
        }
        composable("updates") {
            MuUpdates(
                modifier,
                navController
            )
        }
    }
}
