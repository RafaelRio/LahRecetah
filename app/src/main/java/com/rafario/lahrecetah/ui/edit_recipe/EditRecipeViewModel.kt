package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormStateHolder
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private var loadedRecipe: Recipe? = null

    private var loadJob: Job? = null
    private var loadGeneration = 0L

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
                    _isLoading.value = false
                }
            }
        }
    }
}

sealed interface EditRecipeEvent {
    data class Error(val error: RecipeFormUiError) : EditRecipeEvent
}