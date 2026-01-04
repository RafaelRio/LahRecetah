package com.rafario.lahrecetah.ui.add_recipe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.usecase.recipes.CreateRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val createRecipeUseCase: CreateRecipeUseCase, private val storage: FirebaseStorage
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

    private val _category = MutableStateFlow(RecipeCategory.OTHER)
    val category = _category.asStateFlow()

    private val _durationText = MutableStateFlow("")
    val durationText = _durationText.asStateFlow()

    private val _localImageUri = MutableStateFlow<String?>(null)
    val localImageUri = _localImageUri.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _localImageUri.value = uri.toString()
    }

    fun onDurationChanged(value: String) {
        _durationText.value = value
    }

    private val _difficulty = MutableStateFlow(3)
    val difficulty = _difficulty.asStateFlow()

    fun onTitleChanged(value: String) {
        _title.value = value
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun onCategoryChanged(value: RecipeCategory) {
        _category.value = value
    }

    fun onDifficultyChanged(value: Int) {
        _difficulty.value = value.coerceIn(1, 5)
    }

    fun addIngredientRow() {
        _ingredients.value += ""
    }

    fun updateIngredient(index: Int, value: String) {
        _ingredients.value = _ingredients.value.toMutableList().also { list ->
            if (index in list.indices) list[index] = value
        }
    }

    fun removeIngredient(index: Int) {
        _ingredients.value = _ingredients.value.toMutableList().also { list ->
            if (index in list.indices) list.removeAt(index)
        }
    }

    fun addStepRow() {
        _steps.value += ""
    }

    fun updateStep(index: Int, value: String) {
        _steps.value = _steps.value.toMutableList().also { list ->
            if (index in list.indices) list[index] = value
        }
    }

    fun removeStep(index: Int) {
        _steps.value = _steps.value.toMutableList().also { list ->
            if (index in list.indices) list.removeAt(index)
        }
    }

    fun createRecipe() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch

            _isLoading.value = true
            try {
                if (_title.value.isBlank()) {
                    _uiEvent.emit(AddRecipeEvent.Error("El título es obligatorio"))
                    return@launch
                }

                if (_ingredients.value.isEmpty() || _ingredients.value.all { it.isBlank() }) {
                    _uiEvent.emit(AddRecipeEvent.Error("Añade al menos un ingrediente"))
                    return@launch
                }

                if (_steps.value.isEmpty() || _steps.value.all { it.isBlank() }) {
                    _uiEvent.emit(AddRecipeEvent.Error("Añade al menos un paso"))
                    return@launch
                }

                val imageUrl = _localImageUri.value?.let { uriStr ->
                    uploadRecipeImage(Uri.parse(uriStr))
                } ?: ""

                val result = createRecipeUseCase(
                    title = _title.value,
                    description = _description.value,
                    ingredients = _ingredients.value.filter { it.isNotBlank() },
                    steps = _steps.value.filter { it.isNotBlank() },
                    category = _category.value,
                    durationMinutes = _durationText.value.toIntOrNull() ?: 0,
                    difficulty = _difficulty.value,
                    imageUrl = imageUrl
                )

                if (result.isSuccess) {
                    clearForm()
                    _uiEvent.emit(AddRecipeEvent.Success)
                } else {
                    _uiEvent.emit(
                        AddRecipeEvent.Error(
                            result.exceptionOrNull()?.message ?: "Error desconocido"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiEvent.emit(AddRecipeEvent.Error(e.message ?: "Error inesperado"))
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun clearForm() {
        _title.value = ""
        _description.value = ""
        _ingredients.value = emptyList()
        _steps.value = emptyList()
        _category.value = RecipeCategory.OTHER
        _durationText.value = ""
        _difficulty.value = 3
    }

    private suspend fun uploadRecipeImage(uri: Uri): String {
        val ref = storage.reference.child("recipes").child("${java.util.UUID.randomUUID()}.jpg")

        // sube
        ref.putFile(uri).await()

        // url pública
        return ref.downloadUrl.await().toString()
    }
}

sealed class AddRecipeEvent {
    object Success : AddRecipeEvent()
    data class Error(val message: String?) : AddRecipeEvent()
}