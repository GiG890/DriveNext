package com.example.drivenext.data.remote

import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Тонкая обёртка над OkHttp для REST-запросов к Supabase (Auth, PostgREST, Storage).
 * Все методы блокирующие: вызывать только с фонового потока (см. apiCall).
 */
class SupabaseHttp {

    class Response(val code: Int, val body: String) {
        val isSuccessful: Boolean get() = code in 200..299

        /** Превращает ответ с ошибкой в исключение с понятным типом и текстом сервера. */
        fun toException(): AppException {
            val json = parseJson(body)
            val errorCode = json?.optString("error_code").orEmpty()
                .ifEmpty { json?.optString("error").orEmpty() }
            val message = listOf("msg", "message", "error_description", "error")
                .firstNotNullOfOrNull { key -> json?.optString(key)?.takeIf { it.isNotBlank() } }

            val error = when {
                errorCode == "invalid_credentials" || errorCode == "invalid_grant" ->
                    AppError.INVALID_CREDENTIALS
                errorCode == "email_not_confirmed" -> AppError.EMAIL_NOT_CONFIRMED
                errorCode == "user_already_exists" || errorCode == "email_exists" ->
                    AppError.EMAIL_TAKEN
                code == 401 && message?.contains("expired", ignoreCase = true) == true ->
                    AppError.SESSION_EXPIRED
                else -> AppError.SERVER
            }
            return AppException(error, message ?: "HTTP $code")
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * @param path путь вместе с query, например "/rest/v1/cars?select=*"
     * @param token JWT пользователя; если null, используется anon-ключ
     */
    fun execute(
        method: String,
        path: String,
        token: String? = null,
        body: RequestBody? = null,
        headers: Map<String, String> = emptyMap()
    ): Response {
        val apiKey = SupabaseConfig.ANON_KEY
        if (apiKey.isBlank() || !apiKey.all { it.code < 128 }) {
            // Ключ ещё не вставлен: говорим об этом прямо, а не падаем с непонятной ошибкой
            throw AppException(
                AppError.SERVER,
                "Не указан ключ Supabase. Вставьте его в файл SupabaseConfig.kt."
            )
        }

        val builder = Request.Builder()
            .url(SupabaseConfig.URL + path)
            .header("apikey", apiKey)

        // Старый anon-ключ это JWT, и его можно слать как Authorization. Новый publishable-ключ (sb_publishable_...)
        // JWT не является: для гостя шлём только заголовок apikey.
        val bearer = token ?: apiKey.takeIf { it.startsWith("eyJ") }
        if (bearer != null) builder.header("Authorization", "Bearer $bearer")

        headers.forEach { (name, value) -> builder.header(name, value) }
        builder.method(method, body)

        try {
            client.newCall(builder.build()).execute().use { response ->
                return Response(response.code, response.body?.string().orEmpty())
            }
        } catch (e: IOException) {
            // Нет сети, таймаут, недоступен хост
            throw AppException(AppError.NO_CONNECTION, null, e)
        }
    }

    companion object {
        fun jsonBody(json: String): RequestBody =
            json.toRequestBody("application/json; charset=utf-8".toMediaType())

        fun parseJson(text: String): JSONObject? = try {
            JSONObject(text)
        } catch (e: JSONException) {
            null
        }
    }
}
