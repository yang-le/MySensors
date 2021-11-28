package me.yangle.myphone

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


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