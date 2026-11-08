package com.example.lab12_maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val ArequipaLocation = LatLng(-16.4040102, -71.559611) // Arequipa, Perú
    val MistiLocation = LatLng(-16.2944, -71.4090) // Volcán Misti
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ArequipaLocation, 11f)
    }

    // Mover la cámara programáticamente: anima hacia Yura con zoom 12 en 3 segundos
    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.2520984, -71.6836503), 12f), // Mover a Yura
            durationMs = 3000
        )
    }

    // Ejercicio 1: tipo de mapa seleccionado (Normal, Híbrido, Satélite o Terreno)
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    // Ejercicio 2: ubicación actual del usuario
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Puntos adicionales: cliente de Places y lugar buscado
    val placesClient = remember { Places.createClient(context) }
    var searchedPlace by remember { mutableStateOf<SelectedPlace?>(null) }

    // Pide la ubicación actual al FusedLocationProviderClient y mueve la cámara hacia ella
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        // Mueve la cámara y coloca el marcador en la ubicación obtenida
        fun showLocation(latitude: Double, longitude: Double) {
            val position = LatLng(latitude, longitude)
            userLocation = position
            scope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(position, 16f),
                    durationMs = 2000
                )
            }
        }

        Toast.makeText(context, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    showLocation(location.latitude, location.longitude)
                } else {
                    // Si aún no hay una lectura nueva del GPS, se usa la última ubicación conocida
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            showLocation(last.latitude, last.longitude)
                        } else {
                            Toast.makeText(context, "No se pudo obtener la ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    // Lanzador que muestra el diálogo de permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Añadir GoogleMap al layout
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = hasLocationPermission // Punto azul de "Mi ubicación"
            )
        ) {
            // Puntos adicionales: marcador del lugar buscado con Places API
            searchedPlace?.let { place ->
                val placeMarkerState = remember(place) { MarkerState(position = place.latLng) }
                Marker(
                    state = placeMarkerState,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                    title = place.name,
                    snippet = place.address
                )
                LaunchedEffect(place) { placeMarkerState.showInfoWindow() }
            }

            // Ejercicio 2: marcador en la ubicación actual del usuario
            userLocation?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    title = "Mi ubicación",
                    snippet = "%.5f, %.5f".format(position.latitude, position.longitude)
                )
            }

            // Añadir marcador en Arequipa, Perú
            Marker(
                state = rememberMarkerState(position = ArequipaLocation),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE), // Icono azul
                title = "Arequipa, Perú"
            )

            // Marcador con imagen personalizada (res/drawable/ic_mountain.xml)
            val mountainIcon = remember { bitmapDescriptorFromVector(context, R.drawable.ic_mountain) }
            Marker(
                state = rememberMarkerState(position = MistiLocation),
                icon = mountainIcon,
                anchor = Offset(0.5f, 0.5f), // Centra el icono circular en la coordenada
                title = "Volcán Misti",
                snippet = "5822 m s. n. m."
            )

            // Varios marcadores: se itera una lista de ubicaciones
            val locations = listOf(
                LatLng(-16.433415, -71.5442652), // JLByR
                LatLng(-16.4205151, -71.4945209), // Paucarpata
                LatLng(-16.3524187, -71.5675994) // Zamacola
            )

            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    title = "Ubicación",
                    snippet = "Punto de interés"
                )
            }

            // ---------- 5.1 Dibujar polígonos ----------
            val mallAventuraPolygon = listOf(
                LatLng(-16.432292, -71.509145),
                LatLng(-16.432757, -71.509626),
                LatLng(-16.433013, -71.509310),
                LatLng(-16.432566, -71.508853)
            )

            val parqueLambramaniPolygon = listOf(
                LatLng(-16.422704, -71.530830),
                LatLng(-16.422920, -71.531340),
                LatLng(-16.423264, -71.531110),
                LatLng(-16.423050, -71.530600)
            )

            val plazaDeArmasPolygon = listOf(
                LatLng(-16.398866, -71.536961),
                LatLng(-16.398744, -71.536529),
                LatLng(-16.399178, -71.536289),
                LatLng(-16.399299, -71.536721)
            )

            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 5f
            )

            // ---------- Polilíneas (ejemplos) ----------
            // Ejemplo 1: ruta continua que une los tres polígonos
            Polyline(
                points = listOf(
                    LatLng(-16.399050, -71.536620), // Plaza de Armas
                    LatLng(-16.422980, -71.530970), // Parque Lambramani
                    LatLng(-16.432660, -71.509240)  // Mall Aventura
                ),
                color = Color(0xFFE91E63),
                width = 12f,
                jointType = JointType.ROUND,
                startCap = RoundCap(),
                endCap = RoundCap()
            )

            // Ejemplo 2: línea discontinua de la Plaza de Armas al Mirador de Yanahuara
            Polyline(
                points = listOf(
                    LatLng(-16.399050, -71.536620), // Plaza de Armas
                    LatLng(-16.387000, -71.541900)  // Mirador de Yanahuara
                ),
                color = Color(0xFF00897B),
                width = 10f,
                pattern = listOf(Dash(30f), Gap(15f))
            )

            // Ejemplo 3: línea punteada y geodésica de Arequipa a Yura
            Polyline(
                points = listOf(
                    ArequipaLocation,
                    LatLng(-16.2520984, -71.6836503) // Yura
                ),
                color = Color(0xFFFF9800),
                width = 10f,
                geodesic = true,
                pattern = listOf(Dot(), Gap(12f))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Ejercicio 1: selector del tipo de mapa sobre el mapa
            MapTypeSelector(
                selected = mapType,
                onSelected = { mapType = it }
            )

            // Puntos adicionales: buscador de lugares (Places API)
            PlaceSearchBar(
                placesClient = placesClient,
                onPlaceSelected = { place ->
                    searchedPlace = place
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(place.latLng, 16f),
                            durationMs = 2000
                        )
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Ejercicio 2: botón para obtener la ubicación actual
        ExtendedFloatingActionButton(
            onClick = {
                if (hasLocationPermission) {
                    getCurrentLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Text("📍 Mi ubicación")
        }
    }
}

/**
 * Verifica si la app ya tiene permiso de ubicación (precisa o aproximada).
 */
fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/**
 * Fila de chips para cambiar el tipo de mapa.
 */
@Composable
fun MapTypeSelector(
    selected: MapType,
    onSelected: (MapType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        MapType.NORMAL to "Normal",
        MapType.HYBRID to "Híbrido",
        MapType.SATELLITE to "Satélite",
        MapType.TERRAIN to "Terreno"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        options.forEach { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFF1A73E8),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * Convierte un drawable vectorial en un BitmapDescriptor para usarlo como icono de marcador.
 * (BitmapDescriptorFactory.fromResource no soporta archivos vectoriales XML).
 */
fun bitmapDescriptorFromVector(context: Context, @DrawableRes resId: Int): BitmapDescriptor {
    val drawable = requireNotNull(ContextCompat.getDrawable(context, resId))
    return BitmapDescriptorFactory.fromBitmap(drawable.toBitmap())
}
