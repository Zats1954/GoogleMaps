package ru.netology.googlemaps

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class AppActivity : AppCompatActivity(R.layout.activity_main) {
    var fragment = MapsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            supportFragmentManager.beginTransaction()
                .replace(R.id.mapa, fragment)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fragment.clear()
        return when (item.itemId) {
            R.id.createMarker -> {
                fragment.addMarker()
                true
            }
            R.id.editMarker -> {
                fragment.editMarker()
                true
            }
            R.id.showMarker -> {
                fragment.showMarker()
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
}