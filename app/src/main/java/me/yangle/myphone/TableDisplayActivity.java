package me.yangle.myphone;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

class TableDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_display);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try {
            JSONArray array = new JSONArray(getIntent().getStringExtra("json"));

            setTitle(array.getString(0));

            ViewGroup layout = (ViewGroup) findViewById(R.id.activity_table_display);
            for (int i = 1; i < array.length(); ++i) {
                JSONArray obj = array.getJSONArray(i);

                TableRow row = new TableRow(this);
                for (int j = 0; j < obj.length(); ++j) {
                    TextView column = new TextView(this);
                    column.setText(obj.getString(j));

                    row.addView(column);
                }

                layout.addView(row);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
