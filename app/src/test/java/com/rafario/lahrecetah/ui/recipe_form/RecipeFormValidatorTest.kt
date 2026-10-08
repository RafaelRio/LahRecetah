package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import org.junit.Assert
import org.junit.Test

class RecipeFormValidatorTest {

    @Test
    fun `valid recipe returns valid result`() {
        val result = RecipeFormValidator.validate(
            title = "Tortilla de patatas",
            description = "Una tortilla tradicional",
            ingredients = listOf("Patatas", "Huevos"),
            steps = listOf("Cortar las patatas", "Freír", "Añadir los huevos"),
            durationText = "45",
            difficulty = 3
        )

        Assert.assertTrue(result is RecipeFormValidationResult.Valid)

        val validResult = result as RecipeFormValidationResult.Valid
        Assert.assertEquals(45, validResult.durationMinutes)
    }

    @Test
    fun `blank title returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "   ",
            description = "Descripción",
            ingredients = listOf("Ingrediente"),
            steps = listOf("Paso"),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.TITLE)
        )
    }

    @Test
    fun `blank description returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "Receta",
            description = "   ",
            ingredients = listOf("Ingrediente"),
            steps = listOf("Paso"),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.DESCRIPTION)
        )
    }

    @Test
    fun `empty ingredients returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "Receta",
            description = "Descripción",
            ingredients = emptyList(),
            steps = listOf("Paso"),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.INGREDIENTS)
        )
    }

    @Test
    fun `ingredients containing only spaces returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "Receta",
            description = "Descripción",
            ingredients = listOf("   ", ""),
            steps = listOf("Paso"),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.INGREDIENTS)
        )
    }

    @Test
    fun `empty steps returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "Receta",
            description = "Descripción",
            ingredients = listOf("Ingrediente"),
            steps = emptyList(),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.STEPS)
        )
    }

    @Test
    fun `steps containing only spaces returns invalid result`() {
        val result = RecipeFormValidator.validate(
            title = "Receta",
            description = "Descripción",
            ingredients = listOf("Ingrediente"),
            steps = listOf("   ", ""),
            durationText = "20",
            difficulty = 3
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.STEPS)
        )
    }

    @Test
    fun `blank duration returns invalid result`() {
        val result = validRecipe(
            durationText = ""
        )

        assertInvalid(
            result = result, expectedError = RecipeFormValidationError.DurationRequired
        )
    }

    @Test
    fun `non numeric duration returns invalid result`() {
        val result = validRecipe(
            durationText = "abc"
        )

        assertInvalid(
            result = result, expectedError = RecipeFormValidationError.DurationNotInteger
        )
    }

    @Test
    fun `zero duration returns invalid result`() {
        val result = validRecipe(
            durationText = "0"
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.DURATION)
        )
    }

    @Test
    fun `negative duration returns invalid result`() {
        val result = validRecipe(
            durationText = "-10"
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.DURATION)
        )
    }

    @Test
    fun `difficulty below minimum returns invalid result`() {
        val result = validRecipe(
            difficulty = 0
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.DIFFICULTY)
        )
    }

    @Test
    fun `difficulty above maximum returns invalid result`() {
        val result = validRecipe(
            difficulty = 6
        )

        assertInvalid(
            result = result,
            expectedError = RecipeFormValidationError.Domain(RecipeValidationError.DIFFICULTY)
        )
    }

    private fun validRecipe(
        durationText: String = "30", difficulty: Int = 3
    ): RecipeFormValidationResult {
        return RecipeFormValidator.validate(
            title = "Receta válida",
            description = "Descripción válida",
            ingredients = listOf("Ingrediente"),
            steps = listOf("Paso"),
            durationText = durationText,
            difficulty = difficulty
        )
    }

    private fun assertInvalid(
        result: RecipeFormValidationResult, expectedError: RecipeFormValidationError
    ) {
        Assert.assertTrue(result is RecipeFormValidationResult.Invalid)

        val invalidResult = result as RecipeFormValidationResult.Invalid
        Assert.assertEquals(expectedError, invalidResult.error)
    }
}