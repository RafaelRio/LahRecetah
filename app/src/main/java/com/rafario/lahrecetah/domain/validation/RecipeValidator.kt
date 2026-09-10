package com.rafario.lahrecetah.domain.validation

enum class RecipeValidationError(val message: String) {
    TITLE("El título es obligatorio"),
    DESCRIPTION("La descripción es obligatoria"),
    INGREDIENTS("Añade al menos un ingrediente"),
    STEPS("Añade al menos un paso"),
    DURATION("La duración debe ser mayor que 0"),
    DIFFICULTY("La dificultad debe estar entre 1 y 5")
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
