package com.example.planttracker
import java.io.Serializable

data class UserPlant(
    val plantId: Int = 0,
    val commonName: String = "",
    val image: String = "",
    val wateringFrequency: Int = 0,
    val lastWateredTimestamp: Long = 0L
): Serializable