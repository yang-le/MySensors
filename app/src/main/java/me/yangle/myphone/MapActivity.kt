package me.yangle.myphone

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


class MapActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getInstance().load(this, prefs)

        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        val options = intent.extras
        val mapController = map.controller
        mapController.setZoom(18.0)
        val startPoint =
            GeoPoint(options?.getDouble("lat") ?: 0.0, options?.getDouble("lon") ?: 0.0)
        mapController.setCenter(startPoint)

//        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
//        locationOverlay.enableMyLocation()
//        map.overlays.add(locationOverlay)

        val marker = Marker(map)
        marker.position = startPoint
        map.overlays.add(marker)

        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        val rotationGestureOverlay = RotationGestureOverlay(map)
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        val dm: DisplayMetrics = resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(map)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        map.overlays.add(scaleBarOverlay)

        val minimapOverlay = MinimapOverlay(this, map.tileRequestCompleteHandler)
        minimapOverlay.width = dm.widthPixels / 5
        minimapOverlay.height = dm.heightPixels / 5
        //optionally, you can set the minimap to a different tile source
        //minimapOverlay.setTileSource(....)
        map.overlays.add(minimapOverlay)
    }

    override fun onResume() {
        super.onResume()
//        getInstance().load(this, prefs);
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
//        getInstance().save(this, prefs);
        map.onPause()
    }

    companion object {
        fun launch(context: Context, lat: Double, lon: Double) =
            context.startActivity(
                Intent(context, MapActivity::class.java).putExtra("lat", lat).putExtra("lon", lon)
            )
    }
}
