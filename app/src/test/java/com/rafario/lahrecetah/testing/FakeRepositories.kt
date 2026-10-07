package com.rafario.lahrecetah.testing

import com.rafario.lahrecetah.domain.model.AuthUser
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.UserProfile
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.domain.repository.SessionRepository
import com.rafario.lahrecetah.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository : AuthRepository {
    var user: AuthUser? = AuthUser("signed-in-uid", "cook@example.com", "Chef")
    var loginResult: Result<AuthUser> = Result.success(requireNotNull(user))
    var beforeLogin: suspend () -> Unit = {}
    var loginCalls = 0
    var googleCalls = 0
    var receivedToken: String? = null
    var receivedEmail: String? = null
    override suspend fun login(email: String, password: String): Result<AuthUser> {
        loginCalls++
        receivedEmail = email
        beforeLogin()
        return loginResult
    }
    override suspend fun loginWithGoogle(idToken: String): Result<AuthUser> {
        googleCalls++
        receivedToken = idToken
        beforeLogin()
        return loginResult
    }
    override fun getCurrentUser() = user
    override fun logout() { user = null }
    override suspend fun register(name: String, email: String, password: String): Result<AuthUser> =
        error("Not used in this test")
    override suspend fun updateDisplayName(newName: String) = Unit
}

class FakeRecipeRepository : RecipeRepository {
    var recipes: Flow<List<Recipe>> = flowOf(emptyList())
    var recipeById: Flow<Recipe?> = flowOf(null)
    val created = mutableListOf<Recipe>()
    var createResult: Result<Unit> = Result.success(Unit)
    var getRecipeByIdResult: Result<Recipe?> = Result.success(null)
    var requestedRecipeId: String? = null
    var beforeCreate: suspend () -> Unit = {}

    override suspend fun createRecipe(recipe: Recipe): Result<Unit> {
        beforeCreate()
        created += recipe
        return createResult
    }

    override suspend fun getRecipeById(recipeId: String): Result<Recipe?> {
        requestedRecipeId = recipeId
        return getRecipeByIdResult
    }

    override fun observeRecipes() = recipes
    override fun observeRecipeById(recipeId: String): Flow<Recipe?> = recipeById
    override fun observeRecipesByUser(uid: String) = recipes
    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> = error("Not used in this test")
    override suspend fun deleteRecipe(recipeId: String): Result<Unit> = error("Not used in this test")
    override suspend fun uploadRecipeImage(uri: String): String = error("Not used in this test")
}

class FakeSessionRepository : SessionRepository {
    override val rememberMeFlow = MutableStateFlow(false)
    val savedValues = mutableListOf<Boolean>()
    var saveFailure: Exception? = null
    override suspend fun saveRememberMe(value: Boolean) {
        savedValues += value
        saveFailure?.let { throw it }
        rememberMeFlow.value = value
    }
    override suspend fun clear() { rememberMeFlow.value = false }
}

class FakeUserRepository : UserRepository {
    val profiles = mutableListOf<UserProfile>()
    override suspend fun userExists(email: String) = profiles.any { it.email == email }
    override suspend fun createUserProfile(profile: UserProfile) { profiles += profile }
    override suspend fun getUserProfile(email: String) = profiles.find { it.email == email }
    override suspend fun updateUserName(email: String, newName: String) = Unit
}
