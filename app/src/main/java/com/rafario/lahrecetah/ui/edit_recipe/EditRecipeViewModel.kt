package com.rafario.lahrecetah.ui.edit_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormStateHolder
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val formStateHolder = RecipeFormStateHolder()
    val formState = formStateHolder.state

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _uiEvent = MutableSharedFlow<EditRecipeEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loadRecipe(recipeId: String) {
        _isReady.value = false
        loadedRecipe = null
        formStateHolder.reset()
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val recipe = recipeRepository
                    .getRecipeById(recipeId)
                    .getOrThrow()

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
                _uiEvent.emit(
                    EditRecipeEvent.Error(RecipeFormUiError.Unexpected)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed interface EditRecipeEvent {
    data class Error(val error: RecipeFormUiError) : EditRecipeEvent
}