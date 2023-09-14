package com.example.weatherforecastapp

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import com.example.weatherforecastapp.databinding.ActivityMainBinding
import com.google.android.material.color.utilities.ViewingConditions
import org.w3c.dom.Text
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.Condition

//3999d0bf3d7cc407ed8873671278e2ac

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData(cityName = "Jaipur")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create<ApiInterface>(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, appid = "3999d0bf3d7cc407ed8873671278e2ac", units = "metric" )
        response.enqueue(object : Callback<WeatherForecastApp>{
            override fun onResponse(call: Call<WeatherForecastApp>, response: Response<WeatherForecastApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val  sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max Temp: $maxTemp °C"
                    binding.minTemp.text = "Min Temp: $minTemp °C"
                    binding.Humidity.text = "$humidity °C"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunrise.text = "${time(sunRise)}"
                    binding.sunset.text = "\"${time(sunSet)}\""
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        binding.cityName.text = "$cityName"
                    //Log.d(TAG, "onResponse: $temperature")

                    changeImagesAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherForecastApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun changeImagesAccordingToWeatherCondition(conditions: String) {
        when (conditions){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}