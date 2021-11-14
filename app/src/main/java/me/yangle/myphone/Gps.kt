package me.yangle.myphone

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.Table

class GpsViewModel(private val locationManager: LocationManager) : ViewModel() {
    class GnssData(
        val svid: Int,
        val type: Int,
        val azimuth: Float,
        val elevation: Float,
        val carrierFrequency: Float?,
        val Cn0DbHz: Float
    ) : Comparable<GnssData> {
        override fun compareTo(other: GnssData): Int {
            return compareValuesBy(this, other, GnssData::type, GnssData::svid)
        }
    }

    private val _gnssData = mutableStateMapOf<Int, GnssData>()
    val gnssData = _gnssData

    @OptIn(ExperimentalCoroutinesApi::class)
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
                    _gnssData[status.getConstellationType(it) * 256 + status.getSvid(it)] =
                        GnssData(
                            status.getSvid(it),
                            status.getConstellationType(it),
                            status.getAzimuthDegrees(it),
                            status.getElevationDegrees(it),
                            if (status.hasCarrierFrequencyHz(it)) status.getCarrierFrequencyHz(it) else null,
                            status.getCn0DbHz(it)
                        )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Gps(
    locationManager: LocationManager,
    gpsPermissionState: PermissionState = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
) {
    var providerEnabled by remember {
        mutableStateOf(
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        )
    }

    val startActivityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),

        ) {
        providerEnabled = locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
    }

    PermissionRequired(
        permissionState = gpsPermissionState,
        permissionNotGrantedContent = {
            if (gpsPermissionState.shouldShowRationale) {
                Snackbar(action = {
                    Button(onClick = {
                        gpsPermissionState.launchPermissionRequest()
                    }) {
                        Text("OK")
                    }
                }) {
                    Text("Location access is required for this function, please give us the permission")
                }
            } else {
                // LaunchedEffect or call in a Button callback? it's a problem.
                LaunchedEffect(gpsPermissionState) {
                    gpsPermissionState.launchPermissionRequest()
                }
            }
        },
        permissionNotAvailableContent = {
            Text("You denied our request, if you want use this function, please consider allow the Location Permission from system settings.")
        }
    ) {
        if (!providerEnabled) {
            Snackbar(action = {
                Button(onClick = { startActivityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                    Text("OK")
                }
            }) {
                Text("Please open GPS switch")
            }
        } else {
            val viewModel: GpsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return GpsViewModel(locationManager) as T
                    }
                }
            )
            Table(viewModel.gnssData.values.sorted().map { data ->
                listOf(
                    when (data.type) {
                        GnssStatus.CONSTELLATION_GPS -> "GPS"
                        GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                        GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                        GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
                        GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
                        GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
                        else -> "unknown"
                    },
                    data.svid.toString(),
                    data.azimuth.toString(),
                    data.elevation.toString(),
                    (data.carrierFrequency ?: "N/A").toString(),
                    data.Cn0DbHz.toString(),
                )
            }, listOf("type", "svid", "azimuth", "elevation", "carrier frequency", "Cn0DbHz"))
        }
    }
}
