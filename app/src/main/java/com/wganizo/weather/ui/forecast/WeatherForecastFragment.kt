package com.wganizo.weather.ui.forecast

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wganizo.weather.R
import com.wganizo.weather.constants.Constants
import kotlinx.coroutines.launch

class WeatherForecastFragment : Fragment() {

    private lateinit var forecastListView: ListView
    private val weatherForecastRepository = WeatherForecastRepository()
    private val constants = Constants()
    private var place: String? = null
    private var description: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_weather_forecast, container, false)
        forecastListView = rootView.findViewById(R.id.forecastListView)

        fetchWeatherData()

        return rootView
    }

    private fun fetchWeatherData() {
        val latitude = arguments?.getFloat("latitude") ?: 0f
        val longitude = arguments?.getFloat("longitude") ?: 0f
        place = arguments?.getString("place")
        lifecycleScope.launch {
            val weatherResponse = weatherForecastRepository.getWeather(latitude.toDouble(), longitude.toDouble())
            if (weatherResponse != null) {
                val groupedWeatherList = weatherResponse.list.groupBy { it.dt_txt.split(" ")[0] }.map { (date, weatherItems) ->
                    val tempMax = weatherItems.maxOf { it.main.temp }
                    val tempMin = weatherItems.minOf { it.main.temp }
                    val avgTemp = String.format("%.2f", weatherItems.map { it.main.temp }.average()).toDouble()
                    description = weatherItems.first().weather.firstOrNull()?.description ?: ""
                    val icon = weatherItems.first().weather.firstOrNull()?.icon ?: ""

                    Weather(
                        date = date,
                        tempMax = tempMax,
                        tempMin = tempMin,
                        temp = avgTemp,
                        description = "$description in $place",
                        icon = icon
                    )
                }
                val adapter = WeatherForecastAdapter(requireContext(), groupedWeatherList)
                forecastListView.adapter = adapter

                // Set item click listener
                forecastListView.setOnItemClickListener { _, _, position, _ ->
                    val selectedWeather = groupedWeatherList[position]
                    showWeatherDetailsDialog(selectedWeather)
                }
            }
        }
    }

    private fun showWeatherDetailsDialog(weather: Weather) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_weather_details, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val weatherImageView = dialogView.findViewById<ImageView>(R.id.weatherImageView)
        val closeDialogImageView = dialogView.findViewById<ImageView>(R.id.closeDialogImageView)
        val placeTextView = dialogView.findViewById<TextView>(R.id.placeTextView)
        val dateTextView = dialogView.findViewById<TextView>(R.id.dateTextView)
        val tempTextView = dialogView.findViewById<TextView>(R.id.tempTextView)
        val averageTempTextView = dialogView.findViewById<TextView>(R.id.averageTempTextView)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.descriptionTextView)

        // Use Glide to load weather icon
        Glide.with(requireContext())
            .load("${constants.baseImageUrl}${weather.icon}${constants.fileFormat}")
            .into(weatherImageView)

        // Set text for TextViews
        dateTextView.text = weather.date
        placeTextView.text = place
        averageTempTextView.text = "${String.format("%.2f", weather.temp)}°"
        tempTextView.text = "Maximum Temperature: ${weather.tempMax}°\nMinimum Temperature : ${weather.tempMin}°"
        descriptionTextView.text = description

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        closeDialogImageView.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}