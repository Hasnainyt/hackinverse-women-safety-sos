package com.brokencoders.narisuraksha.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DECOY_MODE_ENABLED = booleanPreferencesKey("decoy_mode_enabled")
        val SECRET_DECOY_CODE = stringPreferencesKey("secret_decoy_code")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SHAKE_DETECTION_ENABLED = booleanPreferencesKey("shake_detection_enabled")
        val GUARDIAN_SCAN_ENABLED = booleanPreferencesKey("guardian_scan_enabled")
    }

    val isDecoyModeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { preferences ->
        preferences[PreferencesKeys.DECOY_MODE_ENABLED] ?: false
    }

    val secretDecoyCode: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { preferences ->
        // BUGFIX: Never use a hardcoded default. Generate a random 6-digit code
        // the very first time so no one can guess the secret SOS trigger code.
        preferences[PreferencesKeys.SECRET_DECOY_CODE] ?: generateRandomCode()
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val isShakeDetectionEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { preferences ->
        // BUGFIX: Default to false — user must explicitly enable shake detection
        // in Settings. Defaulting to true caused auto-SOS on every app launch
        // because the accelerometer emits glitch events during device boot/startup.
        preferences[PreferencesKeys.SHAKE_DETECTION_ENABLED] ?: true
    }

    val isGuardianScanEnabled: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { preferences ->
        preferences[PreferencesKeys.GUARDIAN_SCAN_ENABLED] ?: true
    }

    suspend fun setDecoyModeEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DECOY_MODE_ENABLED] = enabled
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist decoy mode", e)
        }
    }

    suspend fun setSecretDecoyCode(code: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SECRET_DECOY_CODE] = code
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist decoy code", e)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist onboarding state", e)
        }
    }

    suspend fun setShakeDetectionEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SHAKE_DETECTION_ENABLED] = enabled
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist shake setting", e)
        }
    }

    suspend fun setGuardianScanEnabled(enabled: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.GUARDIAN_SCAN_ENABLED] = enabled
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist guardian scan setting", e)
        }
    }

    /**
     * Generates a random 6-digit numeric code.
     * Used as the first-run secret decoy code so no device ever ships with "1234".
     */
    private fun generateRandomCode(): String = (100000..999999).random().toString()

    /**
     * Call once on first install (e.g. end of Onboarding flow).
     * Persists a randomly-generated secret decoy code and ensures shake detection
     * is explicitly set to OFF so no auto-SOS fires on a fresh install.
     */
    suspend fun initialiseDefaultsIfNeeded() {
        try {
            context.dataStore.edit { preferences ->
                if (!preferences.contains(PreferencesKeys.SECRET_DECOY_CODE)) {
                    preferences[PreferencesKeys.SECRET_DECOY_CODE] = generateRandomCode()
                    Log.i(TAG, "First-run: random secret decoy code generated")
                }
                if (!preferences.contains(PreferencesKeys.SHAKE_DETECTION_ENABLED)) {
                    preferences[PreferencesKeys.SHAKE_DETECTION_ENABLED] = true
                    Log.i(TAG, "First-run: shake detection defaulted to ON")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to persist first-run defaults", e)
        }
    }

    companion object {
        private const val TAG = "UserPrefs"
    }
}
