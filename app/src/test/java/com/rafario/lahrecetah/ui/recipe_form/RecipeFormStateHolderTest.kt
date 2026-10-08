package com.rafario.lahrecetah.ui.recipe_form

import com.rafario.lahrecetah.domain.model.Recipe
import com.rafario.lahrecetah.domain.model.RecipeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeFormStateHolderTest {
    @Test
    fun `updates scalar fields and clamps difficulty`() {
        val holder = RecipeFormStateHolder()

        holder.onTitleChanged("Tortilla")
        holder.onDescriptionChanged("Casera")
        holder.onDurationChanged("15")
        holder.onCategoryChanged(RecipeCategory.MAIN_COURSE)
        holder.onDifficultyChanged(7)

        assertEquals("Tortilla", holder.state.value.title)
        assertEquals("Casera", holder.state.value.description)
        assertEquals("15", holder.state.value.durationText)
        assertEquals(RecipeCategory.MAIN_COURSE, holder.state.value.category)
        assertEquals(5, holder.state.value.difficulty)

        holder.onDifficultyChanged(0)
        assertEquals(1, holder.state.value.difficulty)
    }

    @Test
    fun `manages ingredient and step rows with focus`() {
        val holder = RecipeFormStateHolder()

        holder.addIngredientRow()
        holder.updateIngredient(0, "Huevos")
        holder.addIngredientRow()
        assertEquals(listOf("Huevos", ""), holder.state.value.ingredients)
        assertEquals(1, holder.state.value.focusedIngredientIndex)

        holder.removeIngredient(0)
        assertEquals(listOf(""), holder.state.value.ingredients)
        assertNull(holder.state.value.focusedIngredientIndex)
        holder.updateIngredient(4, "ignored")
        assertEquals(listOf(""), holder.state.value.ingredients)

        holder.addStepRow()
        holder.updateStep(0, "Batir")
        holder.addStepRow()
        assertEquals(listOf("Batir", ""), holder.state.value.steps)
        assertEquals(1, holder.state.value.focusedStepIndex)

        holder.clearStepFocus()
        assertNull(holder.state.value.focusedStepIndex)
        holder.removeStep(0)
        assertEquals(listOf(""), holder.state.value.steps)
        holder.removeStep(4)
        assertEquals(listOf(""), holder.state.value.steps)
    }

    @Test
    fun `selecting local image keeps existing remote image and remove clears both`() {
        val holder = RecipeFormStateHolder()
        val remoteUrl = "https://example.com/recipe.jpg"

        holder.populateFrom(Recipe(imageUrl = remoteUrl))
        holder.onImageSelected("content://picked-image")

        assertEquals(remoteUrl, holder.state.value.existingImageUrl)
        assertEquals("content://picked-image", holder.state.value.selectedLocalImageUri)

        holder.removeImage()

        assertNull(holder.state.value.existingImageUrl)
        assertNull(holder.state.value.selectedLocalImageUri)
    }

    @Test
    fun `populates editable fields from recipe without local image`() {
        val holder = RecipeFormStateHolder()
        val recipe = Recipe(
            title = "Tortilla",
            description = "Casera",
            ingredients = listOf("Huevos"),
            steps = listOf("Batir"),
            durationMinutes = 15,
            category = RecipeCategory.MAIN_COURSE,
            difficulty = 8,
            imageUrl = "https://example.com/recipe.jpg"
        )

        holder.populateFrom(recipe)

        assertEquals("Tortilla", holder.state.value.title)
        assertEquals("15", holder.state.value.durationText)
        assertEquals(listOf("Huevos"), holder.state.value.ingredients)
        assertEquals(5, holder.state.value.difficulty)
        assertEquals(recipe.imageUrl, holder.state.value.existingImageUrl)
        assertNull(holder.state.value.selectedLocalImageUri)
    }

    @Test
    fun `reset restores initial form state`() {
        val holder = RecipeFormStateHolder()
        holder.onTitleChanged("Tortilla")
        holder.onImageSelected("content://picked-image")

        holder.reset()

        assertEquals(RecipeFormState(), holder.state.value)
    }
}