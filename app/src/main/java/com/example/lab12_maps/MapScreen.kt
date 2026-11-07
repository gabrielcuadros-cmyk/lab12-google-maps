package com.example.lab12_maps

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Añadir GoogleMap al layout
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
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
