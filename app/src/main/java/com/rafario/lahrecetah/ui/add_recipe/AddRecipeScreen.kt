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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineDropdownField
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField
import com.rafario.lahrecetah.utils.dashedBorder
import com.rafario.lahrecetah.utils.positionAwareImePadding
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
    val scrollState = rememberScrollState()

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
                        "No se pudo obtener la imagen recortada",
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
                    error?.message ?: "Error recortando imagen",
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
                        "Receta añadida",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                AddRecipeEvent.Updated -> {
                    Toast.makeText(
                        context,
                        "Receta actualizada",
                        Toast.LENGTH_SHORT
                    ).show()

                    onEditFinished()
                }

                is AddRecipeEvent.Error -> {
                    Toast.makeText(
                        context,
                        event.message ?: "Error inesperado",
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
                "No se encontró ninguna aplicación de cámara",
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
                "Permiso de cámara denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
                        enabled = uiState.localImageUri.isNullOrBlank()
                    ) {
                        showImagePickerSheet = true
                    }
            ) {
                if (uiState.localImageUri.isNullOrBlank()) {
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

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Text("Añadir imagen")
                    }
                } else {
                    AsyncImage(
                        model = uiState.localImageUri,
                        contentDescription = "Imagen receta",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = viewModel::removeImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(30.dp)
                            .size(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.8f
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar imagen",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            CustomOutlineTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label = "Título de la receta",
                modifier = Modifier
            )

            CustomOutlineTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = "Descripción de la receta",
                modifier = Modifier,
                multiline = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlineTextField(
                    value = uiState.durationText,
                    onValueChange = viewModel::onDurationChanged,
                    label = "Duración",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                CustomOutlineDropdownField(
                    modifier = Modifier.weight(1f),
                    value = uiState.category,
                    onValueChange = viewModel::onCategoryChanged,
                    label = "Categoría",
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
                        title = "Dificultad"
                    )

                    Text(
                        text = "${uiState.difficulty}/5",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = uiState.difficulty.toFloat(),
                    onValueChange = {
                        viewModel.onDifficultyChanged(it.toInt())
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            SectionHeader(
                title = "Ingredientes"
            )

            if (uiState.ingredients.isEmpty()) {
                Text(
                    text = "No has añadido ingredientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            uiState.ingredients.forEachIndexed { index, ingredient ->
                DynamicRowItem(
                    text = ingredient,
                    onTextChange = {
                        viewModel.updateIngredient(index, it)
                    },
                    onRemove = {
                        viewModel.removeIngredient(index)
                    },
                    placeholder = "Ej. 200g de Harina",
                    requestFocus =
                        index == uiState.focusedIngredientIndex,
                    onFocusRequested =
                        viewModel::clearIngredientFocus
                )
            }

            TextButton(
                onClick = viewModel::addIngredientRow,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.padding(4.dp)
                )

                Text("Añadir Ingrediente")
            }

            HorizontalDivider()

            SectionHeader(
                title = "Pasos de preparación"
            )

            if (uiState.steps.isEmpty()) {
                Text(
                    text = "No has añadido pasos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            uiState.steps.forEachIndexed { index, step ->
                DynamicRowItem(
                    text = step,
                    onTextChange = {
                        viewModel.updateStep(index, it)
                    },
                    onRemove = {
                        viewModel.removeStep(index)
                    },
                    placeholder = "Ej. Mezclar los huevos...",
                    isTextArea = true,
                    requestFocus =
                        index == uiState.focusedStepIndex,
                    onFocusRequested =
                        viewModel::clearStepFocus
                )
            }

            TextButton(
                onClick = viewModel::addStepRow,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    Modifier.padding(4.dp)
                )

                Text("Añadir Paso")
            }

            Spacer(
                modifier = Modifier.height(60.dp)
            )
        }

        Button(
            onClick = {
                if (uiState.isEditMode) {
                    viewModel.saveEdits()
                } else {
                    viewModel.createRecipe()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            enabled =
                !uiState.isLoading &&
                        uiState.title.isNotBlank() &&
                        uiState.description.isNotBlank() &&
                        (!uiState.isEditMode || uiState.isEditReady)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Text("Guardando…")
            } else {
                Text(
                    if (uiState.isEditMode) {
                        "Guardar cambios"
                    } else {
                        "Guardar receta"
                    }
                )
            }
        }
    }

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
                        contentDescription = "Cámara",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text("Cámara")
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
                        contentDescription = "Galería",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text("Galería")
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
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
fun DynamicRowItem(
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
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error
            )
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