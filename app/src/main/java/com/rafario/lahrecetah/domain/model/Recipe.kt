package com.rafario.lahrecetah.domain.model

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdByUid: String = "",
    val createdByName: String = "",
    val durationMinutes: Int = 0,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val difficulty: Int = 1,
    val imageUrl: String = ""
)

enum class RecipeCategory {
    STARTER,        // Entrante
    FIRST_COURSE,   // Primer plato
    MAIN_COURSE,    // Segundo plato
    DESSERT,        // Postre
    SWEET,          // Dulce / repostería
    SALAD,          // Ensalada
    SOUP,           // Sopa
    DRINK,          // Bebida
    OTHER;

    companion object {
        fun fromDisplayName(name: String): RecipeCategory {
            return when (name.lowercase()) {
                "entrante" -> STARTER
                "primer plato" -> FIRST_COURSE
                "segundo plato" -> MAIN_COURSE
                "postre" -> DESSERT
                "dulce" -> SWEET
                "ensalada" -> SALAD
                "sopa" -> SOUP
                "bebida" -> DRINK
                else -> OTHER
            }
        }

        fun toDisplayName(category: RecipeCategory): String {
            return when (category) {
                STARTER -> "Entrante"
                FIRST_COURSE -> "Primer plato"
                MAIN_COURSE -> "Segundo plato"
                DESSERT -> "Postre"
                SWEET -> "Dulce"
                SALAD -> "Ensalada"
                SOUP -> "Sopa"
                DRINK -> "Bebida"
                OTHER -> "Otro"
            }
        }
    }
}