package com.example.planttracker

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val auth = Firebase.auth
    val userId = auth.currentUser?.uid ?: ""

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.your_plants),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            DisplayPlantList(
                fbRef = "users/$userId/plants",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DisplayPlantList(fbRef: String, modifier: Modifier = Modifier) {
    val plantList = remember { mutableStateListOf<UserPlant>() }
    val context = LocalContext.current
    val connectionFailedMsg = stringResource(R.string.connection_failed)

    LaunchedEffect(fbRef) {
        val plantRef = Firebase.database.getReference(fbRef)
        plantRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                plantList.clear()
                snapshot.children
                    .mapNotNull { it.getValue(UserPlant::class.java) }
                    .forEach { plantList.add(it) }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, connectionFailedMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    if (plantList.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.plant_family_empty), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(plantList) { currentPlant ->
                ProfessionalPlantCard(
                    plant = currentPlant,
                    onWatered = { timestamp ->
                        //get the timestamp of when the plant was lasted watered
                        val plantRef = Firebase.database.getReference(fbRef).child(currentPlant.plantId.toString())
                        plantRef.child("lastWateredTimestamp").setValue(timestamp)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfessionalPlantCard(plant: UserPlant, onWatered: (Long) -> Unit) {
    val context = LocalContext.current
    var showWateringDialog by remember { mutableStateOf(false) }
    val statusInfo = calculateWateringStatus(plant.lastWateredTimestamp, plant.wateringFrequency)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val isWateredToday = statusInfo.first == context.getString(R.string.watered_today)
                if (isWateredToday) {
                    Toast.makeText(context, context.getString(R.string.water_plant_dialog_text, plant.commonName), Toast.LENGTH_SHORT).show()
                } else {
                    showWateringDialog = true
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = plant.image,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = plant.commonName.capitalizeWords(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    color = statusInfo.second.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusInfo.first,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusInfo.second,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    //if card is clicked on allow the user to water the plant
    if (showWateringDialog) {
        AlertDialog(
            onDismissRequest = { showWateringDialog = false },
            title = { Text(stringResource(R.string.water_plant_dialog_title)) },
            text = { Text(stringResource(R.string.water_plant_dialog_text, plant.commonName)) },
            confirmButton = {
                Button(onClick = {
                    onWatered(System.currentTimeMillis())
                    showWateringDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showWateringDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun calculateWateringStatus(lastWatered: Long, frequencyDays: Int): Pair<String, Color> {
    if (frequencyDays <= 0) return stringResource(R.string.no_schedule_set) to Color.Gray

    val now = System.currentTimeMillis()
    val nextWateringTime = lastWatered + TimeUnit.DAYS.toMillis(frequencyDays.toLong())
    
    val diffMillis = nextWateringTime - now
    val oneDayMillis = TimeUnit.DAYS.toMillis(1)
    
    val leafGreen = Color(0xFF2D6A4F)
    val warningOrange = Color(0xFFE67E22)
    val errorRed = Color(0xFFB00020)

    var statusText: String
    var statusColor: Color

    if (now - lastWatered < oneDayMillis) {
        statusText = stringResource(R.string.watered_today)
        statusColor = leafGreen
    } else if (diffMillis > 0 && diffMillis < oneDayMillis) {
        statusText = stringResource(R.string.water_today)
        statusColor = warningOrange
    } else if (diffMillis >= oneDayMillis) {
        val days = (diffMillis / oneDayMillis).toInt()
        if (days == 1) {
            statusText = stringResource(R.string.water_tomorrow)
        } else {
            statusText = stringResource(R.string.water_in_days, days)
        }
        statusColor = leafGreen.copy(alpha = 0.7f)
    } else {
        statusText = stringResource(R.string.watering_overdue)
        statusColor = errorRed
    }

    return statusText to statusColor
}
