package com.example.drivenext.domain

interface CarRepository {
    /** Все доступные автомобили. */
    suspend fun getCars(): Result<List<Car>>

    /** Поиск по марке или модели. Пустой запрос вернёт все автомобили. */
    suspend fun searchCars(query: String): Result<List<Car>>
}
