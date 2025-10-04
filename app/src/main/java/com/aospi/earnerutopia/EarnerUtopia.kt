package com.aospi.earnerutopia

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aospi.earnerutopia.ui.theme.StartScreen

@Composable
fun EarnerUtopiaApp(
    name: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = EarnerUtopia.Start.name,
        modifier = modifier,
    ) {
        composable(route = EarnerUtopia.Start.name) {
            StartScreen(modifier, name)
        }
    }
}