package com.rafario.lahrecetah.ui.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.ui.custom_views.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    navHostController: NavHostController
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.error != null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
            }

            state.recipe != null -> {
                val recipe = state.recipe!!

                // Fondo general scrollable
                Box(Modifier.fillMaxSize()) {

                    // --- HERO IMAGE (fondo) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        if (recipe.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = recipe.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }

                        // Degradado sutil
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.40f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )

                        BackButton(
                            modifier = Modifier
                                .padding(top = 15.dp)
                                .padding(20.dp)
                                .align(Alignment.TopStart),
                            onClick = { navHostController.popBackStack() }
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .padding(bottom = 60.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        RecipeCategory.toDisplayName(recipe.category),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.Black.copy(alpha = 0.35f),
                                    labelColor = Color.White,
                                    leadingIconContentColor = Color.White
                                ),
                                border = null
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 250.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = MaterialTheme.colorScheme.background,
                        tonalElevation = 20.dp,
                        shadowElevation = 40.dp
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // META
                            item {
                                MetaStripCard(
                                    durationMinutes = recipe.durationMinutes,
                                    difficulty = recipe.difficulty,
                                    createdByName = recipe.createdByName,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // DESCRIPCIÓN
                            if (recipe.description.isNotBlank()) {
                                item {
                                    SectionCard(
                                        title = "Descripción",
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = recipe.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // INGREDIENTES
                            if (recipe.ingredients.isNotEmpty()) {
                                item {
                                    SectionCard(
                                        title = "Ingredientes",
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            recipe.ingredients.forEach { ingredient ->
                                                IngredientRow(
                                                    text = ingredient,
                                                    icon = ingredientIcon(ingredient)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // PASOS
                            if (recipe.steps.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Pasos",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                itemsIndexed(recipe.steps) { index, step ->
                                    StepCard(
                                        number = index + 1,
                                        text = step,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaStripCard(
    durationMinutes: Int,
    difficulty: Int,
    createdByName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaPill(
                icon = Icons.Default.AccessTime,
                text = "$durationMinutes min",
                modifier = Modifier.weight(1f)
            )

            MetaPill(
                icon = Icons.Default.Star,
                text = "${difficulty}/5",
                modifier = Modifier.weight(1f)
            )

            if (createdByName.isNotBlank()) {
                MetaPill(
                    icon = Icons.Default.Person,
                    text = createdByName,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

@Composable
private fun MetaPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                content()
            }
        )
    }
}

@Composable
private fun IngredientRow(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icono “semántico”
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StepCard(number: Int, text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun ingredientIcon(ingredient: String): ImageVector {
    val t = ingredient.lowercase().trim()

    return when {
        // Proteínas
        listOf(
            "pollo",
            "carne",
            "ternera",
            "cerdo",
            "pavo",
            "atún",
            "salmon",
            "salmón",
            "pescado",
            "bacon"
        )
            .any { it in t } -> Icons.Default.Restaurant

        // Bebidas y lácteos
        listOf("leche", "nata", "yogur", "kefir", "agua", "vino", "cerveza", "zumo", "jugo")
            .any { it in t } -> Icons.Default.LocalDrink

        // Verduras / frutas
        listOf(
            "tomate",
            "cebolla",
            "ajo",
            "pimiento",
            "zanahoria",
            "patata",
            "limón",
            "manzana",
            "plátano",
            "platano"
        )
            .any { it in t } -> Icons.Default.Agriculture

        // Granos / harinas / pan
        listOf(
            "harina",
            "pan",
            "arroz",
            "pasta",
            "avena",
            "trigo",
            "espaguetis",
            "macarrones",
            "espirales"
        )
            .any { it in t } -> Icons.Default.Eco

        // Especias / condimentos
        listOf(
            "sal",
            "pimienta",
            "orégano",
            "oregano",
            "comino",
            "pimentón",
            "pimenton",
            "especias"
        )
            .any { it in t } -> Icons.Default.Spa

        // Café / té / chocolate (como “extra visual”)
        listOf("café", "cafe", "té", "te", "chocolate", "cacao")
            .any { it in t } -> Icons.Default.EmojiFoodBeverage

        else -> Icons.Default.Kitchen
    }
}
