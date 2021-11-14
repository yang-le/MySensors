package me.yangle.myphone

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.yangle.myphone.ui.Table

@Composable
fun Camera(cameraManager: CameraManager = LocalContext.current.getSystemService(Context.CAMERA_SERVICE) as CameraManager) {
    fun <T> getCameraCharacteristics(key: CameraCharacteristics.Key<T>): List<String> {
        return cameraManager.cameraIdList.map {
            val value = cameraManager.getCameraCharacteristics(it).get(key)
            if (value is FloatArray?) value?.toList().toString()
            else value.toString()
        }
    }

    Table(
        listOf(
            "AE COMPENSATION",
            "APERTURES",
            "FOCAL LENGTH",
            "EXPOSURE TIME",
            "ISO"
        ).zip(
            listOf(
                getCameraCharacteristics(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE),
                getCameraCharacteristics(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES),
                getCameraCharacteristics(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS),
                getCameraCharacteristics(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE),
                getCameraCharacteristics(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            )
        ) { s, l -> listOf(s) + l },
        listOf("Facing") + getCameraCharacteristics(CameraCharacteristics.LENS_FACING).map {
            when (it.toInt()) {
                CameraMetadata.LENS_FACING_FRONT -> "FRONT"
                CameraMetadata.LENS_FACING_BACK -> "BACK"
                CameraMetadata.LENS_FACING_EXTERNAL -> "EXTERNAL"
                else -> "UNKNOWN"
            }
        }
    )
}
