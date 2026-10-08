package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecipeFormStateHolder {
    private val _state = MutableStateFlow(RecipeFormState())
    val state = _state.asStateFlow()

    fun onTitleChanged(value: String) {
        _state.update { it.copy(title = value) }
    }

    fun onDescriptionChanged(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onDurationChanged(value: String) {
        _state.update { it.copy(durationText = value) }
    }

    fun onCategoryChanged(value: RecipeCategory) {
        _state.update { it.copy(category = value) }
    }

    fun onDifficultyChanged(value: Int) {
        _state.update { it.copy(difficulty = value.coerceIn(1, 5)) }
    }

    fun addIngredientRow() {
        _state.update { current ->
            val ingredients = current.ingredients + ""
            current.copy(
                ingredients = ingredients,
                focusedIngredientIndex = ingredients.lastIndex
            )
        }
    }

    fun updateIngredient(index: Int, value: String) {
        _state.update { current ->
            if (index !in current.ingredients.indices) return@update current

            val ingredients = current.ingredients.toMutableList().apply {
                this[index] = value
            }
            current.copy(ingredients = ingredients)
        }
    }

    fun removeIngredient(index: Int) {
        _state.update { current ->
            if (index !in current.ingredients.indices) return@update current

            val ingredients = current.ingredients.toMutableList().apply {
                removeAt(index)
            }
            current.copy(
                ingredients = ingredients,
                focusedIngredientIndex = null
            )
        }
    }

    fun clearIngredientFocus() {
        _state.update { it.copy(focusedIngredientIndex = null) }
    }

    fun addStepRow() {
        _state.update { current ->
            val steps = current.steps + ""
            current.copy(
                steps = steps,
                focusedStepIndex = steps.lastIndex
            )
        }
    }

    fun updateStep(index: Int, value: String) {
        _state.update { current ->
            if (index !in current.steps.indices) return@update current

            val steps = current.steps.toMutableList().apply {
                this[index] = value
            }
            current.copy(steps = steps)
        }
    }

    fun removeStep(index: Int) {
        _state.update { current ->
            if (index !in current.steps.indices) return@update current

            val steps = current.steps.toMutableList().apply {
                removeAt(index)
            }
            current.copy(
                steps = steps,
                focusedStepIndex = null
            )
        }
    }

    fun clearStepFocus() {
        _state.update { it.copy(focusedStepIndex = null) }
    }

    fun onImageSelected(uri: String) {
        _state.update { it.copy(selectedLocalImageUri = uri) }
    }

    fun removeImage() {
        _state.update {
            it.copy(
                existingImageUrl = null,
                selectedLocalImageUri = null
            )
        }
    }

    fun populateFrom(recipe: Recipe) {
        _state.value = RecipeFormState(
            title = recipe.title,
            description = recipe.description,
            durationText = recipe.durationMinutes.toString(),
            ingredients = recipe.ingredients,
            steps = recipe.steps,
            difficulty = recipe.difficulty.coerceIn(1, 5),
            category = recipe.category,
            existingImageUrl = recipe.imageUrl.takeIf { it.isNotBlank() }
        )
    }

    fun reset() {
        _state.value = RecipeFormState()
    }
}