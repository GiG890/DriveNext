package com.example.drivenext.domain

import java.io.Serializable

enum class Gender { MALE, FEMALE }

/**
 * Данные регистрации, которые копятся по шагам и передаются между экранами через Intent.
 * Даты хранятся строкой в формате DateRules.DISPLAY_PATTERN, фото: строками Uri.
 */
data class RegistrationData(
    val email: String = "",
    val password: String = "",
    val lastName: String = "",
    val firstName: String = "",
    val patronymic: String = "",
    val birthDate: String = "",
    val gender: Gender? = null,
    val licenseNumber: String = "",
    val licenseIssueDate: String = "",
    val avatarUri: String? = null,
    val licensePhotoUri: String? = null,
    val passportPhotoUri: String? = null
) : Serializable
