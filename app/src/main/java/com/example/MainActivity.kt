package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BlogViewModel
import com.example.ui.viewmodel.BlogViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Read default system dark theme preference
            val systemDarkTheme = isSystemInDarkTheme()
            var darkThemeEnabled by remember { mutableStateOf(systemDarkTheme) }

            MyApplicationTheme(darkTheme = darkThemeEnabled) {
                // Initialize BlogViewModel
                val blogApplication = application as BlogApplication
                val blogViewModel: BlogViewModel = viewModel(
                    factory = BlogViewModelFactory(blogApplication.repository)
                )

                BlogAppShell(
                    viewModel = blogViewModel,
                    darkTheme = darkThemeEnabled,
                    onToggleDarkTheme = { darkThemeEnabled = !darkThemeEnabled }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogAppShell(
    viewModel: BlogViewModel,
    darkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Verify whether the current screen is a full-reading post screen
    val isPostScreen = currentRoute?.startsWith("post/") == true

    Scaffold(
        topBar = {
            if (!isPostScreen) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BlogSphere",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Insights & Tech Perspectives",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleDarkTheme) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle color scheme theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            // Hide bottom tabs on the individual article reading screen to give maximum display layout spacing
            if (!isPostScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home" || currentRoute == null,
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home index"
                            )
                        },
                        label = { Text("Feed") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "explore",
                        onClick = {
                            if (currentRoute != "explore") {
                                navController.navigate("explore") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "explore") Icons.Default.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore index"
                            )
                        },
                        label = { Text("Explore") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "about",
                        onClick = {
                            if (currentRoute != "about") {
                                navController.navigate("about") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "about") Icons.Default.Info else Icons.Outlined.Info,
                                contentDescription = "About index"
                            )
                        },
                        label = { Text("Board") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "admin",
                        onClick = {
                            if (currentRoute != "admin") {
                                navController.navigate("admin") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == "admin") Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "Admin suite"
                            )
                        },
                        label = { Text("Admin") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(
                bottom = if (isPostScreen) 0.dp else innerPadding.calculateBottomPadding(),
                top = if (isPostScreen) 0.dp else innerPadding.calculateTopPadding()
            )
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToArticle = { id -> navController.navigate("post/$id") },
                    onNavigateToExplore = { navController.navigate("explore") }
                )
            }

            composable("explore") {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToArticle = { id -> navController.navigate("post/$id") }
                )
            }

            composable("about") {
                AboutContactScreen()
            }

            composable("admin") {
                AdminScreen(viewModel = viewModel)
            }

            composable(
                route = "post/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("id") ?: 0
                PostScreen(
                    id = articleId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToArticle = { nextId ->
                        // Switch reading view seamlessly to related item
                        navController.navigate("post/$nextId") {
                            popUpTo("home")
                        }
                    }
                )
            }
        }
    }
}
