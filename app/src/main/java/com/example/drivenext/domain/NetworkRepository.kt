package com.example.drivenext.domain

interface NetworkRepository {
    fun isNetworkAvailable(): Boolean
}