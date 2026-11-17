package com.example.drivenext.domain

interface AuthRepository {
    /** Есть ли сохранённая активная сессия. */
    fun isLoggedIn(): Boolean

    /** Вход по почте и паролю. При успехе сохраняет токены и сессию. */
    suspend fun login(email: String, password: String): Result<Unit>

    /** Вход через Google. Пока не подключён: см. реализацию. */
    suspend fun loginWithGoogle(): Result<Unit>

    /** Регистрация: создаёт аккаунт, загружает фото, сохраняет профиль. */
    suspend fun register(data: RegistrationData): Result<Unit>

    /** Профиль текущего пользователя. */
    suspend fun getProfile(): Result<UserProfile>

    /** Загружает новый аватар и сохраняет его в профиле. imageUri: строка Uri выбранной картинки. */
    suspend fun updateAvatar(imageUri: String): Result<Unit>

    fun logout()
}
