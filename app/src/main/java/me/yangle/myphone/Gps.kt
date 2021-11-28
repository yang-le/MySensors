package me.yangle.myphone

import android.content.Context
import android.content.Intent
import android.location.*
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import me.yangle.myphone.ui.Compass
import me.yangle.myphone.ui.SimpleAlertDialog
import me.yangle.myphone.ui.Table


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
            val context = LocalContext.current
            val viewModel: GpsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return GpsViewModel(locationManager, Geocoder(context)) as T
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

            Column {
                AddressCard(viewModel.location, viewModel.addressList)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AddressCard(
    location: Location,
    addressList: List<Address>
) {
    val context = LocalContext.current
    Column {
        Row(Modifier.clickable {
            ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.latitude},${location.longitude}")),
                null
            )
        }) {
            Icon(Icons.Rounded.Place, "coordinate")
            Text("${location.latitude}, ${location.longitude}")
        }
        addressList.forEach {
            Row(Modifier.clickable {
                if (it.hasLatitude() && it.hasLongitude()) {
                    ContextCompat.startActivity(
                        context,
                        Intent(Intent.ACTION_VIEW, Uri.parse("geo:${it.latitude},${it.longitude}")),
                        null
                    )
                }
            }) {
                Icon(Icons.Rounded.Home, "location")
                Text(addressToString(it))
            }
            it.postalCode?.let { postalCode ->
                if (postalCode.isNotBlank()) {
                    Row{
                        Icon(Icons.Rounded.MarkunreadMailbox, "postal code")
                        Text(postalCode)
                    }
                }
            }
            it.phone?.let { phone ->
                if (phone.isNotBlank()) {
                    Row(Modifier.clickable {
                        ContextCompat.startActivity(
                            context,
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                            null
                        )
                    }){
                        Icon(Icons.Rounded.Phone, "phone")
                        Text(phone)
                    }
                }
            }
            it.url?.let { url ->
                if (url.isNotBlank()) {
                    Row(Modifier.clickable {
                        ContextCompat.startActivity(
                            context,
                            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                            null
                        )
                    }){
                        Icon(Icons.Rounded.Link, "url")
                        Text(url)
                    }
                }
            }
            if (it.maxAddressLineIndex > 0) {
                var showNearMe by remember { mutableStateOf(false)}

                Row(Modifier.clickable {
                    showNearMe = !showNearMe
                }) {
                    Icon(Icons.Rounded.NearMe, "near me")
                    Text(it.getAddressLine(0))
                }

                AnimatedVisibility (showNearMe) {
                    Column {
                        (1..it.maxAddressLineIndex).forEach { i ->
                            Row {
                                Icon(Icons.Rounded.NearMe, "near me")
                                Text(it.getAddressLine(i))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun addressToString(address: Address): String {
    var addr = ""
    address.countryName?.let { if (it.isNotBlank()) addr += it }
    address.adminArea?.let { if (it.isNotBlank()) addr += ", $it" }
    address.subAdminArea?.let { if (it.isNotBlank()) addr += ", $it" }
    address.locality?.let { if (it.isNotBlank()) addr += ", $it" }
    address.subLocality?.let { if (it.isNotBlank()) addr += ", $it" }
    address.thoroughfare?.let { if (it.isNotBlank()) addr += ", $it" }
    address.subThoroughfare?.let { if (it.isNotBlank()) addr += ", $it" }
    address.featureName?.let { if (it.isNotBlank()) addr += ", $it" }
    address.premises?.let { if (it.isNotBlank()) addr += ", $it" }
    return addr
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
