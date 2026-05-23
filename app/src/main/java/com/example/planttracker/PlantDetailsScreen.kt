package com.example.planttracker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PlantDetailsScreen(onPlantAdded: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    val apiKey = stringResource(R.string.PlantAPIKey)
    val plantIdString = remember { prefs.getString("plantId", "") ?: "" }
    val plantId = plantIdString.toIntOrNull()

    var plantDetails by remember { mutableStateOf<PlantDetails?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val plantManager = remember { PlantManager() }

    var showWateringDialog by remember { mutableStateOf(false) }

    val userId = Firebase.auth.currentUser?.uid ?: ""
    val fbRef = "users/$userId/plants"

    // Sensor Logic
    var isMeasuringLight by remember { mutableStateOf(false) }
    val lux by produceState(initialValue = 0f, isMeasuringLight) {
        if (!isMeasuringLight) {
            value = 0f
            return@produceState
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor == null) {
            value = -1f
            return@produceState
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                value = event?.values?.get(0) ?: 0f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        awaitDispose { sensorManager.unregisterListener(listener) }
    }

    LaunchedEffect(plantId) {
        if (plantId != null) {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                plantManager.retrievePlantDetails(plantId, apiKey)
            }
            plantDetails = result
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (plantDetails != null) {
            val details = plantDetails!!
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = details.image,
                        contentDescription = details.commonName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = details.commonName.capitalizeWords(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = details.scientificName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.quick_care),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoCard(
                                label = stringResource(R.string.maintenance),
                                value = details.maintenance.capitalizeWords(),
                                icon = R.drawable.grass,
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                label = stringResource(R.string.watering),
                                value = details.watering.capitalizeWords(),
                                icon = R.drawable.water_drop,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoCard(
                                label = stringResource(R.string.sunlight),
                                value = details.sunlight.capitalizeWords(),
                                icon =  if (details.sunlight.lowercase().contains("sun")) R.drawable.sunny else R.drawable.partly_cloudy_day,
                                modifier = Modifier.weight(1f)
                            )
                            InfoCard(
                                label =  stringResource(R.string.toxic),
                                value = if (details.poisonousToHumans || details.poisonousToPets) "Yes" else "No",
                                icon = R.drawable.bad,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (userId.isNotEmpty()) {
                                    showWateringDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ){
                            Text( stringResource(R.string.add_to_family_button))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Light Sensor Section
                        Text(
                            text =  stringResource(R.string.light_meter),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LightMeterCard(lux, isMeasuringLight) { isMeasuringLight = !isMeasuringLight }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text =  stringResource(R.string.about_this_plant),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = details.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        if (showWateringDialog && plantDetails != null) {
            WateringSetupDialog(
                initialFrequencyValue = plantDetails!!.wateringFrequency,
                onDismiss = { showWateringDialog = false },
                onConfirm = { frequencyInDays, lastWateredTimestamp ->
                    val plant = UserPlant(
                        plantId = plantDetails!!.plantId,
                        commonName = plantDetails!!.commonName,
                        image = plantDetails!!.image,
                        wateringFrequency = frequencyInDays,
                        lastWateredTimestamp = lastWateredTimestamp
                    )
                    addPlantToFamily(fbRef, plant)
                    showWateringDialog = false
                    Toast.makeText(context, "Added to family!", Toast.LENGTH_SHORT).show()
                    onPlantAdded()
                }
            )
        }
    }
}

@Composable
fun LightMeterCard(lux: Float, isMeasuring: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = if (isMeasuring) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.light_meter),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = when {
                        lux < 0 -> "This device doesn’t support light sensing"
                        isMeasuring -> "Measuring light… hold your phone near your plant"
                        else -> stringResource(R.string.light_meter_msg_1)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                // rough thresholds based on typical indoor lighting ranges
                if (isMeasuring && lux >= 0) {
                    var lightLevel = ""
                    if (lux < 500) {
                        lightLevel = stringResource(R.string.low_light)
                    } else if (lux < 2500) {
                        lightLevel = stringResource(R.string.medium_light)
                    } else {
                        lightLevel = stringResource(R.string.bright_light)
                    }
                    Text(lightLevel, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
            }
            Button(
                onClick = onToggle,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isMeasuring) stringResource(R.string.stop) else stringResource(R.string.check))
            }
        }
    }
}

@Composable
fun WateringSetupDialog(initialFrequencyValue: Int, onDismiss: () -> Unit, onConfirm: (Int, Long) -> Unit) {
    var frequencyLabel = ""
    if (initialFrequencyValue == 2) {
        frequencyLabel = "Frequent"
    } else if (initialFrequencyValue == 4) {
        frequencyLabel = "Average"
    } else if (initialFrequencyValue == 8) {
        frequencyLabel = "Minimum"
    } else {
        frequencyLabel = "None"
    }

    var selectedFrequency by remember { mutableStateOf(frequencyLabel) }
    var selectedLastWatered by remember { mutableStateOf("Today") }
    val lastWateredOptions = listOf("Today", "Yesterday", "3-4 days ago", "More than a week ago")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Watering Setup", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Last Watered:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                lastWateredOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLastWatered = option }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedLastWatered == option,
                            onClick = { selectedLastWatered = option }
                        )
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var days = 0
                if (selectedFrequency == "Frequent") {
                    days = 2
                } else if (selectedFrequency == "Average") {
                    days = 4
                } else if (selectedFrequency == "Minimum") {
                    days = 8
                }

                val now = System.currentTimeMillis()
                var lastWatered = now
                if (selectedLastWatered == "Yesterday") {
                    lastWatered = now - TimeUnit.DAYS.toMillis(1)
                } else if (selectedLastWatered == "3-4 days ago") {
                    lastWatered = now - TimeUnit.DAYS.toMillis(4)
                } else if (selectedLastWatered == "More than a week ago") {
                    lastWatered = now - TimeUnit.DAYS.toMillis(8)
                }

                onConfirm(days, lastWatered)
            }) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun InfoCard(label: String, value: String, icon: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun addPlantToFamily(fbRef: String, plant: UserPlant) {
    if (fbRef.isEmpty()) return
    val plantRef = Firebase.database.getReference(fbRef)
    val singlePlantRef = plantRef.child(plant.plantId.toString())
    singlePlantRef.setValue(plant)
        .addOnSuccessListener {
            Log.d("FirebaseWrite", "Plant added successfully")
        }
        .addOnFailureListener { error ->
            Log.e("FirebaseWrite", "Failed to add plant", error)
        }
}

// helper function to capitalize the first letter of each word in a string
fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
    } }
