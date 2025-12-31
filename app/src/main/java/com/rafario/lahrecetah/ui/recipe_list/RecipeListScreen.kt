package com.rafario.lahrecetah.ui.recipe_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.domain.model.Recipe

@Composable
fun RecipeListScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is RecipeListUiState.Loading -> {
            CircularProgressIndicator()
        }

        is RecipeListUiState.Error -> {
            Text(
                text = (uiState as RecipeListUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        is RecipeListUiState.Success -> {
            val recipes = (uiState as RecipeListUiState.Success).recipes

            LazyColumn {
                items(recipes) { recipe ->
                    RecipeItem(recipe) {

                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Por ${recipe.createdByName}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}