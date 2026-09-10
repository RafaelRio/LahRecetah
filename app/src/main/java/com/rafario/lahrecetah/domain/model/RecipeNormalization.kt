package com.rafario.lahrecetah.domain.model

fun Recipe.normalized(): Recipe = copy(
    title = title.trim(),
    description = description.trim(),
    ingredients = ingredients.map { it.trim() }.filter { it.isNotEmpty() },
    steps = steps.map { it.trim() }.filter { it.isNotEmpty() }
)
