package com.rafario.lahrecetah.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.ui.add_recipe.AddRecipeScreen
import com.rafario.lahrecetah.ui.profile.ProfileScreen
import com.rafario.lahrecetah.ui.recipe_list.RecipeListScreen
import com.rafario.lahrecetah.ui.theme.principalColor

@Composable
fun MainScreen(modifier: Modifier = Modifier, navHostController: NavHostController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            TabItem(0, Icons.AutoMirrored.Filled.MenuBook),
            TabItem(1, Icons.Default.Add),
            TabItem(2, Icons.Default.Person)
        )
    }

    Scaffold(bottomBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(principalColor)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                Icon(
                    imageVector = tab.icon,
                    contentDescription = "",
                    tint = if (selectedTab == tab.id) Color.White else Color.White.copy(
                        alpha = 0.5f
                    ),
                    modifier = Modifier.clickable { selectedTab = tab.id })
            }
        }
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> RecipeListScreen(navHostController = navHostController)
                1 -> AddRecipeScreen(navHostController = navHostController)
                2 -> ProfileScreen(navHostController = navHostController)
            }
        }
    }
}

data class TabItem(
    val id: Int, val icon: ImageVector
)