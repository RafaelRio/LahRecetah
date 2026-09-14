package com.rafario.lahrecetah.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafario.lahrecetah.R
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onEditRecipe: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dropdown state para "Mis recetas"
    var recipesExpanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // navegación logout
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            try {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            } catch (e: ClearCredentialException) {
                // Firebase y la preferencia local ya se han limpiado.
                Log.w("ProfileScreen", "Could not clear credential provider state", e)
            }
            onLogout()
        }
    }

    var showEditName by remember { mutableStateOf(false) }

    // Error
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.notice)) },
            text = { Text(uiState.errorMessage ?: "") })
    }

    // Confirmación borrado receta
    val pending = uiState.pendingDeleteRecipe
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isDeletingRecipe) viewModel.cancelDeleteRecipe() },
            title = { Text(stringResource(R.string.delete_recipe)) },
            text = { Text(stringResource(R.string.delete_recipe_confirmation, pending.title)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteRecipe() },
                    enabled = !uiState.isDeletingRecipe
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDeleteRecipe() },
                    enabled = !uiState.isDeletingRecipe
                ) { Text(stringResource(R.string.cancel)) }
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
                    Text(stringResource(R.string.loading_profile))
                }
            }

            uiState.profile != null -> {
                val profile = uiState.profile!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {

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
                                        text = profile.name.ifBlank { stringResource(R.string.user) },
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
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_name))
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }


                    val recipes = uiState.myRecipes
                    val hasRecipes = recipes.isNotEmpty()
                    val showList = recipesExpanded && hasRecipes && !uiState.isLoadingRecipes

                    item {
                        RecipeSectionTop(
                            title = stringResource(R.string.my_recipes),
                            subtitle = when {
                                uiState.isLoadingRecipes -> stringResource(R.string.loading_recipes)
                                !hasRecipes -> stringResource(R.string.no_own_recipes)
                                else -> stringResource(R.string.recipe_count, recipes.size)
                            },
                            expanded = recipesExpanded,
                            enabled = !uiState.isLoadingRecipes && hasRecipes,
                            onToggle = { recipesExpanded = !recipesExpanded })
                    }

                    if (uiState.isLoadingRecipes) {
                        item {
                            RecipeSectionInnerRow(
                                isLast = true,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text(stringResource(R.string.loading_recipes))
                                }
                            }
                        }
                    }

                    if (!uiState.isLoadingRecipes && !hasRecipes) {
                        item {
                            RecipeSectionInnerRow(isLast = true) {
                                Text(
                                    text = stringResource(R.string.no_own_recipes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (showList) {
                        items(
                            items = recipes, key = { it.id }) { recipe ->
                            val isLast = recipe.id == recipes.last().id

                            RecipeSectionInnerRow(isLast = isLast) {
                                RecipeRowInSection(
                                    recipe = recipe,
                                    showDivider = !isLast,
                                    onClick = { onEditRecipe(recipe.id) },
                                    onDelete = {
                                        viewModel.askDeleteRecipe(
                                            recipe.id, recipe.title
                                        )
                                    })
                            }
                        }

                    } else if (!uiState.isLoadingRecipes && hasRecipes) {
                        item {
                            RecipeSectionInnerRow(isLast = true) {
                                Text(
                                    text = stringResource(R.string.tap_to_expand),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

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
                                        stringResource(R.string.sign_out),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        stringResource(R.string.sign_out_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { viewModel.logout() }, enabled = !uiState.isSaving
                                ) { Text(stringResource(R.string.exit)) }
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
                    Text(stringResource(R.string.profile_load_error))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadProfile() }) { Text(stringResource(R.string.retry)) }
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
    title: String, subtitle: String, expanded: Boolean, enabled: Boolean, onToggle: () -> Unit
) {
    val shapeTop = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    Surface(
        shape = shapeTop,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onToggle() }
            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
                contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeSectionInnerRow(
    isLast: Boolean, content: @Composable () -> Unit
) {
    val shape = when {
        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
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
    recipe: Recipe, showDivider: Boolean, onClick: () -> Unit, onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
                text = stringResource(R.string.recipe_summary, RecipeCategory.toDisplayName(recipe.category), recipe.durationMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onClick) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_recipe))
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_recipe))
        }
    }

    if (showDivider) HorizontalDivider()
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
private fun EditNameDialog(
    initialValue: String, saving: Boolean, onDismiss: () -> Unit, onSave: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.edit_name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )
                if (saving) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.saving))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(value) }, enabled = !saving && value.isNotBlank()
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = { if (!saving) onDismiss() }) { Text(stringResource(R.string.cancel)) }
        })
}
