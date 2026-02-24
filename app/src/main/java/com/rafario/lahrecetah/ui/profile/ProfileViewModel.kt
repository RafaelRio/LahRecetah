package com.rafario.lahrecetah.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.data.repository.RecipeRepository
import com.rafario.lahrecetah.data.repository.UserFirestoreRepository
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.usecase.users.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository,
    private val userFirestoreRepository: UserFirestoreRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No hay sesión activa."
                    )
                }
                return@launch
            }

            observeMyRecipes(authUser.uid)

            try {
                val profile = userFirestoreRepository.getUserProfile(authUser.email)
                    ?: UserProfile(
                        uid = authUser.uid,
                        name = authUser.displayName ?: "Usuario",
                        email = authUser.email
                    )

                _uiState.update { it.copy(isLoading = false, profile = profile) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Error cargando perfil")
                }
            }
        }
    }

    fun updateName(newName: String) {
        val current = _uiState.value.profile ?: return
        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                userFirestoreRepository.updateUserName(current.email, newName.trim())

                // opcional (si implementaste el paso 2)
                runCatching { authRepository.updateDisplayName(newName.trim()) }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        profile = current.copy(name = newName.trim())
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "No se pudo actualizar el nombre"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _logoutEvent.emit(Unit)
        }
    }

    private fun observeMyRecipes(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecipes = true) }

            recipeRepository.observeRecipesByUser(uid)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingRecipes = false,
                            errorMessage = e.message ?: "Error cargando tus recetas"
                        )
                    }
                }
                .collectLatest { recipes ->
                    _uiState.update {
                        it.copy(
                            isLoadingRecipes = false,
                            myRecipes = recipes
                        )
                    }
                }
        }
    }

    fun askDeleteRecipe(recipeId: String, recipeTitle: String) {
        _uiState.update {
            it.copy(pendingDeleteRecipe = PendingDeleteRecipe(recipeId, recipeTitle))
        }
    }

    fun cancelDeleteRecipe() {
        _uiState.update { it.copy(pendingDeleteRecipe = null) }
    }

    fun confirmDeleteRecipe() {
        val pending = _uiState.value.pendingDeleteRecipe ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingRecipe = true, errorMessage = null) }

            val result = recipeRepository.deleteRecipe(pending.id) // :contentReference[oaicite:13]{index=13}
            result.fold(
                onSuccess = {
                    // quitamos de la lista local (además el Flow se actualizará por snapshot)
                    _uiState.update {
                        it.copy(
                            isDeletingRecipe = false,
                            pendingDeleteRecipe = null,
                            myRecipes = it.myRecipes.filterNot { r -> r.id == pending.id }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isDeletingRecipe = false,
                            pendingDeleteRecipe = null,
                            errorMessage = e.message ?: "No se pudo eliminar la receta"
                        )
                    }
                }
            )
        }
    }
}

data class PendingDeleteRecipe(
    val id: String,
    val title: String
)

data class ProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,

    // NUEVO: Mis recetas
    val isLoadingRecipes: Boolean = false,
    val myRecipes: List<Recipe> = emptyList(),

    // NUEVO: Borrado
    val pendingDeleteRecipe: PendingDeleteRecipe? = null,
    val isDeletingRecipe: Boolean = false
)