package com.rafario.lahrecetah.domain.model

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdByUid: String = "",
    val createdByName: String = ""
)
