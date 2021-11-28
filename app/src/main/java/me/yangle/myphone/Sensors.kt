package me.yangle.myphone

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import me.yangle.myphone.ui.Compass
import me.yangle.myphone.ui.Table


@Composable
fun Sensors(sensorManager: SensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager) {
    var showCompass by remember { mutableStateOf(false) }
    var sensors by remember { mutableStateOf(emptyList<Sensor>()) }

    if (!showCompass) {
        val allSensors = remember { sensorManager.getSensorList(Sensor.TYPE_ALL) }

        Table(allSensors.map {
            listOf(it.stringType, it.name, it.vendor)
        }, listOf("Type", "Name", "Vendor")) { (type, name, _) ->
            when (type) {
                Sensor.STRING_TYPE_ROTATION_VECTOR -> {
                    showCompass = true
                    sensors = listOf(allSensors.find {
                        it.stringType == type && it.name == name
                    }!!)
                }
                Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                    showCompass = true
                    sensors = listOf(
                        allSensors.find { it.stringType == type && it.name == name }!!,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    )
                }
                Sensor.STRING_TYPE_ACCELEROMETER -> {
                    showCompass = true
                    sensors = listOf(
                        allSensors.find { it.stringType == type && it.name == name }!!,
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                    )
                }
            }
        }
    } else {
        val viewModel: SensorViewModel = viewModel(
            key = sensors[0].stringType,
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return SensorViewModel(sensorManager, sensors) as T
                }
            }
        )

        Compass(viewModel.orientation)
        BackHandler {
            showCompass = false
        }
    }
}
