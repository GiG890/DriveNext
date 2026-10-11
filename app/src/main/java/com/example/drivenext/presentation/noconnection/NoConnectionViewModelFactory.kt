package com.example.drivenext.presentation.noconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drivenext.domain.NetworkRepository

class NoConnectionViewModelFactory(
    private val networkRepository: NetworkRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoConnectionViewModel(networkRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}