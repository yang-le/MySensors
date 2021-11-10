package me.yangle.myphone

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.Drawer
import me.yangle.myphone.ui.theme.MyPhoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPhoneTheme {
                HomeScreen(this)
            }
        }
    }
}

@Composable
fun HomeScreen(context: Context) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var title by remember { mutableStateOf("My Phone") }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Drawer(
                context.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_ANY
                ), context.packageManager.hasSystemFeature(
                    PackageManager.FEATURE_LOCATION_GPS
                )
            ) { key ->
                scope.launch { scaffoldState.drawerState.close() }
                navController.navigate(key) {
                    launchSingleTop = true
                    if (key == "My Phone")
                        popUpTo("My Phone")
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch { scaffoldState.drawerState.open() }
                        }
                    ) {
                        Icon(Icons.Filled.Menu, null)
                    }
                }
            )
        },
    ) {
        NavHost(navController = navController, startDestination = "My Phone") {
            composable("My Phone") {
                title = "My Phone"
            }
            composable("CPU") {
                title = "CPU"
            }
            composable("GPU") {
                title = "GPU"
            }
            composable("Storage") {
                title = "Storage"
            }
            composable("Sensors") {
                title = "Sensors"
                Sensors(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
            }
            composable("GPS") {
                title = "GPS"
                val gpsState = rememberGpsState(context)
                Gps(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager, gpsState)
            }
            composable("Camera") {
                title = "Camera"
                Camera(context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
            }
        }
    }
}
