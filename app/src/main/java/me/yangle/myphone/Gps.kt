package me.yangle.myphone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
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
import kotlinx.coroutines.flow.combine
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
    var location by mutableStateOf(Location(LocationManager.GPS_PROVIDER))

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getGnssStatus(locationManager: LocationManager): Flow<GnssStatus> = callbackFlow {
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                trySend(status)
            }
        }

        locationManager.registerGnssStatusCallback(callback, null)

        awaitClose {
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getLocation(locationManager: LocationManager): Flow<Location> = callbackFlow {
        val listener = LocationListener {
            trySend(it)
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000, 0f, listener
        )

        trySend(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: location)

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }

    init {
        viewModelScope.launch {
            getGnssStatus(locationManager).combine(getLocation(locationManager)){ first, second ->
                first to second
            }.collect { (status, location) ->
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
                this@GpsViewModel.location = location
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Gps(
    locationManager: LocationManager = LocalContext.current.getSystemService(Context.LOCATION_SERVICE) as LocationManager,
    gpsPermissionState: PermissionState = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION),
    enableFilter: Boolean = false,
    onFilterOn: (() -> Unit)? = null
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
            Column {
                val viewModel: GpsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return GpsViewModel(locationManager) as T
                        }
                    }
                )

                val gnssDataMap = viewModel.gnssData.values.groupBy { it.type }
                var gnssGroup by remember { mutableStateOf(gnssDataMap) }
                var gnssData: Collection<GpsViewModel.GnssData> by remember {
                    mutableStateOf(viewModel.gnssData.values)
                }

                if (!enableFilter) {
                    gnssData = viewModel.gnssData.values
                    gnssGroup = gnssDataMap
                }

                val context = LocalContext.current
                Text("lat: ${viewModel.location.latitude} lon: ${viewModel.location.longitude}", Modifier.clickable {
                    startActivity(context, Intent(context, MapActivity::class.java), null)
                })
                Compass(gnssData)
                Table(gnssGroup.mapKeys {
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
                    gnssData = if (row[1] != "svid") {
                        listOf(gnssDataMap[constellationStringToType(row[0])]?.find {
                            it.svid.toString() == row[1]
                        }!!)
                    } else {
                        gnssDataMap[constellationStringToType(row[0])]!!
                    }
                    gnssGroup = mapOf(constellationStringToType(row[0]) to gnssData.toList())
                    onFilterOn?.invoke()
                }
            }
        }
    }
}

private fun constellationTypeToString(type: Int) = when (type) {
    GnssStatus.CONSTELLATION_GPS -> "GPS"
    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
    GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
    GnssStatus.CONSTELLATION_QZSS -> "QZSS"
    GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU"
    GnssStatus.CONSTELLATION_GALILEO -> "GALILEO"
    GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
    else -> "UNKNOWN"
}

private fun constellationStringToType(type: String) = when (type) {
    "GPS" -> GnssStatus.CONSTELLATION_GPS
    "SBAS" -> GnssStatus.CONSTELLATION_SBAS
    "GLONASS" -> GnssStatus.CONSTELLATION_GLONASS
    "QZSS" -> GnssStatus.CONSTELLATION_QZSS
    "BEIDOU" -> GnssStatus.CONSTELLATION_BEIDOU
    "GALILEO" -> GnssStatus.CONSTELLATION_GALILEO
    "IRNSS" -> GnssStatus.CONSTELLATION_IRNSS
    else -> GnssStatus.CONSTELLATION_UNKNOWN
}
