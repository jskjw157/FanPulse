package com.aos.fanpulse.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.aos.fanpulse.domain.repository.SettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingDataStore by preferencesDataStore(name = "fanpulse_settings")

@Singleton
class SettingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingRepository {

    private val NOTIFICATION_KEY = booleanPreferencesKey("notification_on")

    override val isNotificationEnabled: Flow<Boolean> = context.settingDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[NOTIFICATION_KEY] ?: true
        }

    override suspend fun setNotificationEnabled(isEnabled: Boolean) {
        context.settingDataStore.edit { preferences ->
            preferences[NOTIFICATION_KEY] = isEnabled
        }
    }
}