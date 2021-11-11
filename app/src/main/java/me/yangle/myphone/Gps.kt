package me.yangle.myphone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Button
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.Table

class GpsViewModel(private val locationManager: LocationManager) : ViewModel() {
    data class GnssData(
        val type: Int,
        val azimuth: Float,
        val elevation: Float,
        val carrierFrequency: Float?,
        val Cn0DbHz: Float
    )

    private val _gnssData = mutableStateMapOf<Int, GnssData>()
    val gnssData = _gnssData

    @SuppressLint("MissingPermission")
    private fun getGnssStatus(locationManager: LocationManager): Flow<GnssStatus> = callbackFlow {
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                trySend(status)
            }
        }

        val listener = LocationListener { }

        locationManager.registerGnssStatusCallback(callback, null)
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000, 0f, listener
        )

        awaitClose {
            locationManager.removeUpdates(listener)
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }

    init {
        viewModelScope.launch {
            getGnssStatus(locationManager).collect { status ->
                (0 until status.satelliteCount).forEach {
                    _gnssData[status.getSvid(it)] = GnssData(
                        status.getConstellationType(it),
                        status.getAzimuthDegrees(it),
                        status.getElevationDegrees(it),
                        status.getCarrierFrequencyHz(it),
                        status.getCn0DbHz(it)
                    )
                }
            }
        }
    }
}

@Composable
fun Gps(
    locationManager: LocationManager,
    gpsPermissionState: PermissionState
) {
    var providerEnabled by remember {
        mutableStateOf(
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        gpsPermissionState::onRequestPermissionResult
    )

    val startActivityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),

        ) {
        providerEnabled = locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
    }

    if (gpsPermissionState.permissionDenied) {
        Text("You denied our request, if you want use this function, please consider allow the Location Permission from system settings.")
    } else if (!gpsPermissionState.permissionGranted) {
        if (gpsPermissionState.showRationale) {
            Snackbar(action = {
                Button(onClick = {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("OK")
                }
            }) {
                Text("Location access is required for this function, please give us the permission")
            }
        } else {
            // LaunchedEffect or call in a Button callback? it's a problem.
            LaunchedEffect(requestPermissionLauncher) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    } else if (!providerEnabled) {
        Snackbar(action = {
            Button(onClick = { startActivityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                Text("OK")
            }
        }) {
            Text("Please open GPS switch")
        }
    } else {
        val viewModel = remember { GpsViewModel(locationManager) }
        Table(viewModel.gnssData.map { (index, data) ->
            listOf(
                when (data.type) {
                    GnssStatus.CONSTELLATION_GPS -> "GPS"
                    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                    GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
                    GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
                    GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
                    else -> "unknow"
                },
                index.toString(),
                data.azimuth.toString(),
                data.elevation.toString(),
                data.carrierFrequency.toString(),
                data.Cn0DbHz.toString(),
            )
        }, listOf("type", "svid", "azimuth", "elevation", "carrier frequency", "Cn0DbHz"))
    }
}
