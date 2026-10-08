package com.rafario.lahrecetah.ui.recipe_form

sealed interface RecipeFormUiError {
    data class Validation(
        val error: RecipeFormValidationError
    ) : RecipeFormUiError

    data object RecipeNotFound : RecipeFormUiError
    data object ImageUploadFailed : RecipeFormUiError
    data object PersistenceFailed : RecipeFormUiError
    data object Unexpected : RecipeFormUiError
}