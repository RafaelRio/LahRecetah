package com.rafario.lahrecetah.ui.recipe_detail

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `emits recipe from observed flow`() = runTest {
        val source = MutableSharedFlow<Recipe?>()
        val repository = FakeRecipeRepository().apply {
            recipeById = source
        }
        val viewModel = RecipeDetailViewModel(repository)
        val recipe = Recipe(id = "recipe-id", title = "Tortilla")

        viewModel.loadRecipe(recipe.id)
        runCurrent()

        assertEquals(RecipeDetailUiState(isLoading = true), viewModel.uiState.value)

        source.emit(recipe)
        runCurrent()

        assertEquals(
            RecipeDetailUiState(isLoading = false, recipe = recipe),
            viewModel.uiState.value
        )
    }

    @Test
    fun `missing recipe emits not found error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            recipeById = flowOf(null)
        }
        val viewModel = RecipeDetailViewModel(repository)

        viewModel.loadRecipe("missing-id")
        runCurrent()

        assertEquals(
            RecipeDetailUiState(
                isLoading = false,
                recipe = null,
                error = "La receta no existe"
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun `flow failure becomes error state`() = runTest {
        val repository = FakeRecipeRepository().apply {
            recipeById = flow { throw IllegalStateException("read failed") }
        }
        val viewModel = RecipeDetailViewModel(repository)

        viewModel.loadRecipe("recipe-id")
        runCurrent()

        assertEquals(
            RecipeDetailUiState(
                isLoading = false,
                error = "read failed"
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun `cancellation does not become UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            recipeById = flow { throw CancellationException("cancel") }
        }
        val viewModel = RecipeDetailViewModel(repository)

        viewModel.loadRecipe("recipe-id")
        runCurrent()

        assertEquals(RecipeDetailUiState(isLoading = true), viewModel.uiState.value)
    }
}