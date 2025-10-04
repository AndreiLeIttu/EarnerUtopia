package com.aospi.earnerutopia

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aospi.earnerutopia.ui.PlanScreen
import com.aospi.earnerutopia.ui.StartScreen

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
            StartScreen(modifier,
                onNextButtonClicked = {navController.navigate(EarnerUtopia.Plan.name)},
                name)
        }
        composable(route = EarnerUtopia.Plan.name) {
            PlanScreen(modifier)
        }
    }
}