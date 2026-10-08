package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import com.rafario.lahrecetah.domain.validation.RecipeValidator

sealed interface RecipeFormValidationError {
    data object DurationRequired : RecipeFormValidationError
    data object DurationNotInteger : RecipeFormValidationError

    data class Domain(
        val reason: RecipeValidationError
    ) : RecipeFormValidationError
}

sealed interface RecipeFormValidationResult {
    data class Valid(
        val durationMinutes: Int
    ) : RecipeFormValidationResult

    data class Invalid(
        val error: RecipeFormValidationError
    ) : RecipeFormValidationResult
}

object RecipeFormValidator {
    fun validate(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        durationText: String,
        difficulty: Int
    ): RecipeFormValidationResult {
        val durationMinutes = durationText.toIntOrNull()
        val domainError = RecipeValidator.validate(
            title,
            description,
            ingredients,
            steps,
            durationMinutes ?: 0,
            difficulty
        )

        if (domainError != null) {
            val error = if (
                domainError == RecipeValidationError.DURATION &&
                durationMinutes == null
            ) {
                if (durationText.isBlank()) {
                    RecipeFormValidationError.DurationRequired
                } else {
                    RecipeFormValidationError.DurationNotInteger
                }
            } else {
                RecipeFormValidationError.Domain(domainError)
            }
            return RecipeFormValidationResult.Invalid(error)
        }

        return RecipeFormValidationResult.Valid(requireNotNull(durationMinutes))
    }
}