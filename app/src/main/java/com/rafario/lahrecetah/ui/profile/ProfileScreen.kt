package com.rafario.lahrecetah.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    onEditRecipe: (String) -> Unit, // ✅ NUEVO
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dropdown state para "Mis recetas"
    var recipesExpanded by rememberSaveable { mutableStateOf(false) }

    // navegación logout
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            navHostController.navigate(Routes.LOGIN) {
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
    }

    var showEditName by remember { mutableStateOf(false) }

    // Error
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } },
            title = { Text("Aviso") },
            text = { Text(uiState.errorMessage ?: "") })
    }

    // Confirmación borrado receta
    val pending = uiState.pendingDeleteRecipe
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isDeletingRecipe) viewModel.cancelDeleteRecipe() },
            title = { Text("Eliminar receta") },
            text = { Text("¿Seguro que quieres eliminar \"${pending.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteRecipe() },
                    enabled = !uiState.isDeletingRecipe
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDeleteRecipe() },
                    enabled = !uiState.isDeletingRecipe
                ) { Text("Cancelar") }
            })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Cargando perfil…")
                }
            }

            uiState.profile != null -> {
                val profile = uiState.profile!!

                // ✅ Un solo scroll (LazyColumn) para toda la pantalla
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {

                    // Header
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileAvatar(
                                    name = profile.name, modifier = Modifier.size(56.dp)
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name.ifBlank { "Usuario" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = profile.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(onClick = { showEditName = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    // App
                    item {
                        SectionCard(title = "Aplicación") {
                            SettingRow(
                                title = "Preferencias",
                                subtitle = "Notificaciones, tema, etc.",
                                trailing = { TextButton(onClick = { /* TODO */ }) { Text("Abrir") } })

                            Divider()

                            SettingRow(
                                title = "Ayuda",
                                subtitle = "Soporte y preguntas frecuentes",
                                trailing = { TextButton(onClick = { /* TODO */ }) { Text("Ver") } })
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    // -------------------------------------------------------------
                    // ✅ MIS RECETAS: "UNA MISMA TARJETA" (header + lista dentro)
                    // Se pinta como varios items, pero visualmente es una sola card.
                    // -------------------------------------------------------------

                    val recipes = uiState.myRecipes
                    val hasRecipes = recipes.isNotEmpty()
                    val showList = recipesExpanded && hasRecipes && !uiState.isLoadingRecipes

                    // 1) Header de la "tarjeta" (con esquinas arriba redondeadas)
                    item {
                        RecipeSectionTop(
                            title = "Mis recetas",
                            subtitle = when {
                                uiState.isLoadingRecipes -> "Cargando recetas…"
                                !hasRecipes -> "Aún no has creado recetas."
                                else -> "${recipes.size} receta(s)"
                            },
                            expanded = recipesExpanded,
                            enabled = !uiState.isLoadingRecipes && hasRecipes,
                            onToggle = { recipesExpanded = !recipesExpanded })
                    }

                    // 2) Si está cargando, metemos una fila “interna” dentro de la misma tarjeta
                    if (uiState.isLoadingRecipes) {
                        item {
                            RecipeSectionInnerRow(
                                isLast = true, // cierra la tarjeta visualmente
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Cargando recetas…")
                                }
                            }
                        }
                    }

                    // 3) Si está vacío, metemos fila “interna” y cerramos la tarjeta
                    if (!uiState.isLoadingRecipes && !hasRecipes) {
                        item {
                            RecipeSectionInnerRow(isLast = true) {
                                Text(
                                    text = "Aún no has creado recetas.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4) Si hay recetas y está expandido: items virtualizados “dentro” de la tarjeta
                    if (showList) {
                        items(
                            items = recipes,
                            key = { it.id }
                        ) { recipe ->
                            val isLast = recipe.id == recipes.last().id

                            RecipeSectionInnerRow(isLast = isLast) {
                                RecipeRowInSection(
                                    recipe = recipe,
                                    showDivider = !isLast,
                                    onClick = { onEditRecipe(recipe.id) },
                                    onDelete = { viewModel.askDeleteRecipe(recipe.id, recipe.title) }
                                )
                            }
                        }

                    } else if (!uiState.isLoadingRecipes && hasRecipes) {
                        // 5) Si hay recetas pero está colapsado, cerramos la tarjeta con una fila “interna” fina.
                        item {
                            RecipeSectionInnerRow(isLast = true) {
                                Text(
                                    text = "Toca para desplegar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    // Logout
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Cerrar sesión",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Saldrás de tu cuenta en este dispositivo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { viewModel.logout() }, enabled = !uiState.isSaving
                                ) { Text("Salir") }
                            }
                        }
                    }
                }

                if (showEditName) {
                    EditNameDialog(
                        initialValue = profile.name,
                        saving = uiState.isSaving,
                        onDismiss = { showEditName = false },
                        onSave = { newName ->
                            viewModel.updateName(newName)
                            showEditName = false
                        })
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No se pudo cargar el perfil.")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadProfile() }) { Text("Reintentar") }
                }
            }
        }
    }
}

/**
 * ---------------------------
 *  "Mis recetas" (UI Section)
 * ---------------------------
 * Se dibuja como una única "tarjeta visual" compuesta por:
 * - Top (con esquinas arriba redondeadas)
 * - N filas internas (sin sombra, mismo color)
 * - Última fila con esquinas abajo redondeadas (cierra la tarjeta)
 */

@Composable
private fun RecipeSectionTop(
    title: String,
    subtitle: String,
    expanded: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val shapeTop = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    Surface(
        shape = shapeTop,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,   // 👈 MISMO tono que las filas internas
        shadowElevation = 1.dp   // 👈 sombra exterior arriba
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Contraer" else "Expandir",
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeSectionInnerRow(
    isLast: Boolean,
    content: @Composable () -> Unit
) {
    val shape = when {
        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp // 👈 también en las del medio
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun RecipeRowInSection(
    recipe: Recipe,
    showDivider: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${RecipeCategory.toDisplayName(recipe.category)} · ${recipe.durationMinutes} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onClick) {
            Icon(Icons.Default.Edit, contentDescription = "Editar receta")
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar receta")
        }
    }

    if (showDivider) Divider()
}

@Composable
private fun ProfileAvatar(
    name: String, modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.joinToString("")
            .ifBlank { "U" }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SectionCard(
    title: String, content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            })
    }
}

@Composable
private fun SettingRow(
    title: String, subtitle: String, trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing()
    }
}

@Composable
private fun EditNameDialog(
    initialValue: String, saving: Boolean, onDismiss: () -> Unit, onSave: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Editar nombre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (saving) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Guardando…")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(value) }, enabled = !saving && value.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = { if (!saving) onDismiss() }) { Text("Cancelar") }
        })
}
