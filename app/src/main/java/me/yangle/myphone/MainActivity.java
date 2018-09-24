package me.yangle.myphone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PREFIX = "TYPE_";
    private JSONArray gpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //OclHelper.Hello();
    }

    public void onSensorBtnClick(View v) {
        JSONArray array = new JSONArray();

        array.put(getString(R.string.sensor));

        JSONArray title = new JSONArray();
        title.put(getString(R.string.type));
        title.put(getString(R.string.name));
        title.put(getString(R.string.vendor));

        array.put(title);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : allSensors) {
            JSONArray obj = new JSONArray();
            obj.put(sensor.getStringType());
            obj.put(sensor.getName());
            obj.put(sensor.getVendor());

            array.put(obj);
        }

        Intent intent = new Intent(this, TableDisplayActivity.class);
        intent.putExtra("json", array.toString());
        startActivity(intent);
    }

    public void onCpuBtnClick(View v) {
        finish();
    }

    public void onGpuBtnClick(View v) {
        finish();
    }

    public void onStorageBtnClick(View v) {
        finish();
    }

    public void onGpsBtnClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, getString(R.string.gps_no_signal), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);

                gpsStatus = new JSONArray();
                gpsStatus.put(getString(R.string.gps));

                JSONArray title = new JSONArray();
                title.put(getString(R.string.type));
                title.put(getString(R.string.svid));
                title.put(getString(R.string.gps_azimuth));
                title.put(getString(R.string.gps_elevation));
                title.put(getString(R.string.gps_carrier_frequency));
                title.put(getString(R.string.gps_cn));
                gpsStatus.put(title);

                String type[] = {
                        getString(R.string.unknow),
                        getString(R.string.gps),
                        getString(R.string.gps_sbas),
                        getString(R.string.gps_glonass),
                        getString(R.string.gps_qzss),
                        getString(R.string.gps_beidou),
                        getString(R.string.gps_galileo)
                };

                for (int i = 0; i < status.getSatelliteCount(); ++i) {
                    JSONArray obj = new JSONArray();
                    obj.put(type[status.getConstellationType(i)]);
                    obj.put(status.getSvid(i));
                    try {
                        obj.put(status.getAzimuthDegrees(i));
                        obj.put(status.getElevationDegrees(i));
                        obj.put(status.hasCarrierFrequencyHz(i) ? status.getCarrierFrequencyHz(i) : "N/A");
                        obj.put(status.getCn0DbHz(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    gpsStatus.put(obj);
                }
            }

            @Override
            public void onStarted() {
                super.onStarted();
            }

            @Override
            public void onStopped() {
                super.onStopped();
            }
        });

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(MainActivity.this, getString(R.string.gps_open), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(MainActivity.this, getString(R.string.gps_close), Toast.LENGTH_SHORT).show();
            }
        });

        if (gpsStatus != null) {
            Intent intent = new Intent(this, TableDisplayActivity.class);
            intent.putExtra("json", gpsStatus.toString());
            startActivity(intent);
        } else {
            Toast.makeText(this ,getString(R.string.gps_no_signal), Toast.LENGTH_SHORT).show();
        }
    }

    public void onQuitBtnClick(View v)
    {
        finish();
    }
}
