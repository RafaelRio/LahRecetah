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

        if (title.isBlank()) {
            return RecipeValidationResult.Invalid(
                "El título es obligatorio"
            )
        }

        if (description.isBlank()) {
            return RecipeValidationResult.Invalid(
                "La descripción es obligatoria"
            )
        }

        if (ingredients.none { it.isNotBlank() }) {
            return RecipeValidationResult.Invalid(
                "Añade al menos un ingrediente"
            )
        }

        if (steps.none { it.isNotBlank() }) {
            return RecipeValidationResult.Invalid(
                "Añade al menos un paso"
            )
        }

        if (durationText.isBlank()) {
            return RecipeValidationResult.Invalid(
                "La duración es obligatoria"
            )
        }

        val durationMinutes = durationText.toIntOrNull()
            ?: return RecipeValidationResult.Invalid(
                "La duración debe ser un número entero"
            )

        if (durationMinutes <= 0) {
            return RecipeValidationResult.Invalid(
                "La duración debe ser mayor que 0"
            )
        }

        if (difficulty !in 1..5) {
            return RecipeValidationResult.Invalid(
                "La dificultad debe estar entre 1 y 5"
            )
        }

        return RecipeValidationResult.Valid(
            durationMinutes = durationMinutes
        )
    }
}