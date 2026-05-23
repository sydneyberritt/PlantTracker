package com.example.planttracker

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingPlantScreen(onPlantSelection: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE) }
    val trendingList = remember { mutableStateListOf<Pair<Plant, Int>>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val databaseRef = Firebase.database.getReference("users")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plantCounts = mutableMapOf<Int, Int>()
                val plantData = mutableMapOf<Int, Plant>()

                for (userSnapshot in snapshot.children) {
                    val plantsSnapshot = userSnapshot.child("plants")
                    for (plantSnapshot in plantsSnapshot.children) {
                        try {
                            val id = plantSnapshot.child("plantId").getValue(Int::class.java) ?: 0
                            if (id == 0) continue

                            val commonName = plantSnapshot.child("commonName").getValue(String::class.java) ?: "Unknown Plant"
                            val scientificName = plantSnapshot.child("scientificName").getValue(String::class.java) ?: ""
                            val image = plantSnapshot.child("image").getValue(String::class.java) ?: ""

                            val plant = Plant(id, commonName, scientificName, image)

                            plantCounts[id] = plantCounts.getOrDefault(id, 0) + 1
                            plantData[id] = plant
                        } catch (e: Exception) { }
                    }
                }

                val sortedTrending = plantCounts.toList()
                    .sortedByDescending { it.second }
                    .take(10)
                    .mapNotNull { (id, count) ->
                        plantData[id]?.let { it to count }
                    }

                trendingList.clear()
                trendingList.addAll(sortedTrending)
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                Toast.makeText(context, "Failed to load trending plants", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.trending_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.most_loved_plants),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (trendingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_plants_added_yet), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(trendingList) { (plant) ->
                        PlantSearchCard(onPlantSelection, plant)
                    }
                }
            }
        }
    }
}