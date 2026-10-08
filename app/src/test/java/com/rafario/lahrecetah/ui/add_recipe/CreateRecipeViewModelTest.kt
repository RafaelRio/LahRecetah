package com.rafario.lahrecetah.ui.add_recipe

import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
class CreateRecipeViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `invalid form emits typed validation error without persisting`() = runTest {
        val repository = FakeRecipeRepository()
        val viewModel = createViewModel(repository)
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(
            CreateRecipeEvent.Error(
                RecipeFormUiError.Validation(
                    RecipeFormValidationError.Domain(RecipeValidationError.TITLE)
                )
            ),
            event.await()
        )
        assertTrue(repository.created.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `valid form without image creates recipe and resets form`() = runTest {
        val repository = FakeRecipeRepository()
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(CreateRecipeEvent.Created, event.await())
        assertEquals("", repository.created.single().imageUrl)
        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `create failure emits persistence error and preserves form`() = runTest {
        val repository = FakeRecipeRepository().apply {
            createResult = Result.failure(IllegalStateException("write failed"))
        }
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        val stateBeforeCreate = viewModel.formState.value
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(
            CreateRecipeEvent.Error(RecipeFormUiError.PersistenceFailed),
            event.await()
        )
        assertEquals(stateBeforeCreate, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cancellation is propagated without emitting a UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            beforeCreate = { throw CancellationException("cancel") }
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)

        viewModel.createRecipe()
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cancellation wrapped in result is not converted to UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            createResult = Result.failure(CancellationException("cancel"))
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)

        viewModel.createRecipe()
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    private fun createViewModel(repository: FakeRecipeRepository) =
        CreateRecipeViewModel(
            CreateRecipeUseCase(repository, FakeAuthRepository())
        )

    private fun fillValidForm(viewModel: CreateRecipeViewModel) {
        viewModel.onTitleChanged("Tortilla")
        viewModel.onDescriptionChanged("Casera")
        viewModel.onDurationChanged("15")
        viewModel.addIngredientRow()
        viewModel.updateIngredient(0, "Huevos")
        viewModel.addStepRow()
        viewModel.updateStep(0, "Batir")
    }
}