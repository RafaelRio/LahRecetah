package com.rafario.lahrecetah.domain.validation

enum class RecipeValidationError {
    TITLE,
    DESCRIPTION,
    INGREDIENTS,
    STEPS,
    DURATION,
    DIFFICULTY
}

object RecipeValidator {
    fun validate(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        durationMinutes: Int,
        difficulty: Int
    ): RecipeValidationError? = when {
        title.isBlank() -> RecipeValidationError.TITLE
        description.isBlank() -> RecipeValidationError.DESCRIPTION
        ingredients.none { it.isNotBlank() } -> RecipeValidationError.INGREDIENTS
        steps.none { it.isNotBlank() } -> RecipeValidationError.STEPS
        durationMinutes <= 0 -> RecipeValidationError.DURATION
        difficulty !in 1..5 -> RecipeValidationError.DIFFICULTY
        else -> null
    }
}
