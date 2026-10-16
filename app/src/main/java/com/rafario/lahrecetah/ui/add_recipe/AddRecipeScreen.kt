package com.rafario.lahrecetah.ui.add_recipe

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import com.rafario.lahrecetah.ui.edit_recipe.EditRecipeRoute
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormContent
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import com.rafario.lahrecetah.ui.recipe_form.RecipeImagePicker

@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    editingRecipeId: String?,
    onEditFinished: () -> Unit,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {

    if (!editingRecipeId.isNullOrBlank()) {
        EditRecipeRoute(
            recipeId = editingRecipeId, onEditFinished = onEditFinished, modifier = modifier
        )
        return
    }

    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.exitEditingMode()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddRecipeEvent.Created -> {
                    Toast.makeText(
                        context, context.getString(R.string.recipe_created), Toast.LENGTH_SHORT
                    ).show()
                }

                is AddRecipeEvent.Error -> {
                    Toast.makeText(
                        context,
                        event.message ?: context.getString(R.string.unexpected_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is AddRecipeEvent.ValidationError -> {
                    val resourceId = when (val error = event.error) {
                        RecipeFormValidationError.DurationRequired -> R.string.recipe_validation_duration_required

                        RecipeFormValidationError.DurationNotInteger -> R.string.recipe_validation_duration_not_integer

                        is RecipeFormValidationError.Domain -> when (error.reason) {
                            RecipeValidationError.TITLE -> R.string.recipe_validation_title
                            RecipeValidationError.DESCRIPTION -> R.string.recipe_validation_description
                            RecipeValidationError.INGREDIENTS -> R.string.recipe_validation_ingredients
                            RecipeValidationError.STEPS -> R.string.recipe_validation_steps
                            RecipeValidationError.DURATION -> R.string.recipe_validation_duration
                            RecipeValidationError.DIFFICULTY -> R.string.recipe_validation_difficulty
                        }
                    }

                    Toast.makeText(
                        context, context.getString(resourceId), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    RecipeImagePicker(
        onImageSelected = { uri ->
            viewModel.onImageSelected(uri.toUri())
        }) { onPickImage ->

        RecipeFormContent(
            modifier = modifier,
            state = RecipeFormState(
                title = uiState.title,
                description = uiState.description,
                durationText = uiState.durationText,
                ingredients = uiState.ingredients,
                steps = uiState.steps,
                difficulty = uiState.difficulty,
                category = uiState.category,
                focusedIngredientIndex = uiState.focusedIngredientIndex,
                focusedStepIndex = uiState.focusedStepIndex
            ),
            previewImage = uiState.localImageUri,
            saveButtonText = stringResource(R.string.save_recipe),
            saveEnabled = uiState.title.isNotBlank() && uiState.description.isNotBlank(),
            onSave = viewModel::createRecipe,
            isLoading = uiState.isLoading,
            onPickImage = onPickImage,
            onRemoveImage = viewModel::removeImage,
            onTitleChanged = viewModel::onTitleChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onDurationChanged = viewModel::onDurationChanged,
            onCategoryChanged = viewModel::onCategoryChanged,
            onDifficultyChanged = viewModel::onDifficultyChanged,
            onAddIngredient = viewModel::addIngredientRow,
            onUpdateIngredient = viewModel::updateIngredient,
            onRemoveIngredient = viewModel::removeIngredient,
            onIngredientFocusHandled = viewModel::clearIngredientFocus,
            onAddStep = viewModel::addStepRow,
            onUpdateStep = viewModel::updateStep,
            onRemoveStep = viewModel::removeStep,
            onStepFocusHandled = viewModel::clearStepFocus
        )
    }
}
