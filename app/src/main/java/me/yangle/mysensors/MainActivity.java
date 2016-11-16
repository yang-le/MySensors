package me.yangle.mysensors;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PREFIX = "TYPE_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for sensorType to String
        SparseArray<String> typeString = new SparseArray<>();

        Field fields[] = Sensor.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith(PREFIX)
                    && (field.getType() == int.class)) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers)) {
                    try {
                        typeString.put(field.getInt(null),
                                field.getName().substring(PREFIX.length()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        JSONArray array = new JSONArray();

        JSONArray title = new JSONArray();
        title.put(getString(R.string.type));
        title.put(getString(R.string.name));

        array.put(title);

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : allSensors) {
            JSONArray obj = new JSONArray();
            obj.put(typeString.get(sensor.getType()));
            obj.put(sensor.getName());

            array.put(obj);
        }

        Intent intent = new Intent(this, TableDisplayActivity.class);
        intent.putExtra("json", array.toString());
        startActivity(intent);
    }
}
