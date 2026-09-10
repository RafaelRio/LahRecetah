package com.rafario.lahrecetah.domain.validation

sealed interface RecipeValidationResult {

    data class Valid(
        val durationMinutes: Int
    ) : RecipeValidationResult

    data class Invalid(
        val message: String
    ) : RecipeValidationResult
}

object RecipeFormValidator {

    fun validate(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        durationText: String,
        difficulty: Int
    ): RecipeValidationResult {

        val durationMinutes = durationText.toIntOrNull()
        val error = RecipeValidator.validate(
            title, description, ingredients, steps, durationMinutes ?: 0, difficulty
        )
        if (error != null) {
            val message = if (error == RecipeValidationError.DURATION && durationMinutes == null) {
                if (durationText.isBlank()) "La duración es obligatoria"
                else "La duración debe ser un número entero"
            } else {
                error.message
            }
            return RecipeValidationResult.Invalid(message)
        }
        return RecipeValidationResult.Valid(requireNotNull(durationMinutes))
    }
}
