package com.rafario.lahrecetah.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_prefs"
)

class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    suspend fun saveRememberMe(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME] = value
        }
    }

    val rememberMeFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[REMEMBER_ME] ?: false
        }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}