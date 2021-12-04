package me.yangle.myphone

import android.content.Context
import android.location.LocationManager
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyPhone() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val gnssYear = if (locationManager.gnssYearOfHardware > 0) "${locationManager.gnssYearOfHardware}" else "pre 2016"
    Text("GNSS: ${locationManager.gnssHardwareModelName} @ $gnssYear")
}
