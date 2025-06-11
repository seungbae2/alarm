package com.sb.alarm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sb.alarm.presentation.schedule.ScheduleScreen

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
            ScheduleScreen()
        }
    }
} 