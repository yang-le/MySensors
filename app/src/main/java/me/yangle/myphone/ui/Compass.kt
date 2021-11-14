package me.yangle.myphone.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import me.yangle.myphone.R

@Preview
@Composable
fun Compass(orientation: FloatArray = FloatArray(3)) {
    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
    val modifier =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (orientation[2] < 0) {   // left
                Modifier
                    .fillMaxHeight()
                    .rotate(-90 - degrees)
            } else {
                Modifier
                    .fillMaxHeight()
                    .rotate(90 - degrees)
            }
        } else {
            Modifier
                .fillMaxWidth()
                .rotate(-degrees)
        }
    Image(
        painterResource(id = R.drawable.compass),
        "compass",
        modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
