package com.example.drivenext.domain

interface AuthRepository {
    fun isLoggedIn(): Boolean
}