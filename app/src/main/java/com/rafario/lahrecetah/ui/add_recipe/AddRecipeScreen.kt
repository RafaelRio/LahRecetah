package com.rafario.lahrecetah.ui.add_recipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafario.lahrecetah.ui.edit_recipe.EditRecipeRoute

@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    editingRecipeId: String?,
    onEditFinished: () -> Unit
) {
    if (editingRecipeId.isNullOrBlank()) {
        CreateRecipeRoute(
            modifier = modifier
        )
    } else {
        EditRecipeRoute(
            recipeId = editingRecipeId,
            onEditFinished = onEditFinished,
            modifier = modifier
        )
    }
}
