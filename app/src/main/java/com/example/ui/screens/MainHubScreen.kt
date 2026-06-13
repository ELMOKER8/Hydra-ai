package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.MainTab
import com.example.ui.viewmodel.HydrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHubScreen(viewModel: HydrationViewModel) {
    var activeTab by remember { mutableStateOf<String>("dashboard") }
    val profile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HYDRA AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    // Quick add simulator tool
                    IconButton(onClick = { activeTab = "widgets" }) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Widget Simulator",
                            tint = if (activeTab == "widgets") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings trigger
                    IconButton(onClick = { activeTab = "settings" }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (activeTab == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                // Tab 1: Dashboard
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Coach", fontWeight = FontWeight.Bold) }
                )

                // Tab 2: Statistics
                NavigationBarItem(
                    selected = activeTab == "statistics",
                    onClick = { activeTab = "statistics" },
                    icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Metrics") },
                    label = { Text("Metrics", fontWeight = FontWeight.Bold) }
                )

                // Tab 3: AI Coach chat
                NavigationBarItem(
                    selected = activeTab == "ai_coach",
                    onClick = { activeTab = "ai_coach" },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
                    label = { Text("AI Ask", fontWeight = FontWeight.Bold) }
                )

                // Tab 4: Achievements
                NavigationBarItem(
                    selected = activeTab == "achievements",
                    onClick = { activeTab = "achievements" },
                    icon = { Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Achievements") },
                    label = { Text("Medals", fontWeight = FontWeight.Bold) }
                )

                // Tab 5: History Logs
                NavigationBarItem(
                    selected = activeTab == "history",
                    onClick = { activeTab = "history" },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "History") },
                    label = { Text("Logs", fontWeight = FontWeight.Bold) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "dashboard" -> DashboardScreen(viewModel = viewModel)
                "statistics" -> StatisticsScreen(viewModel = viewModel)
                "ai_coach" -> AiInsightsScreen(viewModel = viewModel)
                "achievements" -> AchievementsScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "widgets" -> WidgetSimulatorScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
