package com.example.drivenext.domain

enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_TAKEN,
    GOOGLE_FAILED
}

class AuthException(val error: AuthError) : Exception(error.name)
