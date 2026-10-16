package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeFormErrorMapperTest {

    @Test
    fun `persistence errors map to their UI messages`() {
        assertEquals(
            R.string.recipe_not_found,
            RecipeFormUiError.RecipeNotFound.toStringRes()
        )
        assertEquals(
            R.string.recipe_image_upload_failed,
            RecipeFormUiError.ImageUploadFailed.toStringRes()
        )
        assertEquals(
            R.string.recipe_persistence_failed,
            RecipeFormUiError.PersistenceFailed.toStringRes()
        )
        assertEquals(
            R.string.unexpected_error,
            RecipeFormUiError.Unexpected.toStringRes()
        )
    }

    @Test
    fun `duration validation distinguishes missing and invalid input`() {
        assertEquals(
            R.string.recipe_validation_duration_required,
            RecipeFormUiError.Validation(
                RecipeFormValidationError.DurationRequired
            ).toStringRes()
        )
        assertEquals(
            R.string.recipe_validation_duration_not_integer,
            RecipeFormUiError.Validation(
                RecipeFormValidationError.DurationNotInteger
            ).toStringRes()
        )
    }

    @Test
    fun `domain validation errors map to the corresponding field messages`() {
        val expectedResources = mapOf(
            RecipeValidationError.TITLE to R.string.recipe_validation_title,
            RecipeValidationError.DESCRIPTION to R.string.recipe_validation_description,
            RecipeValidationError.INGREDIENTS to R.string.recipe_validation_ingredients,
            RecipeValidationError.STEPS to R.string.recipe_validation_steps,
            RecipeValidationError.DURATION to R.string.recipe_validation_duration,
            RecipeValidationError.DIFFICULTY to R.string.recipe_validation_difficulty
        )

        assertEquals(
            RecipeValidationError.entries.toSet(),
            expectedResources.keys
        )

        expectedResources.forEach { (reason, expectedResource) ->
            val error = RecipeFormUiError.Validation(
                RecipeFormValidationError.Domain(reason)
            )

            assertEquals(
                "Mensaje incorrecto para $reason",
                expectedResource,
                error.toStringRes()
            )
        }
    }
}