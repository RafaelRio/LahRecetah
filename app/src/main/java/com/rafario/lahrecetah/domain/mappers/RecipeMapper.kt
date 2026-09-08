package com.rafario.lahrecetah.domain.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory

fun DocumentSnapshot.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        ingredients = getStringList("ingredients"),
        steps = getStringList("steps"),
        createdByUid = getString("createdByUid") ?: "",
        createdByName = getString("createdByName") ?: "",
        durationMinutes = getLong("durationMinutes")?.toInt() ?: 0,
        difficulty = getLong("difficulty")?.toInt() ?: 1,
        category = getString("category")
            ?.let { value ->
                runCatching {
                    RecipeCategory.valueOf(value)
                }.getOrDefault(RecipeCategory.OTHER)
            }
            ?: RecipeCategory.OTHER,
        imageUrl = getString("imageUrl") ?: ""
    )
}

private fun DocumentSnapshot.getStringList(field: String): List<String> {
    return (get(field) as? List<*>)
        ?.filterIsInstance<String>()
        .orEmpty()
}