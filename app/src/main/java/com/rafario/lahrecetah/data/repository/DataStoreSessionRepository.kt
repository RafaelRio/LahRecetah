package com.rafario.lahrecetah.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.rafario.lahrecetah.domain.repository.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_prefs"
)

class DataStoreSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {

    companion object {
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    override suspend fun saveRememberMe(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME] = value
        }
    }

    override val rememberMeFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[REMEMBER_ME] ?: false
        }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}