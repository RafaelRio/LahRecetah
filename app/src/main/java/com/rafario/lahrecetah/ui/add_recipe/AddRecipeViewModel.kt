package com.rafario.lahrecetah.ui.add_recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _ingredients = MutableStateFlow<List<String>>(emptyList())
    val ingredients = _ingredients.asStateFlow()

    private val _steps = MutableStateFlow<List<String>>(emptyList())
    val steps = _steps.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AddRecipeEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTitleChanged(value: String) {
        _title.value = value
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun addIngredient(value: String) {
        if (value.isNotBlank()) {
            _ingredients.value += value
        }
    }

    fun addStep(value: String) {
        if (value.isNotBlank()) {
            _steps.value += value
        }
    }

    fun createRecipe() {
        viewModelScope.launch {
            val result = createRecipeUseCase(
                title.value,
                description.value,
                ingredients.value,
                steps.value
            )

            if (result.isSuccess) {
                _uiEvent.emit(AddRecipeEvent.Success)
            } else {
                _uiEvent.emit(
                    AddRecipeEvent.Error(result.exceptionOrNull()?.message)
                )
            }
        }
    }
}

sealed class AddRecipeEvent {
    object Success : AddRecipeEvent()
    data class Error(val message: String?) : AddRecipeEvent()
}