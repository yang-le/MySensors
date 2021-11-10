package me.yangle.myphone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Button
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat

class GpsState(
    permissionGranted: Boolean,
    providerEnabled: Boolean,
    val showRationale: Boolean
) {
    var permissionGranted by mutableStateOf(permissionGranted)
        private set

    var permissionDenied by mutableStateOf(false)
        private set

    var providerEnabled by mutableStateOf(providerEnabled)

    fun onRequestPermissionResult(result: Boolean) {
        permissionGranted = result
        permissionDenied = !result
    }
}

@Composable
fun rememberGpsState(context: Context): GpsState {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return remember {
        GpsState(
            permissionGranted = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER),
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}

@Composable
fun Gps(
    locationManager: LocationManager, gpsState: GpsState
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        gpsState::onRequestPermissionResult
    )

    val startActivityLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        gpsState.providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    if (gpsState.permissionDenied) {
        Text("You denied our request, if you want use this function, please consider allow the Location Permission from system settings.")
    } else if (!gpsState.permissionGranted) {
        if (gpsState.showRationale) {
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
    } else if (!gpsState.providerEnabled) {
        Snackbar(action = {
            Button(onClick = { startActivityLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                Text("OK")
            }
        }) {
            Text("Please open GPS switch")
        }
    }
    // observe data and update
//    else {
//        locationManager.registerGnssStatusCallback(callback, null)
//        locationManager.requestLocationUpdates(
//            LocationManager.GPS_PROVIDER, 1000, 0f
//        ) {
//
//        }
//    }
}
