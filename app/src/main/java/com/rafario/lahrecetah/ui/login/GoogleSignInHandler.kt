package com.rafario.lahrecetah.ui.login

import android.content.Context
import android.content.MutableContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException

class GoogleSignInHandler(
    private val credentialManager: CredentialManager
) {

    suspend fun signIn(
        context: Context,
        serverClientId: String
    ): GoogleSignInResult {
        return try {
            val googleOption = GetSignInWithGoogleOption.Builder(
                serverClientId
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            val response = credentialManager.getCredential(
                context = MutableContextWrapper(context),
                request = request
            )

            val credential = response.credential

            if (
                credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return GoogleSignInResult.Error(
                    IllegalStateException("Credencial de Google no válida")
                )
            }

            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            GoogleSignInResult.Success(
                idToken = googleCredential.idToken
            )

        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled

        } catch (_: NoCredentialException) {
            GoogleSignInResult.NoCredential

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            GoogleSignInResult.Error(e)
        }
    }

    companion object {
        fun create(context: Context): GoogleSignInHandler {
            return GoogleSignInHandler(
                CredentialManager.create(context)
            )
        }
    }
}

sealed interface GoogleSignInResult {

    data class Success(
        val idToken: String
    ) : GoogleSignInResult

    data object Cancelled : GoogleSignInResult

    data object NoCredential : GoogleSignInResult

    data class Error(
        val cause: Throwable
    ) : GoogleSignInResult
}