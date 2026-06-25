package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SaptahaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SaptahaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. SPLASH SCREEN
                        composable("splash") {
                            SplashScreen(
                                onProceed = {
                                    navController.navigate("dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. MAIN DASHBOARD HUB
                        composable("dashboard") {
                            DashboardHub(
                                viewModel = viewModel,
                                onNavigateToTopic = { topicId ->
                                    navController.navigate("reader")
                                }
                            )
                        }

                        // 3. TEXT READER / SHLOKA MODULE SCREEN
                        composable("reader") {
                            ReadingScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHub(
    viewModel: SaptahaViewModel,
    onNavigateToTopic: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: Days, 2: Devotional, 3: Bookmarks, 4: Settings

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WarmCream,
                contentColor = Saffron,
                modifier = Modifier.testTag("dashboard_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("गृह (Home)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = Gold.copy(alpha = 0.2f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Saptaha Days") },
                    label = { Text("७ दिन (Days)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = Gold.copy(alpha = 0.2f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("nav_item_days")
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Devotional") },
                    label = { Text("भक्ति (Devotional)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = Gold.copy(alpha = 0.2f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("nav_item_devotional")
                )

                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Bookmarks") },
                    label = { Text("सङ्ग्रह (Library)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = Gold.copy(alpha = 0.2f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("nav_item_bookmarks")
                )

                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("प्राथमिकता (Settings)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Saffron,
                        selectedTextColor = Saffron,
                        indicatorColor = Gold.copy(alpha = 0.2f),
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render active tab body
            when (activeTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDay = { dayId ->
                        viewModel.selectDay(dayId)
                        activeTab = 1 // Switch to Days tab
                    },
                    onNavigateToTopic = { topicId ->
                        viewModel.selectTopic(topicId)
                        onNavigateToTopic(topicId)
                    },
                    onNavigateToDevotional = { activeTab = 2 },
                    onNavigateToBookmarks = { activeTab = 3 }
                )
                1 -> DaysScreen(
                    viewModel = viewModel,
                    onNavigateToTopic = { topicId ->
                        onNavigateToTopic(topicId)
                    }
                )
                2 -> DevotionalToolsScreen(viewModel = viewModel)
                3 -> BookmarksAndNotesScreen(
                    viewModel = viewModel,
                    onNavigateToTopic = { topicId ->
                        onNavigateToTopic(topicId)
                    }
                )
                4 -> SettingsScreen(viewModel = viewModel)
            }

            // Floating bottom audio overlay panel (persistent across tabs!)
            AudioPlayerPanel(viewModel = viewModel)
        }
    }
}
