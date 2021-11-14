package me.yangle.myphone

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.Compass
import me.yangle.myphone.ui.Table

class SensorViewModel(
    private val sensorManager: SensorManager,
    private val sensors: List<Sensor>
) : ViewModel() {
    var orientation by mutableStateOf(FloatArray(3))
        private set

    private var geomagnetic = FloatArray(3)
    private var gravity = FloatArray(3)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getSensorData(): Flow<SensorEvent?> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor in sensors) {
                    trySend(event)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sensors.forEach {
            sensorManager.registerListener(listener, it, 100000)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    init {
        viewModelScope.launch {
            getSensorData().collect {
                it?.let {
                    val rotationMatrix = FloatArray(9)
                    val tempOrientation = FloatArray(3)

                    when (it.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(
                                rotationMatrix,
                                it.values
                            )
                            orientation =
                                SensorManager.getOrientation(rotationMatrix, tempOrientation)
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            geomagnetic = it.values
                            if (SensorManager.getRotationMatrix(
                                    rotationMatrix,
                                    null,
                                    gravity,
                                    geomagnetic
                                )
                            ) {
                                orientation =
                                    SensorManager.getOrientation(rotationMatrix, tempOrientation)
                            }
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            gravity = it.values
                            if (SensorManager.getRotationMatrix(
                                    rotationMatrix,
                                    null,
                                    gravity,
                                    geomagnetic
                                )
                            ) {
                                orientation =
                                    SensorManager.getOrientation(rotationMatrix, tempOrientation)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Sensors(sensorManager: SensorManager) {
    var showCompass by remember { mutableStateOf(false) }
    var sensors by remember { mutableStateOf(emptyList<Sensor>()) }

    if (!showCompass) {
        val allSensors = remember { sensorManager.getSensorList(Sensor.TYPE_ALL) }
        val sensorMap = remember {
            val sensorMap = mutableMapOf<String, MutableMap<String, Sensor>>()
            allSensors.forEach {
                if (!sensorMap.containsKey(it.stringType)) {
                    sensorMap[it.stringType] = mutableMapOf()
                }
                sensorMap[it.stringType]?.set(it.name, it)
            }
            sensorMap
        }

        Table(allSensors.map {
            listOf(it.stringType, it.name, it.vendor)
        }, listOf("Type", "Name", "Vendor")) { (type, name, _) ->
            when (type) {
                Sensor.STRING_TYPE_ROTATION_VECTOR -> {
                    showCompass = true
                    sensors = listOf(sensorMap[type]?.get(name)) as List<Sensor>
                }
                Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                    showCompass = true
                    sensors = listOf(
                        sensorMap[type]?.get(name),
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    ) as List<Sensor>
                }
                Sensor.STRING_TYPE_ACCELEROMETER -> {
                    showCompass = true
                    sensors = listOf(
                        sensorMap[type]?.get(name),
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                    ) as List<Sensor>
                }
            }
        }
    } else {
        val viewModel: SensorViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return SensorViewModel(sensorManager, sensors) as T
                }
            }
        )

        Column {
            Compass(viewModel.orientation)
            Button({
                showCompass = false
            }) {
                Text("Back")
            }
        }
    }
}
