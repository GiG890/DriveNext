package com.example.drivenext.presentation.noconnection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drivenext.domain.NetworkRepository

class NoConnectionViewModel(
    private val networkRepository: NetworkRepository
) : ViewModel() {


    private val _networkRestored = MutableLiveData<Boolean>()
    val networkRestored: LiveData<Boolean> = _networkRestored


    private val _showErrorMessage = MutableLiveData<Boolean>()
    val showErrorMessage: LiveData<Boolean> = _showErrorMessage

    fun onRetryClicked() {
        if (networkRepository.isNetworkAvailable()) {
            _networkRestored.value = true
        } else {
            _showErrorMessage.value = true
        }
    }
}