package com.rafario.lahrecetah.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        assertTrue(result is RecipeValidationResult.Valid)

        val validResult = result as RecipeValidationResult.Valid
        assertEquals(45, validResult.durationMinutes)
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
            expectedMessage = "El título es obligatorio"
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
            expectedMessage = "La descripción es obligatoria"
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
            expectedMessage = "Añade al menos un ingrediente"
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
            expectedMessage = "Añade al menos un ingrediente"
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
            expectedMessage = "Añade al menos un paso"
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
            expectedMessage = "Añade al menos un paso"
        )
    }

    @Test
    fun `blank duration returns invalid result`() {
        val result = validRecipe(
            durationText = ""
        )

        assertInvalid(
            result = result,
            expectedMessage = "La duración es obligatoria"
        )
    }

    @Test
    fun `non numeric duration returns invalid result`() {
        val result = validRecipe(
            durationText = "abc"
        )

        assertInvalid(
            result = result,
            expectedMessage = "La duración debe ser un número entero"
        )
    }

    @Test
    fun `zero duration returns invalid result`() {
        val result = validRecipe(
            durationText = "0"
        )

        assertInvalid(
            result = result,
            expectedMessage = "La duración debe ser mayor que 0"
        )
    }

    @Test
    fun `negative duration returns invalid result`() {
        val result = validRecipe(
            durationText = "-10"
        )

        assertInvalid(
            result = result,
            expectedMessage = "La duración debe ser mayor que 0"
        )
    }

    @Test
    fun `difficulty below minimum returns invalid result`() {
        val result = validRecipe(
            difficulty = 0
        )

        assertInvalid(
            result = result,
            expectedMessage = "La dificultad debe estar entre 1 y 5"
        )
    }

    @Test
    fun `difficulty above maximum returns invalid result`() {
        val result = validRecipe(
            difficulty = 6
        )

        assertInvalid(
            result = result,
            expectedMessage = "La dificultad debe estar entre 1 y 5"
        )
    }

    private fun validRecipe(
        durationText: String = "30",
        difficulty: Int = 3
    ): RecipeValidationResult {
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
        result: RecipeValidationResult,
        expectedMessage: String
    ) {
        assertTrue(result is RecipeValidationResult.Invalid)

        val invalidResult = result as RecipeValidationResult.Invalid
        assertEquals(expectedMessage, invalidResult.message)
    }
}