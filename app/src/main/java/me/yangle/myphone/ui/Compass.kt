package me.yangle.myphone.ui

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.GnssStatus
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import me.yangle.myphone.GpsViewModel
import me.yangle.myphone.R
import me.yangle.myphone.SensorViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Compass(orientation: FloatArray = FloatArray(3), onDraw: (DrawScope.() -> Unit)? = null) {
    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
    val modifier =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (orientation[2] < 0) {   // left
                Modifier.rotate(-90 - degrees)
            } else {
                Modifier.rotate(90 - degrees)
            }.fillMaxHeight()
        } else {
            Modifier
                .rotate(-degrees)
                .fillMaxWidth()
        }
            .clip(CircleShape)
            .drawWithContent {
                drawContent()
                onDraw?.invoke(this)
            }

    Image(
        painterResource(id = R.drawable.compass),
        "compass",
        modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun Compass(data: Collection<GpsViewModel.GnssData>) {
    val iconSize = 50
    val sensorManager: SensorManager =
        LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val viewModel: SensorViewModel = viewModel(
        key = "compass",
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SensorViewModel(
                    sensorManager, listOf(
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                    ) as List<Sensor>
                ) as T
            }
        }
    )
    val borderColor = MaterialTheme.colors.secondaryVariant
    Compass(viewModel.orientation) {
        data.forEach {
            val center = Offset(size.width / 2, size.height / 2)
            val e = Math.toRadians(it.elevation.toDouble())
            val a = Math.toRadians(it.azimuth.toDouble())
            val r = (size.width + size.height) / 7  // actually / 4 * 4 / 7
            val l = r * cos(e)
            val x = l * cos(a)
            val y = l * sin(a)
            drawCircle(borderColor, r, center, style = Stroke())
            drawImage(
                EmojiDrawable(constellationTypeToIcon(it.type)).toBitmap(iconSize, iconSize)
                    .asImageBitmap(),
                center - Offset(iconSize / 2 - y.toFloat(), iconSize / 2 + x.toFloat())
            )
        }
    }
}

private fun constellationTypeToIcon(type: Int) = when (type) {
    GnssStatus.CONSTELLATION_GPS -> "\uD83C\uDDFA\uD83C\uDDF8"
    GnssStatus.CONSTELLATION_SBAS -> "SBAS"
    GnssStatus.CONSTELLATION_GLONASS -> "\uD83C\uDDF7\uD83C\uDDFA"
    GnssStatus.CONSTELLATION_QZSS -> "\uD83C\uDDEF\uD83C\uDDF5"
    GnssStatus.CONSTELLATION_BEIDOU -> "\uD83C\uDDE8\uD83C\uDDF3"
    GnssStatus.CONSTELLATION_GALILEO -> "\uD83C\uDDEA\uD83C\uDDFA"
    GnssStatus.CONSTELLATION_IRNSS -> "\uD83C\uDDEE\uD83C\uDDF3"
    else -> "❔"
}
