package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import com.rafario.lahrecetah.domain.model.normalized
import com.rafario.lahrecetah.domain.repository.AuthRepository
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import com.rafario.lahrecetah.domain.validation.RecipeValidator
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

        val error = RecipeValidator.validate(
            title, description, ingredients, steps, durationMinutes, difficulty
        )
        if (error != null) return Result.failure(IllegalArgumentException(error.message))

        val user = authRepository.getCurrentUser()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        val recipe = Recipe(
            title = title,
            description = description,
            ingredients = ingredients,
            steps = steps,
            category = category,
            durationMinutes = durationMinutes,
            difficulty = difficulty,
            createdByUid = user.uid,
            createdByName = user.displayName.orEmpty(),
            imageUrl = imageUrl
        ).normalized()

        return recipeRepository.createRecipe(recipe)
    }
}