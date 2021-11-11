package me.yangle.myphone

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat

class PermissionState(
    permissionGranted: Boolean,
    val showRationale: Boolean
) {
    var permissionGranted by mutableStateOf(permissionGranted)
        private set

    var permissionDenied by mutableStateOf(false)
        private set

    fun onRequestPermissionResult(result: Boolean) {
        permissionGranted = result
        permissionDenied = !result
    }
}

@Composable
fun rememberPermissionState(context: Context, permission: String): PermissionState {
    return remember {
        PermissionState(
            permissionGranted = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                permission
            ),
            showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permission
            )
        )
    }
}
