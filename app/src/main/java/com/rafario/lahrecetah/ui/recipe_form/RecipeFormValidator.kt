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
        if (durationText.isBlank()) {
            return RecipeFormValidationResult.Invalid(
                RecipeFormValidationError.DurationRequired
            )
        }

        val durationMinutes = durationText.toIntOrNull()
            ?: return RecipeFormValidationResult.Invalid(
                RecipeFormValidationError.DurationNotInteger
            )

        val domainError = RecipeValidator.validate(
            title,
            description,
            ingredients,
            steps,
            durationMinutes,
            difficulty
        )

        if (domainError != null) {
            return RecipeFormValidationResult.Invalid(
                RecipeFormValidationError.Domain(domainError)
            )
        }

        return RecipeFormValidationResult.Valid(durationMinutes)
    }
}