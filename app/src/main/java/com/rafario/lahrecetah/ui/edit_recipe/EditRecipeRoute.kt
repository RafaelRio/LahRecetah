package com.rafario.lahrecetah.ui.edit_recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormContent
import com.rafario.lahrecetah.ui.recipe_form.RecipeImagePicker
import com.rafario.lahrecetah.ui.recipe_form.toStringRes

@Composable
fun EditRecipeRoute(
    recipeId: String,
    onEditFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val currentOnEditFinished by rememberUpdatedState(onEditFinished)

    LaunchedEffect(viewModel, context) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                EditRecipeEvent.Updated -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_updated),
                        Toast.LENGTH_SHORT
                    ).show()

                    currentOnEditFinished()
                }

                is EditRecipeEvent.Error -> {
                    Toast.makeText(
                        context,
                        context.getString(event.error.toStringRes()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    LaunchedEffect(viewModel, recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    RecipeImagePicker(
        onImageSelected = viewModel::onImageSelected
    ) { onPickImage ->
        RecipeFormContent(
            modifier = modifier,
            state = formState,
            isLoading = isLoading,
            saveButtonText = stringResource(R.string.save_changes),
            saveEnabled =
                isReady &&
                        formState.title.isNotBlank() &&
                        formState.description.isNotBlank(),
            onSave = viewModel::saveRecipe,
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