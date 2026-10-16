package com.rafario.lahrecetah.ui.recipe_form

import androidx.annotation.StringRes
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.validation.RecipeValidationError

@StringRes
fun RecipeFormUiError.toStringRes(): Int = when (this) {
    RecipeFormUiError.RecipeNotFound ->
        R.string.recipe_not_found

    RecipeFormUiError.ImageUploadFailed ->
        R.string.recipe_image_upload_failed

    RecipeFormUiError.PersistenceFailed ->
        R.string.recipe_persistence_failed

    RecipeFormUiError.Unexpected ->
        R.string.unexpected_error

    is RecipeFormUiError.Validation -> when (val validationError = error) {
        RecipeFormValidationError.DurationRequired ->
            R.string.recipe_validation_duration_required

        RecipeFormValidationError.DurationNotInteger ->
            R.string.recipe_validation_duration_not_integer

        is RecipeFormValidationError.Domain -> when (validationError.reason) {
            RecipeValidationError.TITLE ->
                R.string.recipe_validation_title

            RecipeValidationError.DESCRIPTION ->
                R.string.recipe_validation_description

            RecipeValidationError.INGREDIENTS ->
                R.string.recipe_validation_ingredients

            RecipeValidationError.STEPS ->
                R.string.recipe_validation_steps

            RecipeValidationError.DURATION ->
                R.string.recipe_validation_duration

            RecipeValidationError.DIFFICULTY ->
                R.string.recipe_validation_difficulty
        }
    }
}