package com.rafario.lahrecetah.ui.recipe_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase
) : ViewModel() {

    val uiState: StateFlow<RecipeListUiState> =
        getRecipesUseCase()
            .map< List<Recipe>, RecipeListUiState > {
                RecipeListUiState.Success(it)
            }
            .onStart {
                emit(RecipeListUiState.Loading)
            }
            .catch { e ->
                emit(
                    RecipeListUiState.Error(
                        e.message ?: "Error al cargar recetas"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RecipeListUiState.Loading
            )
}

sealed interface RecipeListUiState {
    object Loading : RecipeListUiState
    data class Success(val recipes: List<Recipe>) : RecipeListUiState
    data class Error(val message: String) : RecipeListUiState
}