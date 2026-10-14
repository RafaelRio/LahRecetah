package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.recipe.RecipeNotFoundException
import com.rafario.lahrecetah.domain.recipe.RecipeUpdateCommittedCancellationException
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
import org.junit.Assert.assertSame
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

    @Test
    fun `save preserves identity author and existing image`() = runTest {
        val recipe = validRecipe()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("  Tortilla actualizada  ")

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        val expectedRecipe = recipe.copy(title = "Tortilla actualizada")

        assertEquals(listOf(expectedRecipe), repository.updated)
        assertEquals(EditRecipeEvent.Updated, event.await())
        assertEquals(null, repository.uploadedImageUri)
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(expectedRecipe.title, viewModel.formState.value.title)
        assertEquals(recipe.imageUrl, viewModel.formState.value.existingImageUrl)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `save uploads selected image and keeps it after successful update`() = runTest {
        val recipe = validRecipe()
        val localUri = "content://recipes/new-image"
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected(localUri)

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(localUri, repository.uploadedImageUri)
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertEquals(EditRecipeEvent.Updated, event.await())
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(uploadedUrl, viewModel.formState.value.existingImageUrl)
        assertEquals(null, viewModel.formState.value.selectedLocalImageUri)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `save after removing image persists empty image URL`() = runTest {
        val recipe = validRecipe()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.removeImage()

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            listOf(recipe.copy(imageUrl = "")),
            repository.updated
        )
        assertEquals(EditRecipeEvent.Updated, event.await())
        assertEquals(null, repository.uploadedImageUri)
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(null, viewModel.formState.value.existingImageUrl)
        assertEquals(null, viewModel.formState.value.selectedLocalImageUri)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `update failure cleans uploaded image and preserves draft`() = runTest {
        val recipe = validRecipe()
        val localUri = "content://recipes/new-image"
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(
                IllegalStateException("update failed")
            )
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("Título editado")
        viewModel.onImageSelected(localUri)
        val draft = viewModel.formState.value

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(localUri, repository.uploadedImageUri)
        assertEquals(
            listOf(
                recipe.copy(
                    title = "Título editado",
                    imageUrl = uploadedUrl
                )
            ),
            repository.updated
        )
        assertEquals(
            EditRecipeEvent.Error(RecipeFormUiError.PersistenceFailed),
            event.await()
        )
        assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cleanup failure preserves persistence error and draft`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(
                IllegalStateException("update failed")
            )
            deleteRecipeImageResult = Result.failure(
                IllegalStateException("cleanup failed")
            )
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("Título editado")
        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            listOf(recipe.copy(title = "Título editado", imageUrl = uploadedUrl)),
            repository.updated
        )
        assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
        assertEquals(
            listOf(EditRecipeEvent.Error(RecipeFormUiError.PersistenceFailed)),
            events
        )
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `update cancellation wrapped in result keeps uploaded image`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(
                CancellationException("update cancelled")
            )
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `thrown update cancellation keeps uploaded image and propagates`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val cancellation = CancellationException("update cancelled")
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            beforeUpdate = { throw cancellation }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `committed update cancellation keeps uploaded image and propagates`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val originalCancellation = CancellationException("cleanup cancelled")
        val cancellation =
            RecipeUpdateCommittedCancellationException(originalCancellation)

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            beforeUpdate = { throw cancellation }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertSame(originalCancellation, completionCause?.cause)
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `committed update cancellation wrapped in result keeps uploaded image`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val originalCancellation = CancellationException("cleanup cancelled")
        val cancellation =
            RecipeUpdateCommittedCancellationException(originalCancellation)

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(cancellation)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertSame(originalCancellation, completionCause?.cause)
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `invalid form prevents image upload and update`() = runTest {
        val recipe = validRecipe()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onDurationChanged("")
        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            EditRecipeEvent.Error(
                RecipeFormUiError.Validation(
                    RecipeFormValidationError.DurationRequired
                )
            ),
            event.await()
        )
        assertEquals(null, repository.uploadedImageUri)
        assertTrue(repository.updated.isEmpty())
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `image upload failure prevents update and preserves draft`() = runTest {
        val recipe = validRecipe()
        val localUri = "content://recipes/new-image"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.failure(
                IllegalStateException("upload failed")
            )
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("Título editado")
        viewModel.onImageSelected(localUri)
        val draft = viewModel.formState.value

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            EditRecipeEvent.Error(RecipeFormUiError.ImageUploadFailed),
            event.await()
        )
        assertEquals(localUri, repository.uploadedImageUri)
        assertTrue(repository.updated.isEmpty())
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `save while update is pending does not duplicate update`() = runTest {
        val recipe = validRecipe()
        val releaseUpdate = CompletableDeferred<Unit>()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            beforeUpdate = { releaseUpdate.await() }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        val events = mutableListOf<EditRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.saveRecipe()
            runCurrent()

            assertTrue(viewModel.isLoading.value)
            assertEquals(listOf(recipe), repository.updated)

            viewModel.saveRecipe()
            runCurrent()

            assertEquals(listOf(recipe), repository.updated)
            assertTrue(events.isEmpty())
        } finally {
            releaseUpdate.complete(Unit)
            advanceUntilIdle()
        }

        assertEquals(listOf(recipe), repository.updated)
        assertEquals(listOf(EditRecipeEvent.Updated), events)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loading another recipe during save preserves latest form and busy state`() = runTest {
        val firstRecipe = validRecipe()
        val latestRecipe = validRecipe().copy(
            id = "latest",
            title = "Última receta",
            imageUrl = "https://example.com/latest.jpg"
        )
        val releaseUpdate = CompletableDeferred<Unit>()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(firstRecipe)
            beforeUpdate = { releaseUpdate.await() }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(firstRecipe.id)
        advanceUntilIdle()

        val events = mutableListOf<EditRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.saveRecipe()
            runCurrent()

            repository.getRecipeByIdResult = Result.success(latestRecipe)
            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            assertEquals(latestRecipe.title, viewModel.formState.value.title)
            assertEquals(latestRecipe.imageUrl, viewModel.formState.value.existingImageUrl)
            assertTrue(viewModel.isReady.value)
            assertTrue(viewModel.isLoading.value)

            viewModel.saveRecipe()
            runCurrent()

            assertEquals(listOf(firstRecipe), repository.updated)
        } finally {
            releaseUpdate.complete(Unit)
            advanceUntilIdle()
        }

        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertEquals(latestRecipe.imageUrl, viewModel.formState.value.existingImageUrl)
        assertEquals(listOf(firstRecipe), repository.updated)
        assertTrue(events.isEmpty())
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `save completion does not stop pending recipe loading`() = runTest {
        val firstRecipe = validRecipe()
        val latestRecipe = validRecipe().copy(
            id = "latest",
            title = "Última receta"
        )
        val releaseUpdate = CompletableDeferred<Unit>()
        val latestResponse = CompletableDeferred<Result<Recipe?>>()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { recipeId ->
                when (recipeId) {
                    firstRecipe.id -> Result.success(firstRecipe)
                    latestRecipe.id -> latestResponse.await()
                    else -> error("Unexpected recipe ID")
                }
            }
            beforeUpdate = { releaseUpdate.await() }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(firstRecipe.id)
        advanceUntilIdle()

        val events = mutableListOf<EditRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.saveRecipe()
            runCurrent()

            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            releaseUpdate.complete(Unit)
            runCurrent()

            assertTrue(viewModel.isLoading.value)
            assertFalse(viewModel.isReady.value)
            assertEquals(RecipeFormState(), viewModel.formState.value)
            assertEquals(listOf(firstRecipe), repository.updated)
            assertTrue(events.isEmpty())
        } finally {
            releaseUpdate.complete(Unit)
            latestResponse.complete(Result.success(latestRecipe))
            advanceUntilIdle()
        }

        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `older save failure cleans its image without affecting latest recipe`() = runTest {
        val firstRecipe = validRecipe()
        val latestRecipe = validRecipe().copy(
            id = "latest",
            title = "Última receta",
            imageUrl = "https://example.com/latest.jpg"
        )
        val uploadedUrl = "https://example.com/new-image.jpg"
        val releaseUpdate = CompletableDeferred<Unit>()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(firstRecipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            beforeUpdate = { releaseUpdate.await() }
            updateResult = Result.failure(
                IllegalStateException("old update failed")
            )
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(firstRecipe.id)
        advanceUntilIdle()
        viewModel.onImageSelected("content://recipes/new-image")

        val events = mutableListOf<EditRecipeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        try {
            viewModel.saveRecipe()
            runCurrent()

            repository.getRecipeByIdResult = Result.success(latestRecipe)
            viewModel.loadRecipe(latestRecipe.id)
            runCurrent()

            assertEquals(latestRecipe.title, viewModel.formState.value.title)
            assertTrue(viewModel.isLoading.value)
        } finally {
            releaseUpdate.complete(Unit)
            advanceUntilIdle()
        }

        assertEquals(
            listOf(firstRecipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
        assertTrue(events.isEmpty())
        assertEquals(latestRecipe.title, viewModel.formState.value.title)
        assertEquals(latestRecipe.imageUrl, viewModel.formState.value.existingImageUrl)
        assertEquals(null, viewModel.formState.value.selectedLocalImageUri)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cancelling pending update keeps uploaded image without UI error`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            beforeUpdate = { awaitCancellation() }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()

        try {
            runCurrent()

            assertTrue(viewModel.isLoading.value)
            assertEquals(
                listOf(recipe.copy(imageUrl = uploadedUrl)),
                repository.updated
            )
            assertFalse(saveJob.isCompleted)
        } finally {
            saveJob.cancel()
            advanceUntilIdle()
        }

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `save before readiness or during loading does nothing`() = runTest {
        val recipe = validRecipe()
        val response = CompletableDeferred<Result<Recipe?>>()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdHandler = { response.await() }
        }
        val viewModel = EditRecipeViewModel(repository)
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        runCurrent()

        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.isReady.value)
        assertTrue(repository.updated.isEmpty())
        assertTrue(events.isEmpty())

        try {
            viewModel.loadRecipe(recipe.id)
            runCurrent()

            viewModel.saveRecipe()
            runCurrent()

            assertTrue(viewModel.isLoading.value)
            assertFalse(viewModel.isReady.value)
            assertTrue(repository.updated.isEmpty())
            assertEquals(null, repository.uploadedImageUri)
            assertTrue(events.isEmpty())
        } finally {
            response.complete(Result.success(recipe))
            advanceUntilIdle()
        }

        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
        assertTrue(repository.updated.isEmpty())
        assertTrue(events.isEmpty())
    }

    @Test
    fun `save persists edited fields and normalizes text and lists`() = runTest {
        val recipe = validRecipe()
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("  Ensalada  ")
        viewModel.onDescriptionChanged("  Ensalada casera  ")
        viewModel.onDurationChanged("15")
        viewModel.onDifficultyChanged(4)
        viewModel.onCategoryChanged(RecipeCategory.SALAD)
        viewModel.updateIngredient(0, "  Tomate  ")
        viewModel.updateIngredient(1, "   ")
        viewModel.updateStep(0, "  Cortar y servir  ")
        viewModel.updateStep(1, "   ")

        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        val expected = recipe.copy(
            title = "Ensalada",
            description = "Ensalada casera",
            durationMinutes = 15,
            difficulty = 4,
            category = RecipeCategory.SALAD,
            ingredients = listOf("Tomate"),
            steps = listOf("Cortar y servir")
        )

        assertEquals(listOf(expected), repository.updated)
        assertEquals(EditRecipeEvent.Updated, event.await())
        assertEquals(
            RecipeFormState(
                title = expected.title,
                description = expected.description,
                durationText = "15",
                difficulty = 4,
                category = RecipeCategory.SALAD,
                ingredients = expected.ingredients,
                steps = expected.steps,
                existingImageUrl = expected.imageUrl
            ),
            viewModel.formState.value
        )
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `missing recipe during update emits not found and cleans uploaded image`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(RecipeNotFoundException())
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val event = async { viewModel.uiEvent.first() }
        runCurrent()

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            EditRecipeEvent.Error(RecipeFormUiError.RecipeNotFound),
            event.await()
        )
        assertEquals(
            listOf(recipe.copy(imageUrl = uploadedUrl)),
            repository.updated
        )
        assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
        assertEquals(draft, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `upload cancellation wrapped in result propagates without updating`() = runTest {
        val recipe = validRecipe()
        val cancellation = CancellationException("upload cancelled")
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.failure(cancellation)
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertTrue(repository.updated.isEmpty())
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cancellation before update waits for image cleanup and propagates`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val cancellation = CancellationException("cancelled before update")
        val releaseCleanup = CompletableDeferred<Unit>()
        var cleanupFinished = false

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            beforeUploadRecipeImage = {
                currentCoroutineContext().job.cancel(cancellation)
            }
            beforeDeleteRecipeImage = {
                currentCoroutineContext().ensureActive()
                releaseCleanup.await()
                cleanupFinished = true
            }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        try {
            runCurrent()

            assertTrue(saveJob.isCancelled)
            assertFalse(saveJob.isCompleted)
            assertTrue(viewModel.isLoading.value)
            assertTrue(repository.updated.isEmpty())
            assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
            assertFalse(cleanupFinished)
        } finally {
            releaseCleanup.complete(Unit)
            advanceUntilIdle()
        }

        assertTrue(cleanupFinished)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertTrue(repository.updated.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `thrown upload cancellation propagates without updating`() = runTest {
        val recipe = validRecipe()
        val cancellation = CancellationException("upload cancelled")
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            beforeUploadRecipeImage = { throw cancellation }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        advanceUntilIdle()

        assertTrue(saveJob.isCancelled)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertTrue(repository.updated.isEmpty())
        assertTrue(repository.deletedRecipeImageUrls.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `cleanup cancellation preserves original cancellation before update`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val cancellation = CancellationException("cancelled before update")
        val releaseCleanup = CompletableDeferred<Unit>()
        var cleanupFinished = false

        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            deleteRecipeImageResult = Result.failure(
                CancellationException("cleanup cancelled")
            )
            beforeUploadRecipeImage = {
                currentCoroutineContext().job.cancel(cancellation)
            }
            beforeDeleteRecipeImage = {
                currentCoroutineContext().ensureActive()
                releaseCleanup.await()
                cleanupFinished = true
            }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        val saveJob = viewModel.viewModelScope.coroutineContext.job.children.single()
        var completionCause: Throwable? = null
        saveJob.invokeOnCompletion { completionCause = it }

        try {
            runCurrent()

            assertTrue(saveJob.isCancelled)
            assertFalse(saveJob.isCompleted)
            assertTrue(viewModel.isLoading.value)
            assertTrue(repository.updated.isEmpty())
            assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
            assertFalse(cleanupFinished)
        } finally {
            releaseCleanup.complete(Unit)
            advanceUntilIdle()
        }

        assertTrue(cleanupFinished)
        assertTrue(saveJob.isCompleted)
        assertSame(cancellation, completionCause)
        assertTrue(repository.updated.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(draft, viewModel.formState.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `thrown cleanup failure preserves persistence error and draft`() = runTest {
        val recipe = validRecipe()
        val uploadedUrl = "https://example.com/new-image.jpg"
        val repository = FakeRecipeRepository().apply {
            getRecipeByIdResult = Result.success(recipe)
            uploadRecipeImageResult = Result.success(uploadedUrl)
            updateResult = Result.failure(
                IllegalStateException("update failed")
            )
            beforeDeleteRecipeImage = {
                throw IllegalStateException("cleanup failed")
            }
        }
        val viewModel = EditRecipeViewModel(repository)

        viewModel.loadRecipe(recipe.id)
        advanceUntilIdle()

        viewModel.onTitleChanged("Título editado")
        viewModel.onImageSelected("content://recipes/new-image")
        val draft = viewModel.formState.value
        val events = mutableListOf<EditRecipeEvent>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.saveRecipe()
        advanceUntilIdle()

        assertEquals(
            listOf(recipe.copy(title = "Título editado", imageUrl = uploadedUrl)),
            repository.updated
        )
        assertEquals(listOf(uploadedUrl), repository.deletedRecipeImageUrls)
        assertEquals(
            listOf(EditRecipeEvent.Error(RecipeFormUiError.PersistenceFailed)),
            events
        )
        assertEquals(draft, viewModel.formState.value)
        assertTrue(viewModel.isReady.value)
        assertFalse(viewModel.isLoading.value)
    }

    private fun validRecipe() = Recipe(
        id = "recipe-123",
        title = "Tortilla de patatas",
        description = "Una tortilla casera",
        ingredients = listOf("Patatas", "Huevos"),
        steps = listOf("Cocinar las patatas", "Añadir los huevos y cuajar"),
        createdByUid = "author-123",
        createdByName = "Rafa",
        durationMinutes = 30,
        difficulty = 2,
        imageUrl = "https://example.com/original.jpg"
    )
}