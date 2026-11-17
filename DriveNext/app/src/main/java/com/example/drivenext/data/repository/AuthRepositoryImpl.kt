package com.example.drivenext.data.repository

import android.content.Context
import com.example.drivenext.domain.AuthError
import com.example.drivenext.domain.AuthException
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.RegistrationData
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import java.util.UUID

/**
 * Локальная реализация-заглушка: бэкенда в лабе нет, поэтому пользователи
 * лежат в SharedPreferences, а "токен" генерируется на устройстве.
 * Когда появится сервер, меняется только этот класс: интерфейс и экраны остаются.
 */
class AuthRepositoryImpl(context: Context) : AuthRepository {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val users = appContext.getSharedPreferences("auth_users", Context.MODE_PRIVATE)

    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    override fun isEmailRegistered(email: String): Boolean = users.contains(normalize(email))

    override fun login(email: String, password: String): Result<Unit> {
        val key = normalize(email)
        val raw = users.getString(key, null) ?: return invalidCredentials()
        return try {
            val record = JSONObject(raw)
            val expected = record.getString(FIELD_HASH)
            val actual = hash(password, record.getString(FIELD_SALT))
            if (!MessageDigest.isEqual(expected.toByteArray(), actual.toByteArray())) {
                invalidCredentials()
            } else {
                saveSession(key, newToken())
                Result.success(Unit)
            }
        } catch (e: JSONException) {
            invalidCredentials()
        }
    }

    override fun loginWithGoogle(): Result<Unit> {
        // TODO: заменить на Credential Manager + Google Sign-In, когда будет Web Client ID
        //  из Google Cloud Console. Сейчас вход "успешен" всегда и токен фиктивный.
        saveSession(GOOGLE_STUB_EMAIL, "google-stub-" + newToken())
        return Result.success(Unit)
    }

    override fun register(data: RegistrationData): Result<Unit> {
        val key = normalize(data.email)
        if (users.contains(key)) return Result.failure(AuthException(AuthError.EMAIL_TAKEN))

        val salt = newSalt()
        val record = JSONObject().apply {
            put(FIELD_SALT, salt)
            put(FIELD_HASH, hash(data.password, salt))
            put("lastName", data.lastName)
            put("firstName", data.firstName)
            put("patronymic", data.patronymic)
            put("birthDate", data.birthDate)
            put("gender", data.gender?.name ?: "")
            put("licenseNumber", data.licenseNumber)
            put("licenseIssueDate", data.licenseIssueDate)
            // С реальным сервером фото отправляются multipart-запросом, а не хранятся как Uri.
            put("avatarUri", data.avatarUri ?: "")
            put("licensePhotoUri", data.licensePhotoUri ?: "")
            put("passportPhotoUri", data.passportPhotoUri ?: "")
        }
        users.edit().putString(key, record.toString()).apply()
        saveSession(key, newToken())
        return Result.success(Unit)
    }

    override fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_TOKEN)
            .remove(KEY_EMAIL)
            .apply()
    }

    private fun saveSession(email: String, token: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    private fun invalidCredentials(): Result<Unit> =
        Result.failure(AuthException(AuthError.INVALID_CREDENTIALS))

    private fun normalize(email: String) = email.trim().lowercase(Locale.ROOT)

    private fun newToken() = UUID.randomUUID().toString()

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_TOKEN = "auth_token"
        const val KEY_EMAIL = "auth_email"
        const val FIELD_SALT = "salt"
        const val FIELD_HASH = "hash"
        const val GOOGLE_STUB_EMAIL = "google_user"
    }
}
