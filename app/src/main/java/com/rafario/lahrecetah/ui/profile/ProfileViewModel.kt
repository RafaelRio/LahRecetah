package com.rafario.lahrecetah.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.domain.repository.UserRepository
import com.rafario.lahrecetah.domain.usecase.users.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    private var recipesJob: Job? = null

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
                val profile = userRepository.getUserProfile(authUser.email)
                    ?: UserProfile(
                        uid = authUser.uid,
                        name = authUser.displayName ?: "Usuario",
                        email = authUser.email
                    )

                _uiState.update { it.copy(isLoading = false, profile = profile) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error cargando perfil"
                    )
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
                userRepository.updateUserName(current.email, newName.trim())

                try {
                    authRepository.updateDisplayName(newName.trim())
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // El nombre ya se actualizó en Firestore; no bloqueamos el flujo por Auth.
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        profile = current.copy(name = newName.trim())
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "No se pudo actualizar el nombre"
                    )
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
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
        recipesJob?.cancel()

        recipesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecipes = true) }

            recipeRepository.observeRecipesByUser(uid)
                .catch { e ->
                    if (e is CancellationException) throw e
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

            val result = recipeRepository.deleteRecipe(pending.id)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeletingRecipe = false,
                            pendingDeleteRecipe = null,
                            myRecipes = it.myRecipes.filterNot { r -> r.id == pending.id }
                        )
                    }
                },
                onFailure = { e ->
                    if (e is CancellationException) throw e
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

    // Recetas del usuario
    val isLoadingRecipes: Boolean = false,
    val myRecipes: List<Recipe> = emptyList(),

    // Estado de borrado
    val pendingDeleteRecipe: PendingDeleteRecipe? = null,
    val isDeletingRecipe: Boolean = false
)