package com.rafario.lahrecetah.ui.recipe_form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafario.lahrecetah.domain.model.RecipeCategory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.utils.positionAwareImePadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineDropdownField
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField
import com.rafario.lahrecetah.utils.dashedBorder

@Composable
fun RecipeFormContent(
    state: RecipeFormState,
    isLoading: Boolean,
    saveButtonText: String,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDurationChanged: (String) -> Unit,
    onCategoryChanged: (RecipeCategory) -> Unit,
    onDifficultyChanged: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    onUpdateIngredient: (Int, String) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onIngredientFocusHandled: () -> Unit,
    onAddStep: () -> Unit,
    onUpdateStep: (Int, String) -> Unit,
    onRemoveStep: (Int) -> Unit,
    onStepFocusHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val previewImage = state.selectedLocalImageUri ?: state.existingImageUrl

    Box(
        modifier = modifier
            .positionAwareImePadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .dashedBorder(
                        color = MaterialTheme.colorScheme.primary
                    )
                    .clickable(
                        enabled = previewImage.isNullOrBlank(),
                        onClick = onPickImage
                    )
            ) {
                if (previewImage.isNullOrBlank()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(stringResource(R.string.add_image))
                    }
                } else {
                    AsyncImage(
                        model = previewImage,
                        contentDescription = stringResource(R.string.recipe_image),
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(30.dp)
                            .size(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.remove_image),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            CustomOutlineTextField(
                value = state.title,
                onValueChange = onTitleChanged,
                label = stringResource(R.string.recipe_title),
                modifier = Modifier
            )

            CustomOutlineTextField(
                value = state.description,
                onValueChange = onDescriptionChanged,
                label = stringResource(R.string.recipe_description),
                modifier = Modifier,
                multiline = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlineTextField(
                    value = state.durationText,
                    onValueChange = onDurationChanged,
                    label = stringResource(R.string.duration),
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                CustomOutlineDropdownField(
                    modifier = Modifier.weight(1f),
                    value = state.category,
                    onValueChange = onCategoryChanged,
                    label = stringResource(R.string.category),
                    options = RecipeCategory.entries.toList(),
                    optionLabel = RecipeCategory::toDisplayName
                )
            }

            HorizontalDivider()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader(
                        title = stringResource(R.string.difficulty)
                    )

                    Text(
                        text = stringResource(R.string.difficulty_value, state.difficulty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = state.difficulty.toFloat(),
                    onValueChange = {
                        onDifficultyChanged(it.toInt())
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            SectionHeader(
                title = stringResource(R.string.ingredients)
            )

            if (state.ingredients.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_ingredients),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.ingredients.forEachIndexed { index, ingredient ->
                DynamicRowItem(
                    text = ingredient,
                    onTextChange = {
                        onUpdateIngredient(index, it)
                    },
                    onRemove = {
                        onRemoveIngredient(index)
                    },
                    placeholder = stringResource(R.string.ingredient_example),
                    requestFocus = index == state.focusedIngredientIndex,
                    onFocusRequested = onIngredientFocusHandled
                )
            }

            TextButton(
                onClick = onAddIngredient,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(Modifier.padding(4.dp))

                Text(stringResource(R.string.add_ingredient))
            }

            HorizontalDivider()

            SectionHeader(
                title = stringResource(R.string.preparation_steps)
            )

            if (state.steps.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.steps.forEachIndexed { index, step ->
                DynamicRowItem(
                    text = step,
                    onTextChange = {
                        onUpdateStep(index, it)
                    },
                    onRemove = {
                        onRemoveStep(index)
                    },
                    placeholder = stringResource(R.string.step_example),
                    isTextArea = true,
                    requestFocus = index == state.focusedStepIndex,
                    onFocusRequested = onStepFocusHandled
                )
            }

            TextButton(
                onClick = onAddStep,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(Modifier.padding(4.dp))

                Text(stringResource(R.string.add_step))
            }

            Spacer(
                modifier = Modifier.height(60.dp)
            )
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            enabled = saveEnabled && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(stringResource(R.string.saving))
            } else {
                Text(saveButtonText)
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DynamicRowItem(
    text: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    placeholder: String,
    isTextArea: Boolean = false,
    requestFocus: Boolean = false,
    onFocusRequested: () -> Unit = {}
) {
    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusRequested()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment =
            if (isTextArea) {
                Alignment.Top
            } else {
                Alignment.CenterVertically
            }
    ) {
        CustomOutlineTextField(
            value = text,
            onValueChange = onTextChange,
            label = placeholder,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            multiline = isTextArea
        )

        IconButton(
            onClick = onRemove
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}