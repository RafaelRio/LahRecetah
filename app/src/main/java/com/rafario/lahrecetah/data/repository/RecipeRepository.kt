package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.data.remote.firestore.RecipeFirestoreDataSource
import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val dataSource: RecipeFirestoreDataSource
) {

    fun observeRecipes(): Flow<List<Recipe>> {
        return dataSource.observeRecipes()
    }

    suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        return try {
            dataSource.createRecipe(recipe)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}