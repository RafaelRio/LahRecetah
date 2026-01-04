package com.rafario.lahrecetah.ui.add_recipe

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineDropdownField
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField
import com.rafario.lahrecetah.utils.positionAwareImePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val durationText by viewModel.durationText.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val difficulty by viewModel.difficulty.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {
            when (it) {
                is AddRecipeEvent.Success -> {
                    Toast.makeText(context, "Receta añadida", Toast.LENGTH_SHORT).show()
                }
                is AddRecipeEvent.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(Modifier
        .positionAwareImePadding()
        .padding(20.dp)) {
        Column(
            modifier = Modifier

                .verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            CustomOutlineTextField(
                value = title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = "Título de la receta",
                modifier = Modifier
            )

            CustomOutlineTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
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
                    value = durationText,
                    onValueChange = { viewModel.onDurationChanged(it) },
                    label = "Duración",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                CustomOutlineDropdownField(
                    modifier = Modifier.weight(1f),
                    value = category,
                    onValueChange = { viewModel.onCategoryChanged(it) },
                    label = "Categoría",
                    options = RecipeCategory.entries.toList(),
                    optionLabel = { RecipeCategory.toDisplayName(it) }
                )
            }

            HorizontalDivider()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionHeader(title = "Dificultad")
                    Text(
                        text = "${difficulty}/5",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = difficulty.toFloat(),
                    onValueChange = { viewModel.onDifficultyChanged(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            SectionHeader(title = "Ingredientes")

            if (ingredients.isEmpty()) {
                Text(
                    text = "No has añadido ingredientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ingredients.forEachIndexed { index, ingredient ->
                DynamicRowItem(
                    text = ingredient,
                    onTextChange = { viewModel.updateIngredient(index, it) },
                    onRemove = { viewModel.removeIngredient(index) },
                    placeholder = "Ej. 200g de Harina"
                )
            }

            TextButton(
                onClick = { viewModel.addIngredientRow() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Añadir Ingrediente")
            }

            HorizontalDivider()

            SectionHeader(title = "Pasos de preparación")

            if (steps.isEmpty()) {
                Text(
                    text = "No has añadido pasos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            steps.forEachIndexed { index, step ->
                DynamicRowItem(
                    text = step,
                    onTextChange = { viewModel.updateStep(index, it) },
                    onRemove = { viewModel.removeStep(index) },
                    placeholder = "Ej. Mezclar los huevos...",
                    isTextArea = true
                )
            }

            TextButton(
                onClick = { viewModel.addStepRow() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Añadir Paso")
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        Button(
            onClick = {
                viewModel.createRecipe()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            enabled = title.isNotBlank() && description.isNotBlank()
        ) {
            Text("Guardar Receta")
        }
    }


}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun DynamicRowItem(
    text: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    placeholder: String,
    isTextArea: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = if (isTextArea) Alignment.Top else Alignment.CenterVertically
    ) {
        CustomOutlineTextField(
            value = text,
            onValueChange = onTextChange,
            label = placeholder,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
