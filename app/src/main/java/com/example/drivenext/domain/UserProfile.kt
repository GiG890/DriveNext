package com.example.drivenext.domain

data class UserProfile(
    val userId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: Gender?,
    val avatarUrl: String?,
    /** Дата регистрации в формате ISO-8601, как её отдаёт сервер. */
    val createdAt: String?
) {
    /** «Имя Фамилия», как в макете. Может быть пустым, если профиль ещё не заполнен. */
    val displayName: String
        get() = "$firstName $lastName".trim()
}
