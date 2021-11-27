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
            DrawerItem(Icons.Rounded.PhoneAndroid, "My Phone", onClick = onClick)
            Divider()
            if (hasCamera)
                DrawerItem(Icons.Rounded.Camera, "Camera", onClick = onClick)
            if (hasGps)
                DrawerItem(Icons.Rounded.Satellite, "GNSS", onClick = onClick)
            DrawerItem(Icons.Rounded.Sensors, "Sensors", onClick = onClick)
            DrawerItem(Icons.Rounded.Memory, "CPU", onClick = onClick)
            DrawerItem(Icons.TwoTone.Memory, "GPU", onClick = onClick)
            DrawerItem(Icons.Rounded.Storage, "Storage", onClick = onClick)
            DrawerItem(Icons.Rounded.Info, "About", onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DrawerItem(
    icon: ImageVector? = null,
    text: String? = null,
    key: String = text ?: "",
    onClick: ((key: String) -> Unit)? = null
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
