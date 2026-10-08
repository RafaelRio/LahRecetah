package com.rafario.lahrecetah.domain.validation

class RecipeValidationException(
    val reason: RecipeValidationError
) : Exception()