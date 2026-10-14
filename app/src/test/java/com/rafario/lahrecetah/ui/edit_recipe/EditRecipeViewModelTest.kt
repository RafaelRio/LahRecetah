package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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

    @Test
    fun `older response does not overwrite latest recipe`() = runTest {
        val firstRecipe = Recipe(id = "first", title = "Primera receta")
        val latestRecipe = Recipe(id = "latest", title = "Última receta")
        val firstResponse = CompletableDeferred<Result<Recipe?>>()

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { recipeId ->
                when (recipeId) {
                    firstRecipe.id -> withContext(NonCancellable) {
                        firstResponse.await()
                    }
                    latestRecipe.id -> Result.success(latestRecipe)
                    else -> error("Unexpected recipe ID")
                }
            }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(firstRecipe.id)
        val firstJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        runCurrent()

        try {
            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            assertEquals(latestRecipe.title, viewModel.formState.value.title)
            assertTrue(viewModel.isReady.value)
            assertFalse(viewModel.isLoading.value)
        } finally {
            firstResponse.complete(Result.success(firstRecipe))
            advanceUntilIdle()
        }

        assertTrue(firstJob.isCancelled)
        assertTrue(firstJob.isCompleted)
        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `older completion does not stop latest loading`() = runTest {
        val firstRecipe = Recipe(id = "first", title = "Primera receta")
        val latestRecipe = Recipe(id = "latest", title = "Última receta")
        val firstResponse = CompletableDeferred<Result<Recipe?>>()
        val latestResponse = CompletableDeferred<Result<Recipe?>>()

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { recipeId ->
                when (recipeId) {
                    firstRecipe.id -> withContext(NonCancellable) {
                        firstResponse.await()
                    }
                    latestRecipe.id -> latestResponse.await()
                    else -> error("Unexpected recipe ID")
                }
            }
        }
        val viewModel = EditRecipeViewModel(repository)

        try {
            viewModel.loadRecipe(firstRecipe.id)
            runCurrent()

            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            firstResponse.complete(Result.success(firstRecipe))
            runCurrent()

            assertEquals(RecipeFormState(), viewModel.formState.value)
            assertFalse(viewModel.isReady.value)
            assertTrue(viewModel.isLoading.value)
        } finally {
            firstResponse.complete(Result.success(firstRecipe))
            latestResponse.complete(Result.success(latestRecipe))
            advanceUntilIdle()
        }

        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `older read failure does not emit UI error`() = runTest {
        val latestRecipe = Recipe(id = "latest", title = "Última receta")
        val firstResponse = CompletableDeferred<Result<Recipe?>>()

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { recipeId ->
                when (recipeId) {
                    "first" -> withContext(NonCancellable) {
                        firstResponse.await()
                    }
                    latestRecipe.id -> Result.success(latestRecipe)
                    else -> error("Unexpected recipe ID")
                }
            }
        }
        val viewModel = EditRecipeViewModel(repository)
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.loadRecipe("first")
            runCurrent()

            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            assertEquals(latestRecipe.title, viewModel.formState.value.title)
            assertTrue(viewModel.isReady.value)
        } finally {
            firstResponse.complete(
                Result.failure(IllegalStateException("old read failed"))
            )
            advanceUntilIdle()
        }

        assertTrue(events.isEmpty())
        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `older missing recipe does not emit UI error`() = runTest {
        val latestRecipe = Recipe(id = "latest", title = "Última receta")
        val firstResponse = CompletableDeferred<Result<Recipe?>>()

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { recipeId ->
                when (recipeId) {
                    "first" -> withContext(NonCancellable) {
                        firstResponse.await()
                    }
                    latestRecipe.id -> Result.success(latestRecipe)
                    else -> error("Unexpected recipe ID")
                }
            }
        }
        val viewModel = EditRecipeViewModel(repository)
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.loadRecipe("first")
            runCurrent()

            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            assertEquals(latestRecipe.title, viewModel.formState.value.title)
            assertTrue(viewModel.isReady.value)
        } finally {
            firstResponse.complete(Result.success(null))
            advanceUntilIdle()
        }

        assertTrue(events.isEmpty())
        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loading another recipe discards previous draft and local image`() = runTest {
        val firstRecipe = Recipe(
            id = "first",
            title = "Primera receta",
            imageUrl = "https://example.com/first.jpg"
        )
        val secondRecipe = Recipe(
            id = "second",
            title = "Segunda receta",
            durationMinutes = 25,
            imageUrl = "https://example.com/second.jpg"
        )
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(firstRecipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(firstRecipe.id)
        advanceUntilIdle()
        val originalState = viewModel.formState.value

        viewModel.onTitleChanged("Título editado")
        viewModel.onImageSelected("content://recipes/draft")

        assertEquals(
            originalState.copy(
                title = "Título editado",
                selectedLocalImageUri = "content://recipes/draft"
            ),
            viewModel.formState.value
        )

        repository.getRecipeByIdResult = Result.success(secondRecipe)
        viewModel.loadRecipe(secondRecipe.id)

        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isReady.value)
        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertEquals(
            RecipeFormState(
                title = secondRecipe.title,
                durationText = "25",
                difficulty = 1,
                existingImageUrl = secondRecipe.imageUrl
            ),
            viewModel.formState.value
        )
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }
}