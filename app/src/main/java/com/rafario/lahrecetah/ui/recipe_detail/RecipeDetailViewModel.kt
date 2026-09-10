package com.rafario.lahrecetah.ui.recipe_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.usecase.recipes.ObserveRecipeByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val observeRecipeByIdUseCase: ObserveRecipeByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var recipeJob: Job? = null

    fun loadRecipe(recipeId: String) {
        recipeJob?.cancel()

        recipeJob = viewModelScope.launch {
            _uiState.value = RecipeDetailUiState(isLoading = true)

            observeRecipeByIdUseCase(recipeId)
                .catch { e ->
                    if (e is CancellationException) throw e
                    _uiState.value = RecipeDetailUiState(
                        isLoading = false,
                        error = e.message ?: "Error cargando receta"
                    )
                }
                .collectLatest { recipe ->
                    _uiState.value = RecipeDetailUiState(
                        isLoading = false,
                        recipe = recipe,
                        error = if (recipe == null) "La receta no existe" else null
                    )
                }
        }
    }
}

data class RecipeDetailUiState(
    val isLoading: Boolean = false,
    val recipe: Recipe? = null,
    val error: String? = null
)