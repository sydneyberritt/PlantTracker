package com.example.planttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.planttracker.ui.theme.PlantTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantTrackerTheme {
                PlantTracker()
            }
        }
    }
}

@Composable
fun PlantTracker() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    //screens in the navbar
    val mainScreens = listOf("home", "addPlant", "trending")
    val showNavBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "home",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Plant") },
                        label = { Text(stringResource(R.string.add_new_plant), fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "addPlant",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            navController.navigate("addPlant") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Whatshot, contentDescription = "Trending") },
                        label = { Text(stringResource(R.string.trending_title), fontWeight = FontWeight.Bold) },
                        selected = currentRoute == "trending",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            navController.navigate("trending") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home")
                    },
                    onSignUp = {
                        navController.navigate("signup")
                    }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("login")
                    }
                )
            }
            composable("home") {
                HomeScreen()
            }
            composable("addPlant") {
                SearchPlantScreen(
                    onPlantSelection = {
                        navController.navigate("plantDetails")
                    }
                )
            }
            composable("trending") {
                TrendingPlantScreen(
                    onPlantSelection = {
                        navController.navigate("plantDetails")
                    }
                )
            }
            composable("plantDetails") {
                PlantDetailsScreen(
                    onPlantAdded = {
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            }
        }
    }
}
