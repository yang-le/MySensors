package me.yangle.myphone

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Button
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

@Composable
fun Gps() {
    val context = LocalContext.current
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var permissionGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted = it
        }

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

    if (!permissionGranted) {
        Snackbar(action = {
            Button(onClick = {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("OK")
            }
        }) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Text("Location access is required because blah blah blah..., please give us the permission")
            } else {
                Text("Location not available, please give us the permission")
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
        Snackbar(action = {
            Button(onClick = {
                locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {

                }, null)
            }) {
                Text("OK")
            }
        }) {
            Text("Location permission available")
        }
    }
}
