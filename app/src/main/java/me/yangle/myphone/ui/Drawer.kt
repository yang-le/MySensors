package me.yangle.myphone.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.twotone.Memory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun Drawer(
    hasCamera: Boolean = LocalContext.current.packageManager.hasSystemFeature(
        PackageManager.FEATURE_CAMERA_ANY
    ),
    hasGps: Boolean = LocalContext.current.packageManager.hasSystemFeature(
        PackageManager.FEATURE_LOCATION_GPS
    ),
    onClick: ((key: String) -> Unit)? = null
) {
    Surface {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DrawerItem(Icons.Rounded.PhoneAndroid, "My Phone", onClick)
            Divider()
            if (hasCamera)
                DrawerItem(Icons.Rounded.Camera, "Camera", onClick)
            if (hasGps)
                DrawerItem(Icons.Rounded.Satellite, "GPS", onClick)
            DrawerItem(Icons.Rounded.Sensors, "Sensors", onClick)
            DrawerItem(Icons.Rounded.Memory, "CPU", onClick)
            DrawerItem(Icons.TwoTone.Memory, "GPU", onClick)
            DrawerItem(Icons.Rounded.Storage, "Storage", onClick)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DrawerItem(
    icon: ImageVector? = null,
    text: String? = null,
    onClick: ((key: String) -> Unit)? = null,
    key: String = text ?: ""
) = ListItem(
    modifier = onClick?.let {
        Modifier.clickable { it(key) }
    } ?: Modifier,
    icon = icon?.let {
        { Icon(it, null) }
    }
) {
    text?.let {
        Text(text = it)
    }
}
