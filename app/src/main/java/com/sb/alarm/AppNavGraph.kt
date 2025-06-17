package com.sb.alarm

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sb.alarm.presentation.schedule.ScheduleScreen
import com.sb.alarm.presentation.updateSchedule.UpdateScheduleScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = "schedule",
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("schedule") {
            ScheduleScreen(navController = navController)
        }
        
        composable(
            route = "updateSchedule/{alarmId}",
            arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: 0
            UpdateScheduleScreen(
                alarmId = alarmId,
                navController = navController
            )
        }
    }
} 