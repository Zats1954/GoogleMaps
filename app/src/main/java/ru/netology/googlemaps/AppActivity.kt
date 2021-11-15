package ru.netology.googlemaps

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback


class AppActivity : AppCompatActivity(R.layout.activity_main), OnMapReadyCallback {
    private var fragment = MapsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragment = MapsFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map, fragment)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.createMarker -> {
                fragment.addMarker()
                true
            }
            R.id.editeMarker -> {
                fragment.editMarker()
                true
            }
            R.id.deleteMarker -> {
                fragment.deleteMarker()
                true
            }
            R.id.showMarkers -> {
                fragment.showMarkers()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap){
            println("****************** Main onMapReady")
    }

}