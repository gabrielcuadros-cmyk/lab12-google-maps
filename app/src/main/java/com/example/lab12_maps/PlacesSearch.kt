package com.example.lab12_maps

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay

/** Lugar elegido en el buscador (Places API). */
data class SelectedPlace(
    val name: String,
    val address: String,
    val latLng: LatLng
)

/**
 * Puntos adicionales: buscador de lugares con autocompletado usando Places SDK for Android (Places API New).
 * 1. Mientras el usuario escribe, pide sugerencias con findAutocompletePredictions().
 * 2. Al elegir una sugerencia, obtiene los detalles del lugar con fetchPlace().
 */
@Composable
fun PlaceSearchBar(
    placesClient: PlacesClient,
    onPlaceSelected: (SelectedPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    // El token agrupa las búsquedas y la selección final en una sola sesión de Places
    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    // Prioriza resultados dentro de Arequipa
    val arequipaBounds = remember {
        RectangularBounds.newInstance(
            LatLng(-16.55, -71.70), // suroeste
            LatLng(-16.30, -71.40)  // noreste
        )
    }

    // Autocompletado: busca sugerencias cada vez que cambia el texto (con una pequeña espera)
    LaunchedEffect(query) {
        if (query.length < 3) {
            predictions = emptyList()
            return@LaunchedEffect
        }
        delay(300)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(sessionToken)
            .setLocationBias(arequipaBounds)
            .setCountries("PE")
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response -> predictions = response.autocompletePredictions }
            .addOnFailureListener { e ->
                predictions = emptyList()
                Toast.makeText(context, "Error de Places API: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Obtiene nombre, dirección y coordenadas del lugar elegido
    fun selectPrediction(prediction: AutocompletePrediction) {
        val fields = listOf(Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)
        val request = FetchPlaceRequest.builder(prediction.placeId, fields)
            .setSessionToken(sessionToken)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val location = place.location ?: return@addOnSuccessListener
                onPlaceSelected(
                    SelectedPlace(
                        name = place.displayName ?: prediction.getPrimaryText(null).toString(),
                        address = place.formattedAddress ?: "",
                        latLng = location
                    )
                )
                query = ""
                predictions = emptyList()
                sessionToken = AutocompleteSessionToken.newInstance()
                focusManager.clearFocus()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "No se pudo obtener el lugar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Buscar un lugar en Arequipa...") },
            leadingIcon = { Text("🔍") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Text(
                        text = "✕",
                        modifier = Modifier
                            .clickable { query = "" }
                            .padding(12.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        if (predictions.isNotEmpty()) {
            // Lista de sugerencias del autocompletado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                predictions.take(5).forEachIndexed { index, prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectPrediction(prediction) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "📍 " + prediction.getPrimaryText(null).toString(),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = prediction.getSecondaryText(null).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (index < predictions.take(5).lastIndex) HorizontalDivider()
                }
            }
        } else if (query.isEmpty()) {
            // Búsquedas rápidas de ejemplo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Mall Aventura", "UNSA", "Mirador de Yanahuara", "Aeropuerto Arequipa").forEach { suggestion ->
                    AssistChip(
                        onClick = { query = suggestion },
                        label = { Text(suggestion) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
                    )
                }
            }
        }
    }
}
