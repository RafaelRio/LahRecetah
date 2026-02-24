package com.rafario.lahrecetah.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.ui.add_recipe.AddRecipeScreen
import com.rafario.lahrecetah.ui.profile.ProfileScreen
import com.rafario.lahrecetah.ui.recipe_list.RecipeListScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val cs = MaterialTheme.colorScheme
// ✅ NUEVO: recipeId que se va a editar (si es null, modo crear)
    var editingRecipeId by remember { mutableStateOf<String?>(null) }
    val tabs = remember {
        listOf(
            TabItem(0, Icons.AutoMirrored.Filled.MenuBook),
            TabItem(1, Icons.Default.Add),
            TabItem(2, Icons.Default.Person)
        )
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                HorizontalDivider(color = cs.onSurface.copy(alpha = 0.08f))
                NavigationBar(
                    containerColor = cs.surfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val selected = selectedTab == tab.id
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                selectedTab = tab.id
                                if (tab.id != 1) editingRecipeId = null
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.primary,
                                unselectedIconColor = cs.onSurfaceVariant.copy(alpha = 0.65f),
                                indicatorColor = cs.primary.copy(alpha = 0.20f)
                            )
                        )
                    }
                }
            }

        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> RecipeListScreen(navHostController = navHostController)
                1 -> AddRecipeScreen(navHostController = navHostController, editingRecipeId = editingRecipeId, onEditFinished = {
                    // ✅ al terminar edición, vuelves a Perfil (tab 3) y limpias estado
                    editingRecipeId = null
                    selectedTab = 2
                })
                2 -> ProfileScreen(
                    navHostController = navHostController,
                    onEditRecipe = { recipeId ->
                        // ✅ click desde perfil: set id y saltar a tab "Add"
                        editingRecipeId = recipeId
                        selectedTab = 1
                    }
                )
            }
        }
    }
}

data class TabItem(
    val id: Int,
    val icon: ImageVector
)