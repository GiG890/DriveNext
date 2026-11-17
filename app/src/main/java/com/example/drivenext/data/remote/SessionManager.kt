package com.example.drivenext.data.remote

import android.content.Context
import com.example.drivenext.data.remote.SupabaseHttp.Companion.jsonBody
import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import org.json.JSONObject

/**
 * Хранит активную сессию (токены, id и почту пользователя) и обновляет токен, когда он вот-вот истечёт.
 * Токен доступа в Supabase живёт около часа, поэтому без обновления приложение "разлогинилось" бы само.
 */
class SessionManager(
    context: Context,
    private val http: SupabaseHttp
) {
    private val prefs = context.applicationContext
        .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    /** Сессия считается активной, только если есть refresh-токен: старые заглушечные сессии из лабы 2 сюда не попадают. */
    fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false) && !prefs.getString(KEY_REFRESH, null).isNullOrEmpty()

    val userId: String? get() = prefs.getString(KEY_USER_ID, null)

    val email: String? get() = prefs.getString(KEY_EMAIL, null)

    /** Сохраняет ответ Supabase Auth (вход, регистрация или обновление токена). */
    fun save(session: JSONObject) {
        val user = session.optJSONObject("user")
        val expiresAt = if (session.has("expires_at")) {
            session.optLong("expires_at")
        } else {
            System.currentTimeMillis() / 1000 + session.optLong("expires_in", 3600)
        }
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_ACCESS, session.getString("access_token"))
            .putString(KEY_REFRESH, session.optString("refresh_token"))
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putString(KEY_USER_ID, user?.optString("id"))
            .putString(KEY_EMAIL, user?.optString("email"))
            .apply()
    }

    /** Возвращает действующий токен доступа, при необходимости обновляя его. Блокирующий вызов. */
    @Synchronized
    fun validAccessToken(): String {
        val access = prefs.getString(KEY_ACCESS, null)
        val refresh = prefs.getString(KEY_REFRESH, null)
        if (access.isNullOrEmpty() || refresh.isNullOrEmpty()) {
            throw AppException(AppError.SESSION_EXPIRED)
        }

        val now = System.currentTimeMillis() / 1000
        if (prefs.getLong(KEY_EXPIRES_AT, 0L) - now > REFRESH_MARGIN_SECONDS) return access

        val body = JSONObject().put("refresh_token", refresh).toString()
        val response = http.execute(
            "POST", "/auth/v1/token?grant_type=refresh_token", body = jsonBody(body)
        )
        if (!response.isSuccessful) {
            if (response.code in 400..499) {
                clear()
                throw AppException(AppError.SESSION_EXPIRED)
            }
            throw response.toException()
        }
        val json = JSONObject(response.body)
        save(json)
        return json.getString("access_token")
    }

    fun clear() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .apply()
    }

    private companion object {
        const val KEY_LOGGED_IN = "is_logged_in"
        const val KEY_ACCESS = "sb_access_token"
        const val KEY_REFRESH = "sb_refresh_token"
        const val KEY_EXPIRES_AT = "sb_expires_at"
        const val KEY_USER_ID = "sb_user_id"
        const val KEY_EMAIL = "sb_email"
        const val REFRESH_MARGIN_SECONDS = 60L
    }
}
