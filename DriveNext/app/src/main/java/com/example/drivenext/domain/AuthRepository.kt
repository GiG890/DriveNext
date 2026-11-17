package com.example.drivenext.domain

interface AuthRepository {
    /** Есть ли сохранённая активная сессия. */
    fun isLoggedIn(): Boolean

    /** Занята ли почта (для проверки на первом шаге регистрации). */
    fun isEmailRegistered(email: String): Boolean

    /** Вход по почте и паролю. При успехе сохраняет токен и сессию. */
    fun login(email: String, password: String): Result<Unit>

    /** Вход через Google. Сейчас заглушка, см. реализацию. */
    fun loginWithGoogle(): Result<Unit>

    /** Завершение регистрации: сохраняет профиль, токен и сессию. */
    fun register(data: RegistrationData): Result<Unit>

    fun logout()
}
