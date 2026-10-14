package com.rafario.lahrecetah.domain.recipe

import kotlin.coroutines.cancellation.CancellationException

class RecipeUpdateCommittedCancellationException(
    cause: CancellationException
) : CancellationException() {

    init {
        initCause(cause)
    }
}