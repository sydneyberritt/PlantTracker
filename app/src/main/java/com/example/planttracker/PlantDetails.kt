package com.example.planttracker

data class PlantDetails(
    val plantId: Int,
    val commonName: String,
    val scientificName: String,
    val image: String,
    val description: String,

    val family: String,
    val type: String,
    val cycle: String,

    val watering: String,
    val wateringFrequency: Int,
    val sunlight: String,

    val careLevel: String,
    val maintenance: String,

    val poisonousToPets: Boolean,
    val poisonousToHumans: Boolean
)