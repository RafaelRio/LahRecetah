package com.rafario.lahrecetah.domain.usecase.recipes

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.observeRecipes()
    }
}