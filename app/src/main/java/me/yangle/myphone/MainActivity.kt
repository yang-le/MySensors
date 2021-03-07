package me.yangle.myphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private var gpsStatus: JSONArray? = null
    private val cameraManager: CameraManager by lazy { getSystemService(CameraManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //OclHelper.Hello();
    }

    fun onSensorBtnClick(v: View?) {
        val array = JSONArray()
        array.put(getString(R.string.sensor))
        val title = JSONArray()
        title.put(getString(R.string.type))
        title.put(getString(R.string.name))
        title.put(getString(R.string.vendor))
        array.put(title)
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in allSensors) {
            val obj = JSONArray()
            obj.put(sensor.stringType)
            obj.put(sensor.name)
            obj.put(sensor.vendor)
            array.put(obj)
        }
        val intent = Intent(this, TableDisplayActivity::class.java)
        intent.putExtra("json", array.toString())
        startActivity(intent)
    }

    fun onCpuBtnClick(v: View?) {
        finish()
    }

    fun onGpuBtnClick(v: View?) {
        finish()
    }

    fun onStorageBtnClick(v: View?) {
        finish()
    }

    fun onCameraBtnClick(v: View?) {
        // Request camera permissions
        if (allPermissionsGranted()) {
            val array = JSONArray()
            array.put(getString(R.string.camera))

            val title = JSONArray()
            title.put(getString(R.string.id))
            title.put(getString(R.string.parameter))
            title.put(getString(R.string.min))
            title.put(getString(R.string.max))
            array.put(title)

            cameraManager.cameraIdList.forEach { cameraId ->
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)

                val fc = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                val facing = JSONArray()
                facing.put(cameraId)
                facing.put(getString(R.string.facing))
                if (fc == CameraMetadata.LENS_FACING_FRONT) {
                    facing.put(getString(R.string.front))
                    facing.put(getString(R.string.front))
                } else if (fc == CameraMetadata.LENS_FACING_BACK) {
                    facing.put(getString(R.string.back))
                    facing.put(getString(R.string.back))
                } else if (fc == CameraMetadata.LENS_FACING_EXTERNAL) {
                    facing.put(getString(R.string.external))
                    facing.put(getString(R.string.external))
                } else {
                    facing.put(getString(R.string.unknow))
                    facing.put(getString(R.string.unknow))
                }
                array.put(facing)

                val ae = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)!!
                val AE = JSONArray()
                AE.put(cameraId)
                AE.put(getString(R.string.AE))
                AE.put(ae.lower)
                AE.put(ae.upper)
                array.put(AE)

                val ap = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)!!
                val AP = JSONArray()
                AP.put(cameraId)
                AP.put(getString(R.string.AP))
                AP.put(ap.first())
                AP.put(ap.last())
                array.put(AP)

                val fl = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)!!
                val FC = JSONArray()
                FC.put(cameraId)
                FC.put(getString(R.string.FC))
                FC.put(fl.first())
                FC.put(fl.last())
                array.put(FC)

                val et = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!
                val ET = JSONArray()
                ET.put(cameraId)
                ET.put(getString(R.string.ET))
                ET.put("" + et.lower / 1e3 + "us")
                ET.put("" + et.upper / 1e6 + "ms")
                array.put(ET)

                val iso = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
                val ISO = JSONArray()
                ISO.put(cameraId)
                ISO.put(getString(R.string.ISO))
                ISO.put(iso.lower)
                ISO.put(iso.upper)
                array.put(ISO)
            }

            val intent = Intent(this, TableDisplayActivity::class.java)
            intent.putExtra("json", array.toString())
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    fun onGpsBtnClick(v: View?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, getString(R.string.gps_no_signal), Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, 0)
            return
        }
        locationManager.registerGnssStatusCallback(object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                gpsStatus = JSONArray()
                gpsStatus!!.put(getString(R.string.gps))
                val title = JSONArray()
                title.put(getString(R.string.type))
                title.put(getString(R.string.svid))
                title.put(getString(R.string.gps_azimuth))
                title.put(getString(R.string.gps_elevation))
                title.put(getString(R.string.gps_carrier_frequency))
                title.put(getString(R.string.gps_cn))
                gpsStatus!!.put(title)
                val type = arrayOf(
                        getString(R.string.unknow),
                        getString(R.string.gps),
                        getString(R.string.gps_sbas),
                        getString(R.string.gps_glonass),
                        getString(R.string.gps_qzss),
                        getString(R.string.gps_beidou),
                        getString(R.string.gps_galileo)
                )
                for (i in 0 until status.satelliteCount) {
                    val obj = JSONArray()
                    obj.put(type[status.getConstellationType(i)])
                    obj.put(status.getSvid(i))
                    try {
                        obj.put(status.getAzimuthDegrees(i).toDouble())
                        obj.put(status.getElevationDegrees(i).toDouble())
                        obj.put(if (status.hasCarrierFrequencyHz(i)) status.getCarrierFrequencyHz(i) else "N/A")
                        obj.put(status.getCn0DbHz(i).toDouble())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    gpsStatus!!.put(obj)
                }
            }

            override fun onStarted() {
                super.onStarted()
            }

            override fun onStopped() {
                super.onStopped()
            }
        })
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, object : LocationListener {
            override fun onLocationChanged(location: Location) {}
            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
            override fun onProviderEnabled(s: String) {
                Toast.makeText(this@MainActivity, getString(R.string.gps_open), Toast.LENGTH_SHORT).show()
            }

            override fun onProviderDisabled(s: String) {
                Toast.makeText(this@MainActivity, getString(R.string.gps_close), Toast.LENGTH_SHORT).show()
            }
        })
        if (gpsStatus != null) {
            val intent = Intent(this, TableDisplayActivity::class.java)
            intent.putExtra("json", gpsStatus.toString())
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.gps_no_signal), Toast.LENGTH_SHORT).show()
        }
    }

    fun onQuitBtnClick(v: View?) {
        finish()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                onCameraBtnClick(null)
            }else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val PREFIX = "TYPE_"
        private const val TAG = "MyPhone"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}