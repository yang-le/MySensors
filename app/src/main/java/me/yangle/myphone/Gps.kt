package me.yangle.myphone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import me.yangle.myphone.ui.SimpleAlertDialog
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

    private val _gnssData = mutableStateMapOf<Int, SnapshotStateMap<Int, GnssData>>()
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
                    val type = status.getConstellationType(it)
                    if (!_gnssData.containsKey(type)) {
                        _gnssData[type] = mutableStateMapOf()
                    }
                    _gnssData[type]?.set(
                        status.getSvid(it),
                        GnssData(
                            status.getSvid(it),
                            status.getConstellationType(it),
                            status.getAzimuthDegrees(it),
                            status.getElevationDegrees(it),
                            if (status.hasCarrierFrequencyHz(it)) status.getCarrierFrequencyHz(it) else null,
                            status.getCn0DbHz(it)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Gps(
    locationManager: LocationManager = LocalContext.current.getSystemService(Context.LOCATION_SERVICE) as LocationManager,
    gpsPermissionState: PermissionState = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
) {
    var providerEnabled by remember {
        mutableStateOf(
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        )
    }

    val startActivityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            providerEnabled = locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        }

    PermissionRequired(
        permissionState = gpsPermissionState,
        permissionNotGrantedContent = {
            if (gpsPermissionState.shouldShowRationale) {
                SimpleAlertDialog(
                    title = "Permission required",
                    text = "Location access is required for this function, please give us the permission.",
                    confirmText = "OK",
                    dismissText = "NO",
                    dismissContent = { Icon(Icons.Rounded.GpsOff, null, Modifier.fillMaxSize()) }
                ) {
                    gpsPermissionState.launchPermissionRequest()
                }
            } else {
                // LaunchedEffect or call in a Button callback? it's a problem.
                LaunchedEffect(gpsPermissionState) {
                    gpsPermissionState.launchPermissionRequest()
                }
            }
        },
        permissionNotAvailableContent = {
            val packageName = LocalContext.current.packageName
            SimpleAlertDialog(
                title = "Permission denied",
                text = "You denied our request, if you want use this function, please consider allow the Location Permission from system settings.",
                confirmText = "OK",
                dismissText = "NO",
                dismissContent = { Icon(Icons.Rounded.GpsOff, null, Modifier.fillMaxSize()) }
            ) {
                startActivityLauncher.launch(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    ) {
        if (!providerEnabled) {
            SimpleAlertDialog(
                title = "Please open GPS switch.",
                confirmText = "OK",
                dismissText = "NO",
                dismissContent = { Icon(Icons.Rounded.GpsOff, null, Modifier.fillMaxSize()) }
            ) {
                startActivityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            val viewModel: GpsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return GpsViewModel(locationManager) as T
                    }
                }
            )
            Table(viewModel.gnssData.entries.associate { (type, value) ->
                listOf(
                    when (type) {
                        GnssStatus.CONSTELLATION_GPS -> "GPS"
                        GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                        GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                        GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                        GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
                        GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
                        GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
                        else -> "unknown"
                    },
                    "svid",
                    "azimuth",
                    "elevation",
                    "carrier frequency",
                    "Cn0DbHz"
                ) to value.values.map {
                    listOf(
                        "",
                        it.svid.toString(),
                        it.azimuth.toString(),
                        it.elevation.toString(),
                        (it.carrierFrequency ?: "N/A").toString(),
                        it.Cn0DbHz.toString(),
                    )
                }
            })
        }
    }
}
