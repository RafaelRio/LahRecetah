package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.data.repository.RecipeRepository
import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecipeByIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(recipeId: String): Flow<Recipe?> {
        return recipeRepository.observeRecipeById(recipeId)
    }
}