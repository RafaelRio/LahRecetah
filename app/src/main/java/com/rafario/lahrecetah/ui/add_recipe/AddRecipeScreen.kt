package com.rafario.lahrecetah.ui.add_recipe

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.ui.custom_views.CustomOutlineTextField

@Composable
fun AddRecipeScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddRecipeEvent.Success -> Toast.makeText(
                    context,
                    "Creado con exito",
                    Toast.LENGTH_SHORT
                ).show()

                is AddRecipeEvent.Error ->
                    Toast.makeText(
                        context,
                        event.message ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        CustomOutlineTextField(
            value = title,
            onValueChange = {
                viewModel.onTitleChanged(it)
            },
            label = "Título"
        )

        Spacer(Modifier.height(8.dp))

        CustomOutlineTextField(
            value = description,
            onValueChange = {
                viewModel.onDescriptionChanged(it)
            },
            label = "Descripción"
        )

        Spacer(Modifier.height(16.dp))

        Text("Ingredientes")
        ingredients.forEach { Text("• $it") }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { /* diálogo añadir ingrediente */ }) {
            Text("Añadir ingrediente")
        }

        Spacer(Modifier.height(16.dp))

        Text("Pasos")
        steps.forEachIndexed { index, step ->
            Text("${index + 1}. $step")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { /* diálogo añadir paso */ }) {
            Text("Añadir paso")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.createRecipe() }
        ) {
            Text("Guardar receta")
        }
    }
}