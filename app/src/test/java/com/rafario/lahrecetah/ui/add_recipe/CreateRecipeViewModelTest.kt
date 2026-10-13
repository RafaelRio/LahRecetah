package com.rafario.lahrecetah.ui.add_recipe

import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import com.rafario.lahrecetah.testing.FakeAuthRepository
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import org.junit.Assert.assertSame

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
            CreateRecipeEvent.Error(RecipeFormUiError.Validation(RecipeFormValidationError.DurationRequired)),
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
    fun `create cancellation after upload cleans image without UI error`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            beforeCreate = { throw CancellationException("cancel") }
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `create cancellation wrapped in result cleans image without UI error`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            createResult = Result.failure(CancellationException("cancel"))
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `uploaded image url is persisted with the recipe`() = runTest {
        val uri = "content://recipes/image"
        val imageUrl = "https://storage.example/image.jpg"
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
        }
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onImageSelected(uri)
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(CreateRecipeEvent.Created, event.await())
        assertEquals(uri, repository.uploadedImageUri)
        assertEquals(imageUrl, repository.created.single().imageUrl)
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(RecipeFormState(), viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `upload failure emits typed error without persisting`() = runTest {
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.failure(IllegalStateException("upload failed"))
        }
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(
            CreateRecipeEvent.Error(RecipeFormUiError.ImageUploadFailed),
            event.await()
        )
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `upload cancellation wrapped in result is not converted to UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.failure(CancellationException("cancel"))
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")

        viewModel.createRecipe()
        advanceUntilIdle()

        assertTrue(events.isEmpty())
        assertTrue(repository.created.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `create failure after upload cleans image and preserves form`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            createResult = Result.failure(IllegalStateException("write failed"))
        }
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")
        val stateBeforeCreate = viewModel.formState.value

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(
            CreateRecipeEvent.Error(RecipeFormUiError.PersistenceFailed),
            event.await()
        )
        assertEquals(imageUrl, repository.created.single().imageUrl)
        assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
        assertEquals(stateBeforeCreate, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cleanup failure preserves persistence error and form`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            createResult = Result.failure(IllegalStateException("write failed"))
            deleteRecipeImageResult =
                Result.failure(IllegalStateException("delete failed"))
        }
        val viewModel = createViewModel(repository)
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")
        val stateBeforeCreate = viewModel.formState.value

        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.createRecipe()
        advanceUntilIdle()

        assertEquals(
            listOf(CreateRecipeEvent.Error(RecipeFormUiError.PersistenceFailed)),
            events
        )
        assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
        assertEquals(stateBeforeCreate, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cancelled creation completes suspended image cleanup`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val cleanupGate = CompletableDeferred<Unit>()
        lateinit var creationJob: Job
        var cleanupFinished = false

        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            beforeCreate = {
                creationJob = currentCoroutineContext().job
                awaitCancellation()
            }
            beforeDeleteRecipeImage = {
                cleanupGate.await()
                cleanupFinished = true
            }
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")

        viewModel.createRecipe()
        runCurrent()

        creationJob.cancel()
        runCurrent()

        try {
            assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
            assertFalse(cleanupFinished)
            assertFalse(creationJob.isCompleted)
            assertTrue(viewModel.isLoading.value)
        } finally {
            cleanupGate.complete(Unit)
            advanceUntilIdle()
        }

        assertTrue(cleanupFinished)
        assertTrue(creationJob.isCancelled)
        assertTrue(creationJob.isCompleted)
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cleanup cancellation preserves original creation cancellation`() = runTest {
        val imageUrl = "https://storage.example/image.jpg"
        val originalCancellation = CancellationException("creation cancelled")
        val cleanupCancellation = CancellationException("cleanup cancelled")
        var completionCause: Throwable? = null

        val repository = FakeRecipeRepository().apply {
            uploadRecipeImageResult = Result.success(imageUrl)
            deleteRecipeImageResult = Result.failure(cleanupCancellation)
            beforeCreate = {
                currentCoroutineContext().job.invokeOnCompletion {
                    completionCause = it
                }
                throw originalCancellation
            }
        }
        val viewModel = createViewModel(repository)
        val events = mutableListOf<CreateRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }
        fillValidForm(viewModel)
        viewModel.onImageSelected("content://recipes/image")

        viewModel.createRecipe()
        advanceUntilIdle()

        assertSame(originalCancellation, completionCause)
        assertEquals(listOf(imageUrl), repository.deletedRecipeImageUrls)
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    private fun createViewModel(repository: FakeRecipeRepository) =
        CreateRecipeViewModel(
            CreateRecipeUseCase(repository, FakeAuthRepository()),
            repository
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