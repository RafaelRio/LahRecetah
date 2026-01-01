package com.rafario.lahrecetah.data.remote.firestore

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rafario.lahrecetah.domain.mappers.toRecipe
import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeRecipes(): Flow<List<Recipe>> = callbackFlow {
        val listener = firestore.collection("recipes")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toRecipe()
                } ?: emptyList()

                trySend(recipes)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createRecipe(recipe: Recipe) {
        firestore.collection("recipes")
            .add(
                mapOf(
                    "title" to recipe.title,
                    "description" to recipe.description,
                    "ingredients" to recipe.ingredients,
                    "steps" to recipe.steps,
                    "durationMinutes" to recipe.durationMinutes,
                    "category" to recipe.category.name,
                    "difficulty" to recipe.difficulty,
                    "createdByUid" to recipe.createdByUid,
                    "createdByName" to recipe.createdByName,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}