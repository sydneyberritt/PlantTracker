package com.example.planttracker

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class PlantManager {
    val okHttpClient: OkHttpClient
    init{
        val builder = OkHttpClient.Builder()
        val loggingInterceptor= HttpLoggingInterceptor()
        loggingInterceptor.level= HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor ( loggingInterceptor )

        okHttpClient=builder.build()
    }

    suspend fun retrievePlants(searchTerm:String, apiKey:String): List<Plant>
    {

        val request= Request.Builder()
            .url("https://perenual.com/api/v2/species-list?key=$apiKey&indoor=1&q=$searchTerm")
            .header("X-Api-Key", apiKey)
            .get()
            .build()

        val response: Response =okHttpClient.newCall(request).execute()
        val responseBody=response.body?.string()

        if (response.isSuccessful && !responseBody.isNullOrEmpty()){
            val plants=mutableListOf<Plant>()
            val json= JSONObject(responseBody)
            val plantList=json.getJSONArray("data")

            for (i in 0 until plantList.length()) {
                val currentPlant = plantList.getJSONObject(i)
                val id = currentPlant.getInt("id")
                val currentCommonName = currentPlant.getString("common_name")

                val scientificNames = currentPlant.getJSONArray("scientific_name")
                val currentScientificName = scientificNames.getString(0)

                val defaultImage = currentPlant.optJSONObject("default_image")
                val currentImage = defaultImage?.optString("thumbnail") ?: ""
                
                val newPlant = Plant(
                    plantId = id,
                    commonName = currentCommonName,
                    scientificName = currentScientificName,
                    image = currentImage,
                )
                plants.add(newPlant)
            }
            return plants
        }else{
            return listOf()
        }
    }

    suspend fun retrievePlantDetails(plantId: Int, apiKey: String): PlantDetails? {
        val request = Request.Builder()
            .url("https://perenual.com/api/v2/species/details/$plantId?key=$apiKey")
            .get()
            .build()

        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val json = JSONObject(responseBody)

            val sciArray = json.getJSONArray("scientific_name")
            val scientificName = sciArray.getString(0)

            val defaultImage = json.optJSONObject("default_image")
            val image = defaultImage?.optString("original_url") ?: ""

            val sunArray = json.getJSONArray("sunlight")
            val sunlight = sunArray.getString(0)

            val wateringCategory = json.optString("watering", "Average")
            val frequency = when (wateringCategory.lowercase()) {
                "frequent" -> 2
                "average" -> 4
                "minimum" -> 8
                else -> 0
            }

            val plantDetails = PlantDetails(
                plantId = plantId,
                commonName = json.optString("common_name", "Unknown"),
                scientificName = scientificName,
                image = image,
                description = json.optString("description", "No description."),
                family = json.optString("family", "Unknown"),
                type = json.optString("type", "Unknown"),
                cycle = json.optString("cycle", "Unknown"),
                watering = wateringCategory,
                wateringFrequency = frequency,
                sunlight = sunlight,
                careLevel = json.optString("care_level", "Unknown"),
                maintenance = json.optString("maintenance", "Unknown"),
                poisonousToPets = json.optBoolean("poisonous_to_pets", false),
                poisonousToHumans = json.optBoolean("poisonous_to_humans", false)
            )
            return plantDetails
        } else {
            return null
        }
    }
}
