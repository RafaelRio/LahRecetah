package com.rafario.lahrecetah.domain.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory

fun DocumentSnapshot.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        ingredients = get("ingredients") as? List<String> ?: emptyList(),
        steps = get("steps") as? List<String> ?: emptyList(),
        createdByUid = getString("createdByUid") ?: "",
        createdByName = getString("createdByName") ?: "",
        durationMinutes = getLong("durationMinutes")?.toInt() ?: 0,
        difficulty = getLong("difficulty")?.toInt() ?: 1,
        category = getString("category")
            ?.let { value ->
                runCatching { RecipeCategory.valueOf(value) }
                    .getOrDefault(RecipeCategory.OTHER)
            }
            ?: RecipeCategory.OTHER,
    )
}

fun RecipeCategory.toUiText(): String = when (this) {
    RecipeCategory.STARTER -> "Entrante"
    RecipeCategory.FIRST_COURSE -> "Primer plato"
    RecipeCategory.MAIN_COURSE -> "Segundo plato"
    RecipeCategory.DESSERT -> "Postre"
    RecipeCategory.SWEET -> "Dulce"
    RecipeCategory.SALAD -> "Ensalada"
    RecipeCategory.SOUP -> "Sopa"
    RecipeCategory.DRINK -> "Bebida"
    RecipeCategory.OTHER -> "Otros"
}