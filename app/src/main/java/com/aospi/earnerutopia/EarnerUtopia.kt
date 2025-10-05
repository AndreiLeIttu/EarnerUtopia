package com.aospi.earnerutopia

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aospi.earnerutopia.ui.PlanScreen
import com.aospi.earnerutopia.ui.ScheduleScreen
import com.aospi.earnerutopia.ui.StartScreen
import com.aospi.earnerutopia.viewmodel.ScheduleViewModel
import com.aospi.earnerutopia.ui.camera.CameraXCaptureScreen


@Composable
fun EarnerUtopiaApp(
    name: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = EarnerUtopia.Start.name,
        modifier = modifier,
    ) {
        composable(route = EarnerUtopia.Start.name) {
            StartScreen(modifier = modifier,
                name = name,
                onOpenCamera = { navController.navigate("CaptureForModel") },
                navController = navController
            )
        }
        composable(route = EarnerUtopia.Plan.name) {
            PlanScreen(
                modifier,
                viewModel = scheduleViewModel)
        }
        composable(route = EarnerUtopia.Schedule.name) {
            ScheduleScreen(
                modifier,
                onNextButtonClicked = {navController.navigate(EarnerUtopia.Plan.name)},
                viewModel = scheduleViewModel)
        }
        composable(route = "CaptureForModel") {
            CameraXCaptureScreen(
                onPhotoBytes = { bytes ->{ Log.d("CameraX", "Captured pula} bytes")}
                    // TODO: send to your model (upload/run)
                },
                onDone = { navController.popBackStack() }
            )
        }
    }
}