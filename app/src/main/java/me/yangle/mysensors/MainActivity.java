package me.yangle.mysensors;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

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

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : allSensors) {
            TextView type = new TextView(this);
            type.setText(typeString.get(sensor.getType()));

            TextView name = new TextView(this);
            name.setText(sensor.getName());

            TableRow row = new TableRow(this);
            row.addView(type);
            row.addView(name);

            ViewGroup layout = (ViewGroup) findViewById(R.id.activity_main);
            layout.addView(row);
        }
    }
}
