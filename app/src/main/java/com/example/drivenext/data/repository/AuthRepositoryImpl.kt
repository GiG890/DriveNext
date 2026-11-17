package com.example.drivenext.data.repository

import android.content.Context
import android.net.Uri
import com.example.drivenext.data.remote.SessionManager
import com.example.drivenext.data.remote.SupabaseConfig
import com.example.drivenext.data.remote.SupabaseHttp
import com.example.drivenext.data.remote.SupabaseHttp.Companion.jsonBody
import com.example.drivenext.data.remote.apiCall
import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import com.example.drivenext.domain.AuthRepository
import com.example.drivenext.domain.DateRules
import com.example.drivenext.domain.Gender
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.domain.UserProfile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Авторизация и профиль через Supabase (Auth + PostgREST + Storage).
 * Параметры по умолчанию позволяют создавать репозиторий так же, как раньше: AuthRepositoryImpl(context).
 */
class AuthRepositoryImpl(
    context: Context,
    private val http: SupabaseHttp = SupabaseHttp(),
    private val session: SessionManager = SessionManager(context, http)
) : AuthRepository {

    private val appContext = context.applicationContext

    override fun isLoggedIn(): Boolean = session.isLoggedIn()

    override suspend fun login(email: String, password: String): Result<Unit> = apiCall {
        signIn(email.trim(), password)
    }

    override suspend fun loginWithGoogle(): Result<Unit> {
        // TODO: нужен Google OAuth (Credential Manager + провайдер Google в Supabase Auth).
        //  Пока кнопка есть, но вход не выполняется.
        return Result.failure(AppException(AppError.GOOGLE_FAILED))
    }

    override suspend fun register(data: RegistrationData): Result<Unit> = apiCall {
        signUpOrSignIn(data.email.trim(), data.password)

        val userId = session.userId ?: throw AppException(AppError.UNKNOWN)
        val token = session.validAccessToken()

        var avatarUrl: String? = null
        if (data.avatarUri != null) {
            upload("avatars", "$userId/avatar", data.avatarUri, token)
            avatarUrl = publicAvatarUrl(userId)
        }
        val licensePath = "$userId/license"
        val passportPath = "$userId/passport"
        data.licensePhotoUri?.let { upload("documents", licensePath, it, token) }
        data.passportPhotoUri?.let { upload("documents", passportPath, it, token) }

        val row = JSONObject()
            .put("id", userId)
            .put("email", data.email.trim())
            .put("last_name", data.lastName)
            .put("first_name", data.firstName)
            .put("patronymic", data.patronymic.ifBlank { null })
            .put("birth_date", DateRules.parse(data.birthDate)?.toString())
            .put("gender", data.gender?.name)
            .put("license_number", data.licenseNumber)
            .put("license_issue_date", DateRules.parse(data.licenseIssueDate)?.toString())
            .put("avatar_url", avatarUrl)
            .put("license_photo_path", licensePath)
            .put("passport_photo_path", passportPath)

        // upsert: если регистрация оборвалась на полпути, повторная попытка доделает профиль
        val response = http.execute(
            "POST", "/rest/v1/profiles?on_conflict=id", token,
            jsonBody(row.toString()),
            mapOf("Prefer" to "resolution=merge-duplicates,return=minimal")
        )
        if (!response.isSuccessful) throw response.toException()
    }

    override suspend fun getProfile(): Result<UserProfile> = apiCall {
        val token = session.validAccessToken()
        val userId = session.userId ?: throw AppException(AppError.SESSION_EXPIRED)
        val email = session.email.orEmpty()

        val response = http.execute("GET", "/rest/v1/profiles?id=eq.$userId&select=*", token)
        if (!response.isSuccessful) throw response.toException()

        val rows = JSONArray(response.body)
        if (rows.length() == 0) {
            // Профиль ещё не создан (например, регистрация прервалась): показываем хотя бы почту
            UserProfile(userId, email, "", "", null, null, null)
        } else {
            val row = rows.getJSONObject(0)
            UserProfile(
                userId = userId,
                email = row.optStringOrNull("email") ?: email,
                firstName = row.optStringOrNull("first_name").orEmpty(),
                lastName = row.optStringOrNull("last_name").orEmpty(),
                gender = row.optStringOrNull("gender")?.let { name ->
                    Gender.values().firstOrNull { it.name == name }
                },
                avatarUrl = row.optStringOrNull("avatar_url"),
                createdAt = row.optStringOrNull("created_at")
            )
        }
    }

    override suspend fun updateAvatar(imageUri: String): Result<Unit> = apiCall {
        val token = session.validAccessToken()
        val userId = session.userId ?: throw AppException(AppError.SESSION_EXPIRED)

        upload("avatars", "$userId/avatar", imageUri, token)

        val body = JSONObject().put("avatar_url", publicAvatarUrl(userId)).toString()
        val response = http.execute(
            "PATCH", "/rest/v1/profiles?id=eq.$userId", token,
            jsonBody(body),
            mapOf("Prefer" to "return=minimal")
        )
        if (!response.isSuccessful) throw response.toException()
    }

    override fun logout() {
        session.clear()
    }

    // ---------------------------------------------------------------------

    private fun signIn(email: String, password: String) {
        val body = JSONObject().put("email", email).put("password", password).toString()
        val response = http.execute(
            "POST", "/auth/v1/token?grant_type=password", body = jsonBody(body)
        )
        if (!response.isSuccessful) throw response.toException()
        session.save(JSONObject(response.body))
    }

    /**
     * Создаёт аккаунт. Если такая почта уже есть, пробует войти с тем же паролем:
     * так можно закончить регистрацию, которая оборвалась после создания аккаунта.
     */
    private fun signUpOrSignIn(email: String, password: String) {
        val body = JSONObject().put("email", email).put("password", password).toString()
        val response = http.execute("POST", "/auth/v1/signup", body = jsonBody(body))

        if (response.isSuccessful) {
            val json = JSONObject(response.body)
            // Без access_token Supabase ждёт подтверждения почты (настройка "Confirm email")
            if (!json.has("access_token")) throw AppException(AppError.EMAIL_NOT_CONFIRMED)
            session.save(json)
            return
        }

        val error = response.toException()
        if (error.error != AppError.EMAIL_TAKEN) throw error
        try {
            signIn(email, password)
        } catch (e: AppException) {
            if (e.error == AppError.INVALID_CREDENTIALS) throw error else throw e
        }
    }

    private fun upload(bucket: String, path: String, uriString: String, token: String) {
        val uri = Uri.parse(uriString)
        val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw AppException(AppError.UNKNOWN, "Не удалось прочитать выбранный файл")
        val mime = appContext.contentResolver.getType(uri) ?: "image/jpeg"

        val response = http.execute(
            "POST", "/storage/v1/object/$bucket/$path", token,
            bytes.toRequestBody(mime.toMediaType()),
            mapOf("x-upsert" to "true")
        )
        if (!response.isSuccessful) throw response.toException()
    }

    /** Публичная ссылка на аватар. Параметр v меняется, чтобы загрузчик картинок не показывал старую версию из кэша. */
    private fun publicAvatarUrl(userId: String): String =
        "${SupabaseConfig.URL}/storage/v1/object/public/avatars/$userId/avatar?v=${System.currentTimeMillis()}"

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
