package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class EditRecipeViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `load recipe populates form and marks it ready`() = runTest {
        val recipe = Recipe(
            id = "recipe-123",
            title = "Tortilla de patatas",
            durationMinutes = 30,
            imageUrl = "https://example.com/tortilla.jpg"
        )
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)

        assertTrue(viewModel.isLoading.value)
        assertFalse(viewModel.isReady.value)

        advanceUntilIdle()

        assertEquals(recipe.id, repository.requestedRecipeId)
        assertEquals(recipe.title, viewModel.formState.value.title)
        assertEquals("30", viewModel.formState.value.durationText)
        assertEquals(recipe.imageUrl, viewModel.formState.value.existingImageUrl)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `missing recipe emits not found and leaves form unready`() = runTest {
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(null)
        }
        val viewModel = EditRecipeViewModel(repository)
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.loadRecipe("missing-recipe")
        advanceUntilIdle()

        assertEquals(
            EditRecipeEvent.Error(RecipeFormUiError.RecipeNotFound),
            event.await()
        )
        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `read failure emits unexpected error and leaves form unready`() = runTest {
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.failure(
                IllegalStateException("read failed")
            )
        }
        val viewModel = EditRecipeViewModel(repository)
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.loadRecipe("recipe-123")
        advanceUntilIdle()

        assertEquals(
            EditRecipeEvent.Error(RecipeFormUiError.Unexpected),
            event.await()
        )
        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `load cancellation wrapped in result propagates without UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.failure(
                CancellationException("read cancelled")
            )
        }
        val viewModel = EditRecipeViewModel(repository)
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.loadRecipe("recipe-123")
        val loadJob = viewModel.viewModelScope.coroutineContext.job.children.single()

        advanceUntilIdle()

        assertTrue(loadJob.isCancelled)
        assertTrue(loadJob.isCompleted)
        assertTrue(events.isEmpty())
        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }
}