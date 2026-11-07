package com.example.lab12_maps

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
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
