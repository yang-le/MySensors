package me.yangle.myphone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PREFIX = "TYPE_";
    private JSONArray gpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            regGpsStatusCallback();
        }

        OclHelper.Hello();
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

    private void regGpsStatusCallback()
    {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    super.onSatelliteStatusChanged(status);

                    gpsStatus = new JSONArray();
                    gpsStatus.put(getString(R.string.gps));

                    JSONArray title = new JSONArray();
                    title.put(getString(R.string.type));
                    title.put(getString(R.string.svid));
                    gpsStatus.put(title);

                    for (int i = 0; i < status.getSatelliteCount(); ++i)
                    {
                        JSONArray obj = new JSONArray();
                        obj.put(status.getConstellationType(i));
                        obj.put(status.getSvid(i));
                        gpsStatus.put(obj);
                    }
                }

                @Override
                public void onStarted() {
                    super.onStarted();
                    Toast.makeText(MainActivity.this ,getString(R.string.gps_open), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    Toast.makeText(MainActivity.this ,getString(R.string.gps_close), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e){
            e.printStackTrace();
        };
    }

    public void onGpsBtnClick(View v) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // TODO request location
        if (gpsStatus != null) {
            Intent intent = new Intent(this, TableDisplayActivity.class);
            intent.putExtra("json", gpsStatus.toString());
            startActivity(intent);
        } else {
            Toast.makeText(this ,getString(R.string.gps_no_signal), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            regGpsStatusCallback();
        }
    }

    public void onQuitBtnClick(View v)
    {
        finish();
    }
}
