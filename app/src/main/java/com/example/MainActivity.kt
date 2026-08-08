package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.db.AppDatabase
import com.example.data.repository.CoachRepository
import com.example.ui.screens.CoachScreen
import com.example.ui.screens.NotebookScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeLibraryScreen
import com.example.ui.screens.ProgressAnalyticsScreen
import com.example.ui.screens.SessionSummaryScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.ActiveCyan
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.SpeakCoachTheme
import com.example.ui.viewmodel.CoachViewModel
import com.example.ui.viewmodel.NotebookViewModel
import com.example.ui.viewmodel.ProgressViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Today)
    object Today : Screen("today", "Today", Icons.Default.Today)
    object Practice : Screen("practice", "Practice", Icons.Default.RecordVoiceOver)
    object Notebook : Screen("notebook", "Notebook", Icons.Default.Book)
    object Progress : Screen("progress", "Analytics", Icons.Default.Insights)
    object CoachSession : Screen("coach/{scenarioId}", "Coach", Icons.Default.RecordVoiceOver) {
        fun createRoute(scenarioId: String) = "coach/$scenarioId"
    }
    object Summary : Screen("summary/{sessionId}", "Summary", Icons.Default.Insights) {
        fun createRoute(sessionId: String) = "summary/$sessionId"
    }
}

class MainActivity : ComponentActivity() {

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission state handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check & Request Audio Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            SpeakCoachTheme {
                SpeakCoachApp()
            }
        }
    }
}

@Composable
fun SpeakCoachApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val coachViewModel: CoachViewModel = viewModel()
    val notebookViewModel: NotebookViewModel = viewModel()
    val progressViewModel: ProgressViewModel = viewModel()

    var isOnboardingCompleted by remember { mutableStateOf(true) }

    val bottomNavItems = listOf(
        Screen.Today,
        Screen.Practice,
        Screen.Notebook,
        Screen.Progress
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DeepNavy,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (selected) ActiveCyan else Color.White.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (selected) ActiveCyan else Color.White.copy(alpha = 0.6f),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ActiveCyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingCompleted) Screen.Today.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onCompleteOnboarding = { profile ->
                        isOnboardingCompleted = true
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Today.route) {
                TodayScreen(
                    onStartScenario = { scenarioId ->
                        coachViewModel.startScenarioSession(scenarioId)
                        navController.navigate(Screen.CoachSession.createRoute(scenarioId))
                    },
                    onOpenNotebook = {
                        navController.navigate(Screen.Notebook.route)
                    }
                )
            }

            composable(Screen.Practice.route) {
                PracticeLibraryScreen(
                    onSelectScenario = { scenarioId ->
                        coachViewModel.startScenarioSession(scenarioId)
                        navController.navigate(Screen.CoachSession.createRoute(scenarioId))
                    }
                )
            }

            composable(Screen.Notebook.route) {
                NotebookScreen(viewModel = notebookViewModel)
            }

            composable(Screen.Progress.route) {
                ProgressAnalyticsScreen(viewModel = progressViewModel)
            }

            composable(
                route = Screen.CoachSession.route,
                arguments = listOf(navArgument("scenarioId") { type = NavType.StringType })
            ) {
                CoachScreen(
                    viewModel = coachViewModel,
                    onNavigateSummary = { sessionId ->
                        navController.navigate(Screen.Summary.createRoute(sessionId)) {
                            popUpTo(Screen.CoachSession.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Summary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                SessionSummaryScreen(
                    viewModel = coachViewModel,
                    sessionId = sessionId,
                    onDone = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Today.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
