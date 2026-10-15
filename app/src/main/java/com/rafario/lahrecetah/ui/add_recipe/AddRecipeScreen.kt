package com.rafario.lahrecetah.ui.add_recipe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.validation.RecipeValidationError
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormContent
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormState
import com.rafario.lahrecetah.ui.recipe_form.RecipeFormValidationError
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    editingRecipeId: String?,
    onEditFinished: () -> Unit,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showImagePickerSheet by remember {
        mutableStateOf(false)
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val data = result.data

        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val resultUri = data?.let {
                    UCrop.getOutput(it)
                }

                if (resultUri != null) {
                    viewModel.onImageSelected(resultUri)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.crop_result_missing),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            UCrop.RESULT_ERROR -> {
                val error = data?.let {
                    UCrop.getError(it)
                }

                Toast.makeText(
                    context,
                    error?.message ?: context.getString(R.string.crop_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(editingRecipeId) {
        if (!editingRecipeId.isNullOrBlank()) {
            viewModel.startEditing(editingRecipeId)
        } else {
            viewModel.exitEditingMode()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AddRecipeEvent.Created -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_created),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                AddRecipeEvent.Updated -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.recipe_updated),
                        Toast.LENGTH_SHORT
                    ).show()

                    onEditFinished()
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
                        RecipeFormValidationError.DurationRequired ->
                            R.string.recipe_validation_duration_required

                        RecipeFormValidationError.DurationNotInteger ->
                            R.string.recipe_validation_duration_not_integer

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
                        context,
                        context.getString(resourceId),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(
                context.cacheDir,
                "cropped_${System.currentTimeMillis()}.jpg"
            )
        )

        val intent = UCrop.of(
            sourceUri,
            destinationUri
        )
            .withAspectRatio(16f, 9f)
            .getIntent(context)

        cropLauncher.launch(intent)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            startCrop(uri)
        }
    }

    var cameraUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let {
                startCrop(it)
            }
        }
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        cameraUri = uri

        try {
            takePictureLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.camera_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.camera_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
        isLoading = uiState.isLoading,
        saveButtonText = stringResource(
            if (uiState.isEditMode) {
                R.string.save_changes
            } else {
                R.string.save_recipe
            }
        ),
        saveEnabled =
            uiState.title.isNotBlank() &&
                    uiState.description.isNotBlank() &&
                    (!uiState.isEditMode || uiState.isEditReady),
        onSave = {
            if (uiState.isEditMode) {
                viewModel.saveEdits()
            } else {
                viewModel.createRecipe()
            }
        },
        onPickImage = {
            showImagePickerSheet = true
        },
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

    if (showImagePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showImagePickerSheet = false
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 24.dp,
                        horizontal = 16.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        showImagePickerSheet = false

                        if (hasCameraPermission(context)) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(
                                android.Manifest.permission.CAMERA
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.camera),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(stringResource(R.string.camera))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        showImagePickerSheet = false
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.gallery),
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(stringResource(R.string.gallery))
                }
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val file = File(
        context.cacheDir,
        "camera_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}
