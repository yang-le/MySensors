package me.yangle.myphone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.runtime.*
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
import me.yangle.myphone.ui.Compass
import me.yangle.myphone.ui.SimpleAlertDialog
import me.yangle.myphone.ui.Table

class GpsViewModel(private val locationManager: LocationManager) : ViewModel() {
    data class GnssData(
        val svid: Int,
        val type: Int,
        val azimuth: Float,
        val elevation: Float,
        val carrierFrequency: Float?,
        val Cn0DbHz: Float
    )

    val gnssData = mutableStateMapOf<Pair<Int, Int>, GnssData>()

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
                    gnssData[status.getConstellationType(it) to status.getSvid(it)] =
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
            var showCompass by remember { mutableStateOf(false) }
            val pos = remember { mutableStateOf(listOf(0f to 0f)) }
            var type by remember { mutableStateOf(0) }

            val viewModel: GpsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return GpsViewModel(locationManager) as T
                    }
                }
            )
            val gnssDataMap = viewModel.gnssData.values.groupBy { it.type }

            if (!showCompass) {
                Table(gnssDataMap.mapKeys {
                    listOf(
                        constellationTypeToString(it.key),
                        "svid",
                        "azimuth",
                        "elevation",
                        "carrier frequency",
                        "Cn0DbHz"
                    )
                }.mapValues {
                    it.value.map { data ->
                        listOf(
                            constellationTypeToString(data.type),
                            data.svid.toString(),
                            data.azimuth.toString(),
                            data.elevation.toString(),
                            (data.carrierFrequency ?: "N/A").toString(),
                            data.Cn0DbHz.toString(),
                        )
                    }
                }) { row ->
                    showCompass = true
                    type = constellationStringToType(row[0])
                    if (row[1] != "svid") {
                        pos.value = listOf(row[3].toFloat() to row[2].toFloat())
                    } else {
                        val data = gnssDataMap[type]
                        if (data != null) {
                            pos.value = data.map {
                                it.elevation to it.azimuth
                            }
                        }
                    }
                }
            } else {
                Compass(constellationTypeToIcon(type), pos.value)
                BackHandler {
                    showCompass = false
                }
            }
        }
    }
}

fun constellationTypeToString(type: Int) = when (type) {
    GnssStatus.CONSTELLATION_GPS -> "GPS"
    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
    GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
    GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
    GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
    else -> "UNKNOWN"
}

fun constellationStringToType(type: String) = when (type) {
    "GPS" -> GnssStatus.CONSTELLATION_GPS
    "SBAS" -> GnssStatus.CONSTELLATION_SBAS
    "GLONASS" -> GnssStatus.CONSTELLATION_GLONASS
    "QZSS" -> GnssStatus.CONSTELLATION_QZSS
    "BEIDOU" -> GnssStatus.CONSTELLATION_BEIDOU
    "GALILEO" -> GnssStatus.CONSTELLATION_GALILEO
    "IRNSS" -> GnssStatus.CONSTELLATION_IRNSS
    else -> GnssStatus.CONSTELLATION_UNKNOWN
}

fun constellationTypeToIcon(type: Int) = when (type) {
    GnssStatus.CONSTELLATION_GPS -> "\uD83C\uDDFA\uD83C\uDDF8"
    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
    GnssStatus.CONSTELLATION_GLONASS -> "\uD83C\uDDF7\uD83C\uDDFA"
    GnssStatus.CONSTELLATION_QZSS -> "\uD83C\uDDEF\uD83C\uDDF5"
    GnssStatus.CONSTELLATION_BEIDOU -> "\uD83C\uDDE8\uD83C\uDDF3"
    GnssStatus.CONSTELLATION_GALILEO -> "\uD83C\uDDEA\uD83C\uDDFA"
    GnssStatus.CONSTELLATION_IRNSS -> "\uD83C\uDDEE\uD83C\uDDF3"
    else -> "❔"
}
