package com.rafario.lahrecetah.data.repository

import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.rafario.lahrecetah.data.remote.firestore.RecipeFirestoreDataSource
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.recipe.RecipeNotFoundException
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class FirebaseRecipeRepository @Inject constructor(
    private val dataSource: RecipeFirestoreDataSource,
    private val storage: FirebaseStorage
) : RecipeRepository {

    override fun observeRecipes(): Flow<List<Recipe>> {
        return dataSource.observeRecipes()
    }

    override suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        return try {
            dataSource.createRecipe(recipe)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            val recipe = dataSource.getRecipeById(recipeId)

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

    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val currentRecipe = dataSource.getRecipeById(recipe.id)
                ?: return Result.failure(RecipeNotFoundException())

            val oldImageUrl = currentRecipe.imageUrl

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

    override fun observeRecipeById(recipeId: String): Flow<Recipe?> {
        return dataSource.observeRecipeById(recipeId)
    }

    override suspend fun getRecipeById(recipeId: String): Result<Recipe?> {
        return try {
            Result.success(dataSource.getRecipeById(recipeId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRecipesByUser(uid: String): Flow<List<Recipe>> {
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

    override suspend fun uploadRecipeImage(uri: String): String {
        val reference = storage.reference
            .child("recipes")
            .child("${java.util.UUID.randomUUID()}.jpg")

        reference.putFile(uri.toUri()).await()

        return reference.downloadUrl
            .await()
            .toString()
    }
}