package me.yangle.myphone

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException

class TableDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_display)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        try {
            val array = JSONArray(intent.getStringExtra("json"))
            title = array.getString(0)
            val layout = findViewById<View>(R.id.activity_table_display) as ViewGroup
            for (i in 1 until array.length()) {
                val obj = array.getJSONArray(i)
                val row = TableRow(this)
                for (j in 0 until obj.length()) {
                    val column = TextView(this)
                    column.text = obj.getString(j)
                    row.addView(column)
                }
                layout.addView(row)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}