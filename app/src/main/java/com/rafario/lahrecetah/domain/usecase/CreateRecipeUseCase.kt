package com.rafario.lahrecetah.domain.usecase

import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.data.repository.RecipeRepository
import com.rafario.lahrecetah.domain.model.Recipe
import javax.inject.Inject

class CreateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>
    ): Result<Unit> {

        if (title.isBlank()) {
            return Result.failure(
                IllegalArgumentException("El título es obligatorio")
            )
        }

        val user = authRepository.getCurrentUser()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        val recipe = Recipe(
            title = title.trim(),
            description = description.trim(),
            ingredients = ingredients,
            steps = steps,
            createdByUid = user.uid,
            createdByName = user.displayName.orEmpty() // luego usaremos name real
        )

        return recipeRepository.createRecipe(recipe)
    }
}