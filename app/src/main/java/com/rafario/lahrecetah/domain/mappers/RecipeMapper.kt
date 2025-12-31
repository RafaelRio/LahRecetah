package com.rafario.lahrecetah.domain.mappers

import com.google.firebase.firestore.DocumentSnapshot
import com.rafario.lahrecetah.domain.model.Recipe

fun DocumentSnapshot.toRecipe(): Recipe {
    return Recipe(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        ingredients = get("ingredients") as? List<String> ?: emptyList(),
        steps = get("steps") as? List<String> ?: emptyList(),
        createdByUid = getString("createdByUid") ?: "",
        createdByName = getString("createdByName") ?: ""
    )
}