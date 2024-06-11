package com.halidodat.neuralnetworkvisualizer

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val action: (index: Int, navController: NavHostController) -> Unit,
)