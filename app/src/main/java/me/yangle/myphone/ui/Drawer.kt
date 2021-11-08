package me.yangle.myphone.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.twotone.Memory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.theme.MyPhoneTheme

@Composable
fun Drawer(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    navController: NavController = rememberNavController()
) {
    val scope = rememberCoroutineScope()

    Surface {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DrawerItem(Icons.Rounded.PhoneAndroid, "My Phone") {
                scope.launch { drawerState.close() }
                navController.navigate("My Phone") {
                    popUpTo("My Phone") { inclusive = true }
                    launchSingleTop = true
                }
            }
            Divider()
            DrawerItem(Icons.Rounded.Sensors, "Sensors") {
                scope.launch { drawerState.close() }
                navController.navigate("Sensors") {
                    launchSingleTop = true
                }
            }
            DrawerItem(Icons.Rounded.Camera, "Camera") {
                scope.launch { drawerState.close() }
                navController.navigate("Camera") {
                    launchSingleTop = true
                }
            }
            DrawerItem(Icons.Rounded.Memory, "CPU") {}
            DrawerItem(Icons.TwoTone.Memory, "GPU") {}
            DrawerItem(Icons.Rounded.Storage, "Storage") {}
            DrawerItem(Icons.Rounded.Satellite, "GPS") {
                scope.launch { drawerState.close() }
                navController.navigate("GPS") {
                    launchSingleTop = true
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector? = null,
    text: String? = null,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null)
        Modifier
            .height(64.dp)
            .fillMaxSize()
            .clickable(onClick = onClick)
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

@Preview(name = "Light theme")
@Preview(name = "Dark theme", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    MyPhoneTheme {
        Drawer()
    }
}
