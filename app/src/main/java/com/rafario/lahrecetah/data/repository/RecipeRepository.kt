package com.rafario.lahrecetah.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.rafario.lahrecetah.data.remote.firestore.RecipeFirestoreDataSource
import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RecipeRepository @Inject constructor(
    private val dataSource: RecipeFirestoreDataSource,
    private val storage: FirebaseStorage
) {

    fun observeRecipes(): Flow<List<Recipe>> {
        return dataSource.observeRecipes()
    }

    suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        return try {
            dataSource.createRecipe(recipe)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            val recipe = dataSource
                .observeRecipeById(recipeId)
                .first()

            val imageUrl = recipe?.imageUrl.orEmpty()

            dataSource.deleteRecipe(recipeId)

            if (imageUrl.isNotBlank()) {
                deleteRecipeImage(imageUrl)
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val currentRecipe = dataSource
                .observeRecipeById(recipe.id)
                .first()

            val oldImageUrl = currentRecipe?.imageUrl.orEmpty()

            dataSource.updateRecipe(recipe)

            val imageChanged =
                oldImageUrl.isNotBlank() &&
                        oldImageUrl != recipe.imageUrl

            if (imageChanged) {
                deleteRecipeImage(oldImageUrl)
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRecipeById(recipeId: String): Flow<Recipe?> {
        return dataSource.observeRecipeById(recipeId)
    }

    fun observeRecipesByUser(uid: String): Flow<List<Recipe>> {
        return dataSource.observeRecipesByUser(uid)
    }

    private suspend fun deleteRecipeImage(imageUrl: String) {
        try {
            storage
                .getReferenceFromUrl(imageUrl)
                .delete()
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // La receta ya se ha eliminado de Firestore.
            // Un fallo limpiando Storage no debe hacer fallar el borrado completo.
        }
    }

    suspend fun uploadRecipeImage(uri: Uri): String {
        val reference = storage.reference
            .child("recipes")
            .child("${java.util.UUID.randomUUID()}.jpg")

        reference.putFile(uri).await()

        return reference.downloadUrl
            .await()
            .toString()
    }
}