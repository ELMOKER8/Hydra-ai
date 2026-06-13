package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.screens.MainHubScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HydrationViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HydrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val activeTheme = userProfile?.chosenTheme ?: "Ocean Theme"

            MyApplicationTheme(chosenTheme = activeTheme) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(navController = navController, viewModel = viewModel)
                    }
                    composable(Screen.Main.route) {
                        MainHubScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
