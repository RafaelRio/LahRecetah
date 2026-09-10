package com.rafario.lahrecetah.domain.repository

import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun observeRecipes(): Flow<List<Recipe>>
    fun observeRecipeById(recipeId: String): Flow<Recipe?>
    fun observeRecipesByUser(uid: String): Flow<List<Recipe>>
    suspend fun createRecipe(recipe: Recipe): Result<Unit>
    suspend fun updateRecipe(recipe: Recipe): Result<Unit>
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun uploadRecipeImage(uri: String): String
}
