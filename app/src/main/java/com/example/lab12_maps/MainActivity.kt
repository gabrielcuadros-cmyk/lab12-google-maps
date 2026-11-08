package com.example.lab12_maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.libraries.places.api.Places
import com.example.lab12_maps.ui.theme.Lab12_MapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Puntos adicionales: inicializa Places SDK (Places API New) con la misma API Key del Secrets Gradle Plugin
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        setContent {
            Lab12_MapsTheme {
                MapScreen()
            }
        }
    }
}
