package me.yangle.myphone

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import me.yangle.myphone.ui.Table

@Composable
fun Sensors(sensorManager: SensorManager) {
    val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
    Table(allSensors.map {
        listOf(it.stringType, it.name, it.vendor)
    }, listOf("Type", "Name", "Vendor"))
}
