package com.rafario.lahrecetah.ui.add_recipe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.model.normalized
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import com.rafario.lahrecetah.domain.validation.RecipeValidationException
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationResult
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddRecipeEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var editLoadJob: Job? = null

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(localImageUri = uri.toString())
        }
    }

    fun removeImage() {
        _uiState.update {
            it.copy(localImageUri = null)
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it.copy(title = value)
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update {
            it.copy(description = value)
        }
    }

    fun onDurationChanged(value: String) {
        _uiState.update {
            it.copy(durationText = value)
        }
    }

    fun onCategoryChanged(value: RecipeCategory) {
        _uiState.update {
            it.copy(category = value)
        }
    }

    fun onDifficultyChanged(value: Int) {
        _uiState.update {
            it.copy(difficulty = value.coerceIn(1, 5))
        }
    }

    fun addIngredientRow() {
        _uiState.update { state ->
            val updatedIngredients = state.ingredients + ""

            state.copy(
                ingredients = updatedIngredients,
                focusedIngredientIndex = updatedIngredients.lastIndex
            )
        }
    }

    fun updateIngredient(index: Int, value: String) {
        _uiState.update { state ->
            if (index !in state.ingredients.indices) {
                return@update state
            }

            val updatedIngredients = state.ingredients.toMutableList().apply {
                this[index] = value
            }

            state.copy(ingredients = updatedIngredients)
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update { state ->
            if (index !in state.ingredients.indices) {
                return@update state
            }

            val updatedIngredients = state.ingredients.toMutableList().apply {
                removeAt(index)
            }

            state.copy(
                ingredients = updatedIngredients,
                focusedIngredientIndex = null
            )
        }
    }

    fun clearIngredientFocus() {
        _uiState.update {
            it.copy(focusedIngredientIndex = null)
        }
    }

    fun addStepRow() {
        _uiState.update { state ->
            val updatedSteps = state.steps + ""

            state.copy(
                steps = updatedSteps,
                focusedStepIndex = updatedSteps.lastIndex
            )
        }
    }

    fun updateStep(index: Int, value: String) {
        _uiState.update { state ->
            if (index !in state.steps.indices) {
                return@update state
            }

            val updatedSteps = state.steps.toMutableList().apply {
                this[index] = value
            }

            state.copy(steps = updatedSteps)
        }
    }

    fun removeStep(index: Int) {
        _uiState.update { state ->
            if (index !in state.steps.indices) {
                return@update state
            }

            val updatedSteps = state.steps.toMutableList().apply {
                removeAt(index)
            }

            state.copy(
                steps = updatedSteps,
                focusedStepIndex = null
            )
        }
    }

    fun clearStepFocus() {
        _uiState.update {
            it.copy(focusedStepIndex = null)
        }
    }

    fun exitEditingMode() {
        editLoadJob?.cancel()
        editLoadJob = null

        val currentState = _uiState.value

        val wasEditing =
            currentState.isEditMode ||
                    currentState.editingRecipeId != null

        if (wasEditing) {
            _uiState.value = AddRecipeUiState()
        } else {
            _uiState.update {
                it.copy(
                    isEditMode = false,
                    editingRecipeId = null,
                    isEditReady = false,
                    isLoading = false
                )
            }
        }
    }

    fun startEditing(recipeId: String) {
        editLoadJob?.cancel()

        _uiState.value = AddRecipeUiState(
            isEditMode = true,
            editingRecipeId = recipeId,
            isLoading = true
        )

        editLoadJob = viewModelScope.launch {
            try {
                val recipe = recipeRepository
                    .observeRecipeById(recipeId)
                    .first()

                if (recipe == null) {
                    _uiEvent.emit(
                        AddRecipeEvent.Error(
                            "La receta ya no existe o no está disponible"
                        )
                    )
                    return@launch
                }

                _uiState.update { state ->
                    if (state.editingRecipeId != recipeId) {
                        return@update state
                    }

                    state.copy(
                        title = recipe.title,
                        description = recipe.description,
                        ingredients = recipe.ingredients,
                        steps = recipe.steps,
                        category = recipe.category,
                        durationText = recipe.durationMinutes.toString(),
                        difficulty = recipe.difficulty.coerceIn(1, 5),
                        localImageUri = recipe.imageUrl.ifBlank { null },
                        isEditReady = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiEvent.emit(
                    AddRecipeEvent.Error(
                        e.message ?: "Error cargando receta"
                    )
                )
            } finally {
                _uiState.update { state ->
                    if (state.editingRecipeId == recipeId) {
                        state.copy(isLoading = false)
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun saveEdits() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        if (!state.isEditReady) {
            emitError("La receta no está disponible para editar")
            return
        }

        val recipeId = state.editingRecipeId

        if (recipeId == null) {
            emitError("No se encontró la receta a editar")
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

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
                        _uiEvent.emit(AddRecipeEvent.ValidationError(validation.error))
                        return@launch
                    }

                    is RecipeFormValidationResult.Valid -> validation.durationMinutes
                }

                val finalImageUrl = when (val uriString = state.localImageUri) {
                    null -> ""

                    else -> {
                        if (uriString.startsWith("http")) {
                            uriString
                        } else {
                            recipeRepository.uploadRecipeImage(
                                uriString
                            ).getOrThrow()
                        }
                    }
                }

                val updatedRecipe = Recipe(
                    id = recipeId,
                    title = state.title,
                    description = state.description,
                    ingredients = state.ingredients,
                    steps = state.steps,
                    durationMinutes = durationMinutes,
                    category = state.category,
                    difficulty = state.difficulty,
                    imageUrl = finalImageUrl
                ).normalized()

                val result = recipeRepository.updateRecipe(updatedRecipe)

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                if (failure is RecipeValidationException) {
                    _uiEvent.emit(
                        AddRecipeEvent.ValidationError(
                            RecipeFormValidationError.Domain(failure.reason)
                        )
                    )
                } else {
                    _uiEvent.emit(
                        AddRecipeEvent.Error(failure?.message ?: "Error desconocido")
                    )
                }

                if (result.isSuccess) {
                    exitEditingMode()
                    _uiEvent.emit(AddRecipeEvent.Updated)
                } else {
                    _uiEvent.emit(
                        AddRecipeEvent.Error(
                            result.exceptionOrNull()?.message
                                ?: "Error actualizando receta"
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiEvent.emit(
                    AddRecipeEvent.Error(
                        e.message ?: "Error inesperado"
                    )
                )
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun createRecipe() {
        val state = _uiState.value

        if (state.isLoading) {
            return
        }

        _uiState.update {
            it.copy(isLoading = true)
        }

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
                        _uiEvent.emit(AddRecipeEvent.ValidationError(validation.error))
                        return@launch
                    }

                    is RecipeFormValidationResult.Valid -> validation.durationMinutes
                }

                val imageUrl = when (val uriString = state.localImageUri) {
                    null -> ""

                    else -> {
                        if (uriString.startsWith("http")) {
                            uriString
                        } else {
                            recipeRepository.uploadRecipeImage(
                                uriString
                            ).getOrThrow()
                        }
                    }
                }

                val result = createRecipeUseCase(
                    title = state.title,
                    description = state.description,
                    ingredients = state.ingredients,
                    steps = state.steps,
                    category = state.category,
                    durationMinutes = durationMinutes,
                    difficulty = state.difficulty,
                    imageUrl = imageUrl
                )

                val failure = result.exceptionOrNull()
                if (failure is CancellationException) throw failure

                when {
                    result.isSuccess -> {
                        clearForm()
                        _uiEvent.emit(AddRecipeEvent.Created)
                    }

                    failure is RecipeValidationException -> {
                        _uiEvent.emit(
                            AddRecipeEvent.ValidationError(
                                RecipeFormValidationError.Domain(failure.reason)
                            )
                        )
                    }

                    else -> {
                        _uiEvent.emit(
                            AddRecipeEvent.Error(failure?.message ?: "Error desconocido")
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiEvent.emit(
                    AddRecipeEvent.Error(
                        e.message ?: "Error inesperado"
                    )
                )
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun clearForm() {
        _uiState.update { state ->
            state.copy(
                title = "",
                description = "",
                durationText = "",
                ingredients = emptyList(),
                steps = emptyList(),
                difficulty = 3,
                category = RecipeCategory.OTHER,
                localImageUri = null,
                focusedIngredientIndex = null,
                focusedStepIndex = null
            )
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(AddRecipeEvent.Error(message))
        }
    }
}

sealed class AddRecipeEvent {
    data object Created : AddRecipeEvent()
    data object Updated : AddRecipeEvent()
    data class Error(val message: String?) : AddRecipeEvent()
    data class ValidationError(val error: RecipeFormValidationError) : AddRecipeEvent()
}

data class AddRecipeUiState(
    val title: String = "",
    val description: String = "",
    val durationText: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val difficulty: Int = 3,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val localImageUri: String? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val editingRecipeId: String? = null,
    val isEditReady: Boolean = false,
    val focusedIngredientIndex: Int? = null,
    val focusedStepIndex: Int? = null
)