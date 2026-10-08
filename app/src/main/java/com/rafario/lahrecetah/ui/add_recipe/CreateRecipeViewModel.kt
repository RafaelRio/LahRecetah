package com.rafario.lahrecetah.ui.add_recipe

import androidx.lifecycle.ViewModel
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormStateHolder
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import com.rafario.lahrecetah.domain.validation.RecipeValidationException
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationResult
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase
) : ViewModel() {
    private val formStateHolder = RecipeFormStateHolder()
    val formState = formStateHolder.state

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateRecipeEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTitleChanged(value: String) = formStateHolder.onTitleChanged(value)
    fun onDescriptionChanged(value: String) = formStateHolder.onDescriptionChanged(value)
    fun onDurationChanged(value: String) = formStateHolder.onDurationChanged(value)
    fun onCategoryChanged(value: RecipeCategory) = formStateHolder.onCategoryChanged(value)
    fun onDifficultyChanged(value: Int) = formStateHolder.onDifficultyChanged(value)

    fun addIngredientRow() = formStateHolder.addIngredientRow()
    fun updateIngredient(index: Int, value: String) =
        formStateHolder.updateIngredient(index, value)
    fun removeIngredient(index: Int) = formStateHolder.removeIngredient(index)
    fun clearIngredientFocus() = formStateHolder.clearIngredientFocus()

    fun addStepRow() = formStateHolder.addStepRow()
    fun updateStep(index: Int, value: String) = formStateHolder.updateStep(index, value)
    fun removeStep(index: Int) = formStateHolder.removeStep(index)
    fun clearStepFocus() = formStateHolder.clearStepFocus()

    fun onImageSelected(uri: String) = formStateHolder.onImageSelected(uri)
    fun removeImage() = formStateHolder.removeImage()

    fun createRecipe() {
        if (_isLoading.value) return

        val state = formStateHolder.state.value
        _isLoading.value = true

        viewModelScope.launch {
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
                        _uiEvent.emit(
                            CreateRecipeEvent.Error(
                                RecipeFormUiError.Validation(validation.error)
                            )
                        )
                        return@launch
                    }

                    is RecipeFormValidationResult.Valid -> validation.durationMinutes
                }

                val result = createRecipeUseCase(
                    title = state.title,
                    description = state.description,
                    ingredients = state.ingredients,
                    steps = state.steps,
                    category = state.category,
                    durationMinutes = durationMinutes,
                    difficulty = state.difficulty,
                    imageUrl = ""
                )

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                when {
                    result.isSuccess -> {
                        formStateHolder.reset()
                        _uiEvent.emit(CreateRecipeEvent.Created)
                    }

                    failure is RecipeValidationException -> {
                        _uiEvent.emit(
                            CreateRecipeEvent.Error(
                                RecipeFormUiError.Validation(
                                    RecipeFormValidationError.Domain(failure.reason)
                                )
                            )
                        )
                    }

                    else -> {
                        _uiEvent.emit(
                            CreateRecipeEvent.Error(RecipeFormUiError.PersistenceFailed)
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: RecipeValidationException) {
                _uiEvent.emit(
                    CreateRecipeEvent.Error(
                        RecipeFormUiError.Validation(
                            RecipeFormValidationError.Domain(e.reason)
                        )
                    )
                )
            } catch (e: Exception) {
                _uiEvent.emit(
                    CreateRecipeEvent.Error(RecipeFormUiError.Unexpected)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}

sealed interface CreateRecipeEvent {
    data object Created : CreateRecipeEvent
    data class Error(val error: RecipeFormUiError) : CreateRecipeEvent
}