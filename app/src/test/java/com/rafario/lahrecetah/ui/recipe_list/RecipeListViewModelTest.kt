package com.rafario.lahrecetah.ui.recipe_list

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.usecase.recipes.GetRecipesUseCase
import com.rafario.lahrecetah.testing.FakeRecipeRepository
import com.rafario.lahrecetah.testing.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeListViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun `starts loading and emits successive recipes including empty list`() = runTest {
        val source = MutableSharedFlow<List<Recipe>>()
        val repository = FakeRecipeRepository().apply { recipes = source }
        val vm = RecipeListViewModel(GetRecipesUseCase(repository))
        assertEquals(RecipeListUiState.Loading, vm.uiState.value)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        runCurrent()
        val recipes = listOf(Recipe(id = "1", title = "Tortilla"))
        source.emit(recipes)
        runCurrent()
        assertEquals(RecipeListUiState.Success(recipes), vm.uiState.value)
        source.emit(emptyList())
        runCurrent()
        assertEquals(RecipeListUiState.Success(emptyList()), vm.uiState.value)
    }

    @Test fun `flow failure becomes error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            recipes = flow { throw IllegalStateException("read failed") }
        }
        val vm = RecipeListViewModel(GetRecipesUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        runCurrent()
        assertEquals(RecipeListUiState.Error("read failed"), vm.uiState.value)
    }

    @Test fun `cancelled flow does not emit UI error`() = runTest {
        val repository = FakeRecipeRepository().apply {
            recipes = flow { throw CancellationException("cancel") }
        }
        val vm = RecipeListViewModel(GetRecipesUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        runCurrent()
        assertEquals(RecipeListUiState.Loading, vm.uiState.value)
    }
}
