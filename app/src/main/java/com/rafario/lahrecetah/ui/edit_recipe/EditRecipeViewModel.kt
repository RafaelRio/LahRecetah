package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.model.normalized
import com.rafario.lahrecetah.domain.recipe.RecipeNotFoundException
import com.rafario.lahrecetah.domain.recipe.RecipeUpdateCommittedCancellationException
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormStateHolder
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationResult
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private var loadedRecipe: Recipe? = null

    private var loadJob: Job? = null
    private var loadGeneration = 0L
    private var isSaving = false

    private val formStateHolder = RecipeFormStateHolder()
    val formState = formStateHolder.state

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditRecipeEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTitleChanged(value: String) = formStateHolder.onTitleChanged(value)
    fun onDescriptionChanged(value: String) = formStateHolder.onDescriptionChanged(value)
    fun onDurationChanged(value: String) = formStateHolder.onDurationChanged(value)
    fun onCategoryChanged(value: RecipeCategory) = formStateHolder.onCategoryChanged(value)
    fun onDifficultyChanged(value: Int) = formStateHolder.onDifficultyChanged(value)
    fun addIngredientRow() = formStateHolder.addIngredientRow()
    fun updateIngredient(index: Int, value: String) = formStateHolder.updateIngredient(index, value)
    fun removeIngredient(index: Int) = formStateHolder.removeIngredient(index)
    fun clearIngredientFocus() = formStateHolder.clearIngredientFocus()
    fun addStepRow() = formStateHolder.addStepRow()
    fun updateStep(index: Int, value: String) = formStateHolder.updateStep(index, value)
    fun removeStep(index: Int) = formStateHolder.removeStep(index)
    fun clearStepFocus() = formStateHolder.clearStepFocus()
    fun onImageSelected(uri: String) = formStateHolder.onImageSelected(uri)
    fun removeImage() = formStateHolder.removeImage()

    fun saveRecipe() {
        if (_isLoading.value || !_isReady.value) return

        val originalRecipe = loadedRecipe ?: return
        val state = formStateHolder.state.value
        val currentGeneration = loadGeneration

        isSaving = true
        _isLoading.value = true

        viewModelScope.launch {
            var uploadedImageUrl: String? = null
            var updateOutcomeUnknown = false
            var originalCancellation: CancellationException? = null
            try {
                val validation = RecipeFormValidator.validate(
                    title = state.title,
                    description = state.description,
                    ingredients = state.ingredients,
                    steps = state.steps,
                    durationText = state.durationText,
                    difficulty = state.difficulty
                )

                val durationMinutes = when (validation) {
                    is RecipeFormValidationResult.Invalid -> {
                        if (currentGeneration == loadGeneration) {
                            _uiEvent.emit(
                                EditRecipeEvent.Error(
                                    RecipeFormUiError.Validation(validation.error)
                                )
                            )
                        }
                        return@launch
                    }

                    is RecipeFormValidationResult.Valid -> validation.durationMinutes
                }

                val imageUrl = state.selectedLocalImageUri?.let { uri ->
                    val uploadResult = recipeRepository.uploadRecipeImage(uri)
                    val uploadFailure = uploadResult.exceptionOrNull()

                    if (uploadFailure is CancellationException) throw uploadFailure

                    if (uploadFailure != null) {
                        if (currentGeneration == loadGeneration) {
                            _uiEvent.emit(
                                EditRecipeEvent.Error(RecipeFormUiError.ImageUploadFailed)
                            )
                        }
                        return@launch
                    }

                    val uploadedUrl = uploadResult.getOrThrow()
                    uploadedImageUrl = uploadedUrl
                    uploadedUrl
                } ?: state.existingImageUrl.orEmpty()

                val updatedRecipe = originalRecipe.copy(
                    title = state.title,
                    description = state.description,
                    ingredients = state.ingredients,
                    steps = state.steps,
                    category = state.category,
                    durationMinutes = durationMinutes,
                    difficulty = state.difficulty,
                    imageUrl = imageUrl
                ).normalized()

                currentCoroutineContext().ensureActive()

                updateOutcomeUnknown = true
                val updateResult = recipeRepository.updateRecipe(updatedRecipe)
                val updateFailure = updateResult.exceptionOrNull()

                if (updateFailure is CancellationException) throw updateFailure

                updateOutcomeUnknown = false

                if (updateResult.isSuccess) {
                    uploadedImageUrl = null

                    if (currentGeneration == loadGeneration) {
                        loadedRecipe = updatedRecipe
                        formStateHolder.populateFrom(updatedRecipe)
                        _uiEvent.emit(EditRecipeEvent.Updated)
                    }
                } else if (currentGeneration == loadGeneration) {
                    val error = when (updateFailure) {
                        is RecipeNotFoundException -> RecipeFormUiError.RecipeNotFound
                        else -> RecipeFormUiError.PersistenceFailed
                    }

                    _uiEvent.emit(EditRecipeEvent.Error(error))
                }
            } catch (e: RecipeUpdateCommittedCancellationException) {
                uploadedImageUrl = null
                updateOutcomeUnknown = false
                originalCancellation = e
                throw e
            } catch (e: CancellationException) {
                originalCancellation = e
                throw e
            } catch (e: Exception) {
                if (currentGeneration == loadGeneration) {
                    _uiEvent.emit(
                        EditRecipeEvent.Error(RecipeFormUiError.Unexpected)
                    )
                }
            } finally {
                try {
                    if (!updateOutcomeUnknown) {
                        uploadedImageUrl?.let { imageUrl ->
                            withContext(NonCancellable) {
                                cleanupUploadedImage(imageUrl)
                            }
                        }
                    }
                } catch (cleanupCancellation: CancellationException) {
                    throw originalCancellation ?: cleanupCancellation
                } finally {
                    isSaving = false
                    _isLoading.value = loadJob?.isActive == true
                }
            }
        }
    }

    private suspend fun cleanupUploadedImage(imageUrl: String) {
        try {
            recipeRepository.deleteRecipeImage(imageUrl).getOrThrow()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Un fallo de limpieza no debe sustituir al error original.
        }
    }

    fun loadRecipe(recipeId: String) {
        loadGeneration += 1
        val currentGeneration = loadGeneration
        loadJob?.cancel()
        _isReady.value = false
        loadedRecipe = null
        formStateHolder.reset()
        _isLoading.value = true

        loadJob = viewModelScope.launch {
            try {
                val recipe = recipeRepository
                    .getRecipeById(recipeId)
                    .getOrThrow()

                if (currentGeneration != loadGeneration) {
                    return@launch
                }

                if (recipe == null) {
                    _uiEvent.emit(
                        EditRecipeEvent.Error(RecipeFormUiError.RecipeNotFound)
                    )
                    return@launch
                }

                loadedRecipe = recipe
                formStateHolder.populateFrom(recipe)
                _isReady.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (currentGeneration == loadGeneration) {
                    _uiEvent.emit(
                        EditRecipeEvent.Error(RecipeFormUiError.Unexpected)
                    )
                }
            } finally {
                if (currentGeneration == loadGeneration) {
                    _isLoading.value = isSaving
                }
            }
        }
    }
}

sealed interface EditRecipeEvent {
    data object Updated : EditRecipeEvent
    data class Error(val error: RecipeFormUiError) : EditRecipeEvent
}