package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.domain.model.RecipeCategory

data class RecipeFormState(
    val title: String = "",
    val description: String = "",
    val durationText: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val difficulty: Int = 3,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val existingImageUrl: String? = null,
    val selectedLocalImageUri: String? = null,
    val focusedIngredientIndex: Int? = null,
    val focusedStepIndex: Int? = null
)
