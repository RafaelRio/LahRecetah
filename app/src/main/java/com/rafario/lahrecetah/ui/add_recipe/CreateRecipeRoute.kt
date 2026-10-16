package com.rafario.lahrecetah.ui.add_recipe

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormContent
import com.rafario.lahrecetah.ui.recipe_form.RecipeImagePicker
import com.rafario.lahrecetah.ui.recipe_form.toStringRes

@Composable
fun CreateRecipeRoute(
    modifier: Modifier = Modifier,
    viewModel: CreateRecipeViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CreateRecipeEvent.Created -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_created),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is CreateRecipeEvent.Error -> {
                    Toast.makeText(
                        context,
                        context.getString(event.error.toStringRes()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    RecipeImagePicker(
        onImageSelected = viewModel::onImageSelected
    ) { onPickImage ->
        RecipeFormContent(
            modifier = modifier,
            state = formState,
            isLoading = isLoading,
            saveButtonText = stringResource(R.string.save_recipe),
            saveEnabled =
                formState.title.isNotBlank() &&
                        formState.description.isNotBlank(),
            onSave = viewModel::createRecipe,
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