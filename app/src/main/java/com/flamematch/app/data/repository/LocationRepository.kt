package com.flamematch.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
)

class LocationRepository(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationData> = withContext(Dispatchers.IO) {
        try {
            val cancellationToken = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
            
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                
                val city = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea ?: "Unknown"
                val country = addresses?.firstOrNull()?.countryName ?: "Unknown"
                
                Result.success(LocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    city = city,
                    country = country
                ))
            } else {
                Result.failure(Exception("Could not get location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000 // Convert to kilometers
    }
}
