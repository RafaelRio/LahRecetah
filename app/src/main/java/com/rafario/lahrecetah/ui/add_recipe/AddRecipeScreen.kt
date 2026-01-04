package com.rafario.lahrecetah.ui.add_recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.utils.positionAwareImePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    // --- ESTADO DEL FORMULARIO ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var difficulty by remember { mutableFloatStateOf(1f) }

    // Listas dinámicas
    val ingredients = remember { mutableStateListOf("") }
    val steps = remember { mutableStateListOf("") }

    // Dropdown de Categoría
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(RecipeCategory.OTHER) }

    // Scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // ✅ ZONA SCROLL (solo campos)
        Column(
            modifier = Modifier
                .weight(1f)
                .positionAwareImePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva Receta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 1. TÍTULO
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la receta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            // 2. DESCRIPCIÓN
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción breve") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 3. DURACIÓN
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) durationText = it },
                    label = { Text("Minutos") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Text(
                            "min",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )

                // 4. CATEGORÍA (Dropdown M3)
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = RecipeCategory.toDisplayName(selectedCategory),
                        onValueChange = {},
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                    ) {
                        RecipeCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(RecipeCategory.toDisplayName(category)) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // 5. DIFICULTAD (Slider)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Dificultad", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${difficulty.toInt()}/5",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // 6. INGREDIENTES
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
                    onTextChange = { ingredients[index] = it },
                    onRemove = { ingredients.removeAt(index) },
                    placeholder = "Ej. 200g de Harina"
                )
            }

            TextButton(
                onClick = { ingredients.add("") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Añadir Ingrediente")
            }

            HorizontalDivider()

            // 7. PASOS
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
                    onTextChange = { steps[index] = it },
                    onRemove = { steps.removeAt(index) },
                    placeholder = "Ej. Mezclar los huevos...",
                    isTextArea = true
                )
            }

            TextButton(
                onClick = { steps.add("") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Añadir Paso")
            }

            // Un pelín de aire para que el último campo no quede pegado al botón
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val newRecipe = Recipe(
                        title = title,
                        description = description,
                        durationMinutes = durationText.toIntOrNull() ?: 0,
                        category = selectedCategory,
                        difficulty = difficulty.toInt(),
                        ingredients = ingredients.filter { it.isNotBlank() },
                        steps = steps.filter { it.isNotBlank() }
                    )
                    // TODO: viewModel.saveRecipe(newRecipe)
                    // navHostController.popBackStack()
                    println("Guardando receta: $newRecipe")
                },
                modifier = Modifier
                    .fillMaxWidth(), // 👈 clave: el botón se pone encima del teclado
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Guardar Receta")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ✅ BOTÓN FIJO ABAJO (sube con el teclado)

    }
}



// --- COMPONENTES AUXILIARES ---

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
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.weight(1f),
            minLines = 1,
            maxLines = if (isTextArea) 3 else 1,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
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
