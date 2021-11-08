package me.yangle.myphone

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.yangle.myphone.ui.Table

@Composable
fun Sensors() {
    val sensorManager =
        LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    Table(allSensors.map {
        listOf(it.stringType, it.name, it.vendor)
    }, listOf("Type", "Name", "Vendor"))
}
