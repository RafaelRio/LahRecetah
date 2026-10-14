package com.rafario.lahrecetah.data.repository

import com.rafario.lahrecetah.domain.recipe.RecipeUpdateCommittedCancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class RecipeImageCleanupTest {

    @Test
    fun `cleanup cancellation signals confirmed update and preserves cause`() = runTest {
        val cancellation = CancellationException("cleanup cancelled")

        try {
            cleanupImageAfterConfirmedUpdate(
                imageUrl = "https://example.com/old.jpg",
                deleteImage = { throw cancellation }
            )
            fail("Expected RecipeUpdateCommittedCancellationException")
        } catch (e: RecipeUpdateCommittedCancellationException) {
            assertSame(cancellation, e.cause)
        }
    }

    @Test
    fun `cleanup cancellation wrapped in result preserves confirmed update signal`() = runTest {
        val cancellation = CancellationException("cleanup cancelled")

        try {
            cleanupImageAfterConfirmedUpdate(
                imageUrl = "https://example.com/old.jpg",
                deleteImage = { Result.failure(cancellation) }
            )
            fail("Expected RecipeUpdateCommittedCancellationException")
        } catch (e: RecipeUpdateCommittedCancellationException) {
            assertSame(cancellation, e.cause)
        }
    }

    @Test
    fun `ordinary cleanup failure in result does not propagate`() = runTest {
        val imageUrl = "https://example.com/old.jpg"
        var requestedImageUrl: String? = null

        cleanupImageAfterConfirmedUpdate(
            imageUrl = imageUrl,
            deleteImage = { url ->
                requestedImageUrl = url
                Result.failure(IllegalStateException("delete failed"))
            }
        )

        assertEquals(imageUrl, requestedImageUrl)
    }

    @Test
    fun `ordinary cleanup exception does not propagate`() = runTest {
        val imageUrl = "https://example.com/old.jpg"
        var requestedImageUrl: String? = null

        cleanupImageAfterConfirmedUpdate(
            imageUrl = imageUrl,
            deleteImage = { url ->
                requestedImageUrl = url
                throw IllegalStateException("delete failed")
            }
        )

        assertEquals(imageUrl, requestedImageUrl)
    }

    @Test
    fun `successful cleanup deletes the requested image once`() = runTest {
        val imageUrl = "https://example.com/old.jpg"
        val requestedImageUrls = mutableListOf<String>()

        cleanupImageAfterConfirmedUpdate(
            imageUrl = imageUrl,
            deleteImage = { url ->
                requestedImageUrls += url
                Result.success(Unit)
            }
        )

        assertEquals(listOf(imageUrl), requestedImageUrls)
    }
}