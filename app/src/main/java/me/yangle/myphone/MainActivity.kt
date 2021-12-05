package me.yangle.myphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.yangle.myphone.ui.Drawer
import me.yangle.myphone.ui.theme.MyPhoneTheme

@AndroidEntryPoint
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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = rememberAnimatedNavController()
    var title by remember { mutableStateOf("My Phone") }
    var enableFilter by remember { mutableStateOf(false) }
    var enableCompass by remember { mutableStateOf(true) }

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
                },
                actions = {
                    if (title == "GNSS") {
                        IconButton(
                            onClick = {
                                enableCompass = !enableCompass
                            }
                        ) {
                            Icon(Icons.Rounded.Explore, null)
                        }
                        IconButton(
                            onClick = {
                                enableFilter = false
                            },
                            enabled = enableFilter
                        ) {
                            Icon(painterResource(R.drawable.filter_list_off), null)
                        }
                    }
                }
            )
        },
    ) {
        AnimatedNavHost(navController = navController, startDestination = "My Phone") {
            composable("My Phone") {
                title = "My Phone"
                MyPhone()
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
            composable("GNSS") {
                title = "GNSS"
                Gps(enableFilter = enableFilter, enableCompass = enableCompass) {
                    enableFilter = true
                }
            }
            composable("Camera") {
                title = "Camera"
                Camera()
            }
            composable("About") {
                title = "About"
                About()
            }
        }
    }
}
