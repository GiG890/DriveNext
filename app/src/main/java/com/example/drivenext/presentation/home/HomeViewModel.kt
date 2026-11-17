package com.example.drivenext.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drivenext.domain.Car
import com.example.drivenext.domain.CarRepository
import com.example.drivenext.presentation.common.Event
import com.example.drivenext.presentation.common.LoadState
import com.example.drivenext.presentation.common.UiError
import com.example.drivenext.presentation.common.toUiError
import kotlinx.coroutines.launch

class HomeViewModel(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _state = MutableLiveData<LoadState<List<Car>>>()
    val state: LiveData<LoadState<List<Car>>> = _state

    // Одноразовое событие для диалога / экрана No Connection
    private val _errors = MutableLiveData<Event<UiError>>()
    val errors: LiveData<Event<UiError>> = _errors

    init {
        loadCars()
    }

    /** Загружает список автомобилей. Вызывается при открытии экрана и по кнопке «Повторить». */
    fun loadCars() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            carRepository.getCars().fold(
                onSuccess = { _state.value = LoadState.Success(it) },
                onFailure = {
                    val error = it.toUiError()
                    _state.value = LoadState.Error(error)
                    _errors.value = Event(error)
                }
            )
        }
    }
}
