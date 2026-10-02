package com.hocheol.weatherapp

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRepository {

    private const val BASE_URL = "http://apis.data.go.kr/"
    private val SERVER_KEY = BuildConfig.WEATHER_SERVICE_KEY

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(WeatherService::class.java)

    fun getVillageForecast(
        latitude: Double,
        longitude: Double,
        successCallback: (List<Forecast>) -> Unit,
        failureCallback: (Throwable) -> Unit
    ) {
        val baseDateTime = BaseDateTime.getBaseDateTime()
        val converter = GeoPointConverter()
        val point = converter.convert(lat = latitude, lon = longitude)

        service.getVillageForecast(
            serviceKey = SERVER_KEY,
            baseDate = baseDateTime.baseDate,
            baseTime = baseDateTime.baseTime,
            nx = point.nx,
            ny = point.ny
        ).enqueue(object : Callback<WeatherEntity> {
            override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>) {
                val forecastMap = mutableMapOf<String, Forecast>()
                val forecastList = response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                for (forecast in forecastList) {
                    val key = "${forecast.forecastDate}/${forecast.forecastTime}"
                    forecastMap.getOrPut(key) {
                        Forecast(
                            forecastDate = forecast.forecastDate,
                            forecastTime = forecast.forecastTime
                        )
                    }.apply {
                        forecast.category?.let { category ->
                            when (category) {
                                Category.POP -> precipitation = forecast.forecastValue.toInt()
                                Category.PTY -> precipitationType = transformRainType(forecast)
                                Category.SKY -> sky = transformSky(forecast)
                                Category.TMP -> temperature = forecast.forecastValue.toDouble()
                                else -> Unit
                            }
                        }
                    }
                }

                val sortedForecastList = forecastMap.values.sortedBy { "${it.forecastDate}${it.forecastTime}" }

                if (sortedForecastList.isEmpty()) {
                    failureCallback(NullPointerException())
                } else {
                    successCallback(sortedForecastList)
                }
            }

            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                failureCallback(t)
            }
        })
    }

    private fun transformRainType(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSky(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> ""
        }
    }
}