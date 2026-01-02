package com.rafario.lahrecetah.ui.add_recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField

@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val duration by viewModel.durationText.collectAsStateWithLifecycle()
    val difficulty by viewModel.difficulty.collectAsStateWithLifecycle()

    var ingredientInput by remember { mutableStateOf("") }
    var stepInput by remember { mutableStateOf("") }

    val cs = MaterialTheme.colorScheme

    val imeNoNavBars = WindowInsets.ime.exclude(WindowInsets.navigationBars)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.padding(top = 16.dp))
            SectionCard(title = "Información") {
                CustomOutlineTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChanged,
                    label = "Título"
                )
                CustomOutlineTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = "Descripción"
                )
            }

            SectionCard(title = "Detalles") {
                val categories = listOf(
                    "Entrante" to RecipeCategory.STARTER,
                    "Primer plato" to RecipeCategory.FIRST_COURSE,
                    "Segundo plato" to RecipeCategory.MAIN_COURSE,
                    "Postre" to RecipeCategory.DESSERT,
                    "Dulce" to RecipeCategory.SWEET,
                    "Ensalada" to RecipeCategory.SALAD,
                    "Sopa" to RecipeCategory.SOUP,
                    "Bebida" to RecipeCategory.DRINK
                )

                CategoryDropdown(
                    categories = categories,
                    selected = RecipeCategory.toDisplayName(category),
                    onSelected = { viewModel.onCategoryChanged(it) }
                )

                CustomOutlineTextField(
                    value = duration,
                    onValueChange = viewModel::onDurationChanged,
                    label = "Duración (minutos)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Dificultad: $difficulty", color = cs.onSurface)

                Slider(
                    value = difficulty.toFloat(),
                    onValueChange = { viewModel.onDifficultyChanged(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        activeTrackColor = cs.secondary,
                        inactiveTrackColor = cs.secondary.copy(alpha = 0.25f),
                        thumbColor = cs.secondary
                    )
                )
            }

            SectionCard(title = "Ingredientes") {
                ingredients.forEach { Text("• $it", color = cs.onSurface) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomOutlineTextField(
                        modifier = Modifier.weight(1f),
                        value = ingredientInput,
                        onValueChange = { ingredientInput = it },
                        label = "Nuevo ingrediente"
                    )

                    Button(
                        onClick = {
                            if (ingredientInput.isNotBlank()) {
                                viewModel.addIngredient(ingredientInput)
                                ingredientInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.secondary.copy(alpha = 0.18f),
                            contentColor = cs.onSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("+") }
                }
            }

            SectionCard(title = "Pasos") {
                steps.forEachIndexed { index, step ->
                    Text("${index + 1}. $step", color = cs.onSurface)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomOutlineTextField(
                        modifier = Modifier.weight(1f),
                        value = stepInput,
                        onValueChange = { stepInput = it },
                        label = "Nuevo paso"
                    )

                    Button(
                        onClick = {
                            if (stepInput.isNotBlank()) {
                                viewModel.addStep(stepInput)
                                stepInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.secondary.copy(alpha = 0.18f),
                            contentColor = cs.onSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("+") }
                }
            }
        }

        Button(
            onClick = { viewModel.createRecipe() },
            colors = ButtonDefaults.buttonColors(
                containerColor = cs.primary,
                contentColor = cs.onPrimary
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .windowInsetsPadding(imeNoNavBars)
        ) {
            Text("Guardar receta", style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Composable
fun SectionCard(
    title: String, content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Pair<String, RecipeCategory>>,
    selected: String,
    onSelected: (RecipeCategory) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = cs.secondary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.18f),
                focusedLabelColor = cs.secondary,
                unfocusedLabelColor = cs.onSurface.copy(alpha = 0.7f),
                cursorColor = cs.secondary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cs.surface)
        ) {
            categories.forEach { (name, category) ->
                DropdownMenuItem(text = { Text(name, color = cs.onSurface) }, onClick = {
                    onSelected(category)
                    expanded = false
                })
            }
        }
    }
}