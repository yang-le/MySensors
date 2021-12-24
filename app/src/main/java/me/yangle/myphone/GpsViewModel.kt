package me.yangle.myphone

import android.annotation.SuppressLint
import android.location.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GpsViewModel @Inject constructor(
    val locationManager: LocationManager,
    private val geocoder: Geocoder
) : ViewModel() {
    data class GnssData(
        val svid: Int,
        val type: Int,
        val azimuth: Float,
        val elevation: Float,
        val carrierFrequency: Float?,
        val Cn0DbHz: Float
    )

    val gnssData = mutableStateMapOf<Pair<Int, Int>, GnssData>()

    var location by mutableStateOf(Location(LocationManager.GPS_PROVIDER))
    var addressList by mutableStateOf(listOf(Address(Locale.getDefault())))

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    private fun getGnssStatus(): Flow<GnssStatus> = callbackFlow {
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                trySend(status)
            }
        }

        locationManager.registerGnssStatusCallback(callback, null)

        awaitClose {
            locationManager.unregisterGnssStatusCallback(callback)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    fun getLocation(): Flow<Location> = callbackFlow {
        val listener = LocationListener {
            trySend(it)
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 1000, 0f, listener
        )

        trySend(
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: Location(
                LocationManager.GPS_PROVIDER
            )
        )

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }

    private fun locationToAddress(location: Location) =
        Address(Locale.getDefault()).also {
            it.latitude = location.latitude
            it.longitude = location.longitude
        }

    init {
        viewModelScope.launch {
            getGnssStatus().collect { status ->
                (0 until status.satelliteCount).forEach {
                    gnssData[status.getConstellationType(it) to status.getSvid(it)] =
                        GnssData(
                            status.getSvid(it),
                            status.getConstellationType(it),
                            status.getAzimuthDegrees(it),
                            status.getElevationDegrees(it),
                            if (status.hasCarrierFrequencyHz(it)) status.getCarrierFrequencyHz(it) else null,
                            status.getCn0DbHz(it)
                        )
                }
            }
        }
        viewModelScope.launch {
            getLocation().collect {
                location = it
                if (Geocoder.isPresent()) {
                    addressList = withContext(Dispatchers.IO) {
                        try {
                            @Suppress("BlockingMethodInNonBlockingContext")
                            geocoder.getFromLocation(
                                it.latitude,
                                it.longitude,
                                5
                            )
                        } catch (e: IOException) {
                            listOf(locationToAddress(it))
                        }
                    }
                    if (addressList.isEmpty()) {
                        addressList = listOf(locationToAddress(it))
                    }
                } else {
                    addressList = listOf(locationToAddress(it))
                }
            }
        }
    }
}
