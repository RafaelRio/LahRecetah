package com.rafario.lahrecetah.domain.usecase

import com.rafario.lahrecetah.data.repository.RecipeRepository
import com.rafario.lahrecetah.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        return repository.observeRecipes()
    }
}