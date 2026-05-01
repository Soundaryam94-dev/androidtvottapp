package com.example.myottapp.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "ott_prefs")

class PreferencesManager(private val context: Context){
    companion object {
        val KEY_AUTH_TOKEN    = stringPreferencesKey("auth_token")
        val KEY_VIDEO_QUALITY = stringPreferencesKey("video_quality")
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.dataStore.data.first()
        return !prefs[KEY_AUTH_TOKEN].isNullOrEmpty()
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { it[KEY_AUTH_TOKEN] = token }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}