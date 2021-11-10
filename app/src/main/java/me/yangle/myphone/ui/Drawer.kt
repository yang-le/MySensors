package me.yangle.myphone.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.twotone.Memory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun Drawer(
    hasCamera: Boolean = true,
    hasGps: Boolean = true,
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

@Composable
private fun DrawerItem(
    icon: ImageVector? = null,
    text: String? = null,
    onClick: ((key: String) -> Unit)? = null,
    key: String = text ?: ""
) {
    val modifier = if (onClick != null)
        Modifier
            .height(64.dp)
            .fillMaxSize()
            .clickable { onClick(key) }
    else
        Modifier
            .height(64.dp)
            .fillMaxSize()

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(it, null, Modifier.padding(16.dp))
        }
        text?.let {
            Text(it, Modifier.padding(16.dp))
        }
    }
}
