package com.example.drivenext.data.repository

import com.example.drivenext.data.remote.SessionManager
import com.example.drivenext.data.remote.SupabaseHttp
import com.example.drivenext.data.remote.apiCall
import com.example.drivenext.domain.AppError
import com.example.drivenext.domain.AppException
import com.example.drivenext.domain.Car
import com.example.drivenext.domain.CarRepository
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/** Автомобили из таблицы cars в Supabase (PostgREST). */
class CarRepositoryImpl(
    private val session: SessionManager,
    private val http: SupabaseHttp
) : CarRepository {

    override suspend fun getCars(): Result<List<Car>> = apiCall { load(ALL_CARS_PATH) }

    override suspend fun searchCars(query: String): Result<List<Car>> = apiCall {
        // Убираем символы, которые имеют особый смысл в фильтрах PostgREST
        val clean = query.trim().replace(Regex("[,()*%\\\\]"), " ").trim()
        if (clean.isEmpty()) {
            load(ALL_CARS_PATH)
        } else {
            val filter = "(brand.ilike.*$clean*,model.ilike.*$clean*)"
            val encoded = URLEncoder.encode(filter, "UTF-8").replace("+", "%20")
            load("/rest/v1/cars?select=*&or=$encoded&order=id.asc")
        }
    }

    private fun load(path: String): List<Car> {
        val response = http.execute("GET", path, bearerOrNull())
        if (!response.isSuccessful) throw response.toException()

        val rows = JSONArray(response.body)
        return List(rows.length()) { index -> rows.getJSONObject(index).toCar() }
    }

    /** Машины может читать и гость, поэтому при проблемах с сессией просто идём с anon-ключом. */
    private fun bearerOrNull(): String? {
        if (!session.isLoggedIn()) return null
        return try {
            session.validAccessToken()
        } catch (e: AppException) {
            if (e.error == AppError.NO_CONNECTION) throw e else null
        }
    }

    private fun JSONObject.toCar() = Car(
        id = getLong("id"),
        brand = optString("brand"),
        model = optString("model"),
        pricePerDay = optInt("price_per_day"),
        transmission = optString("transmission"),
        fuel = optString("fuel"),
        imageUrl = if (isNull("image_url")) null else optString("image_url").takeIf { it.isNotBlank() }
    )

    private companion object {
        const val ALL_CARS_PATH = "/rest/v1/cars?select=*&order=id.asc"
    }
}
