package me.yangle.myphone

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
