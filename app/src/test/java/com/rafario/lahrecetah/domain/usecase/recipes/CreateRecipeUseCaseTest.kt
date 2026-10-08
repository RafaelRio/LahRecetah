package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import com.rafario.lahrecetah.domain.validation.RecipeValidationException
import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CreateRecipeUseCaseTest {
    private val auth = FakeAuthRepository()
    private val repository = FakeRecipeRepository()
    private val useCase = CreateRecipeUseCase(repository, auth)
    private val valid = Recipe(
        title = "Tortilla", description = "Casera", ingredients = listOf("Huevos"),
        steps = listOf("Batir"), durationMinutes = 15, difficulty = 2
    )

    private suspend fun create(recipe: Recipe = valid) = useCase(
        recipe.title, recipe.description, recipe.ingredients, recipe.steps,
        recipe.category, recipe.durationMinutes, recipe.difficulty, recipe.imageUrl
    )

    @Test
    fun `requires authenticated user`() = runTest {
        auth.user = null
        assertEquals("Usuario no autenticado", create().exceptionOrNull()?.message)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `rejects every invalid business field before persistence`() = runTest {
        val invalid = listOf(
            valid.copy(title = " "), valid.copy(description = ""),
            valid.copy(ingredients = emptyList()), valid.copy(ingredients = listOf(" ", "")),
            valid.copy(steps = emptyList()), valid.copy(steps = listOf(" ", "")),
            valid.copy(durationMinutes = 0), valid.copy(durationMinutes = -1),
            valid.copy(difficulty = 0), valid.copy(difficulty = 6)
        )
        val titleFailure = create(valid.copy(title = " "))
            .exceptionOrNull() as RecipeValidationException
        assertEquals(RecipeValidationError.TITLE, titleFailure.reason)
        invalid.forEach { assertTrue(create(it).exceptionOrNull() is RecipeValidationException) }
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `uses authenticated identity and normalizes without changing order or metadata`() =
        runTest {
            assertTrue(
                create(
                    valid.copy(
                        title = "  Tortilla  ", description = " Casera ",
                        ingredients = listOf(" Huevos ", "", " Sal ", " "),
                        steps = listOf(" Batir ", " ", " Freír "),
                        category = RecipeCategory.OTHER, imageUrl = "https://example.com/image.jpg",
                        createdByUid = "untrusted"
                    )
                ).isSuccess
            )
            val recipe = repository.created.single()
            assertEquals(auth.user?.uid, recipe.createdByUid)
            assertEquals(auth.user?.displayName, recipe.createdByName)
            assertEquals("Tortilla", recipe.title)
            assertEquals("Casera", recipe.description)
            assertEquals(listOf("Huevos", "Sal"), recipe.ingredients)
            assertEquals(listOf("Batir", "Freír"), recipe.steps)
            assertEquals(15, recipe.durationMinutes)
            assertEquals(2, recipe.difficulty)
            assertEquals("https://example.com/image.jpg", recipe.imageUrl)
        }

    @Test
    fun `preserves repository failure`() = runTest {
        val failure = IllegalStateException("write failed")
        repository.createResult = Result.failure(failure)
        assertSame(failure, create().exceptionOrNull())
    }

    @Test
    fun `propagates cancellation`() = runTest {
        val cancellation = CancellationException("cancel")
        repository.beforeCreate = { throw cancellation }
        try {
            create()
            fail("Expected cancellation")
        } catch (e: CancellationException) {
            assertSame(cancellation, e)
        }
    }
}
