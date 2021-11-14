package me.yangle.myphone

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
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var title by remember { mutableStateOf("My Phone") }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Drawer { key ->
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
                Sensors()
            }
            composable("GPS") {
                title = "GPS"
                Gps()
            }
            composable("Camera") {
                title = "Camera"
                Camera()
            }
        }
    }
}
