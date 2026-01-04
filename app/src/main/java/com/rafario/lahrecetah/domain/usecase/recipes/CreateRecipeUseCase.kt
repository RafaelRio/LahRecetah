package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.data.repository.AuthRepository
import com.rafario.lahrecetah.data.repository.RecipeRepository
import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import javax.inject.Inject

class CreateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        title: String,
        description: String,
        ingredients: List<String>,
        steps: List<String>,
        category: RecipeCategory,
        durationMinutes: Int,
        difficulty: Int,
        imageUrl: String
    ): Result<Unit> {

        if (title.isBlank()) return Result.failure(
            IllegalArgumentException("El título es obligatorio")
        )

        if (ingredients.isEmpty()) return Result.failure(
            IllegalArgumentException("Debe haber al menos un ingrediente")
        )

        if (difficulty !in 1..5) return Result.failure(
            IllegalArgumentException("La dificultad debe estar entre 1 y 5")
        )

        val user = authRepository.getCurrentUser()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        val recipe = Recipe(
            title = title.trim(),
            description = description.trim(),
            ingredients = ingredients,
            steps = steps,
            category = category,
            durationMinutes = durationMinutes,
            difficulty = difficulty,
            createdByUid = user.uid,
            createdByName = user.displayName.orEmpty(),
            imageUrl = imageUrl
        )

        return recipeRepository.createRecipe(recipe)
    }
}