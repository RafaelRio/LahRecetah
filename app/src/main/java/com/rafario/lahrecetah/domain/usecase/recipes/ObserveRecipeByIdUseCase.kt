package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRecipeByIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String): Flow<Recipe?> {
        return recipeRepository.observeRecipeById(recipeId)
    }
}