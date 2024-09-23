package cs10.apps.travels.tracer.pages.stops.creator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cs10.apps.travels.tracer.R

class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var placeName: String? = null
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)

        // LOAD COORDS FROM CALLING ACTIVITY
        latitude = intent.getDoubleExtra("lat", 0.0)
        longitude = intent.getDoubleExtra("long", 0.0)
        placeName = intent.getStringExtra("name")

        // CREATE FRAGMENT
        val f = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        f.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        createMarker()
    }

    private fun createMarker(){
        val coords = LatLng(latitude, longitude)
        val marker = MarkerOptions().position(coords).title(placeName)
        googleMap.addMarker(marker)

        // zoom
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coords, 18f), 4000, null
        )
    }
}