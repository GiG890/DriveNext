package com.example.drivenext.data.repository

import android.content.Context
import com.example.drivenext.domain.AuthRepository

class AuthRepositoryImpl(context: Context) : AuthRepository {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override fun isLoggedIn(): Boolean {

        return prefs.getBoolean("is_logged_in", false)
    }
}