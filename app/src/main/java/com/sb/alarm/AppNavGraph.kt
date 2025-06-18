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
            route = "updateSchedule/{alarmId}/{selectedDate}",
            arguments = listOf(
                navArgument("alarmId") { type = NavType.IntType },
                navArgument("selectedDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: 0
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            UpdateScheduleScreen(
                alarmId = alarmId,
                selectedDate = selectedDate,
                navController = navController
            )
        }
    }
} 