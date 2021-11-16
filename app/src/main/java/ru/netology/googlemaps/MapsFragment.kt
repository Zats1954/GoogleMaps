package ru.netology.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import com.google.maps.android.ktx.utils.collection.addMarker
import com.google.android.gms.maps.model.Marker

import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.snackbar.Snackbar


class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    lateinit var collection: MarkerManager.Collection

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                googleMap.apply {
                    isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true

                }
            } else {
                Toast.makeText(requireContext(), "Требуется разрешение", Toast.LENGTH_SHORT).show()
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mapfragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        lifecycle.coroutineScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap().apply {
                isTrafficEnabled = true
                isBuildingsEnabled = true

                uiSettings.apply {
                    isZoomControlsEnabled = true
                    setAllGesturesEnabled(true)
                }
            }

            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    googleMap.apply {
                        isMyLocationEnabled = true
                        uiSettings.isMyLocationButtonEnabled = true

                    }

                    val fusedLocationProviderClient:FusedLocationProviderClient
                    fusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(this@MapsFragment.requireActivity())

                    fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        println(it) }

                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Toast.makeText(context, R.string.warning, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            val target = LatLng(48.99813, 21.27585)
            googleMap.awaitAnimateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition {
                        target(target)
                        zoom(15F)
                    }
                ))

            val markerManager = MarkerManager(googleMap)
            collection = markerManager.newCollection().apply {
                addMarker {
                    position(target)
                    getDrawable(requireContext(), R.drawable.ic_netology_48dp)?.let { icon(it) }
                    title("myPlace")
                    draggable(true)
                }.apply {
                    tag = "Any data here"
                }
            }
            collection.setOnMarkerClickListener { marker ->
                Toast.makeText(requireContext(), R.string.clickPoint, Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    fun MarkerOptions.icon(drawable: Drawable) {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        icon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }

    fun addMarker(){
        Toast.makeText(context,  R.string.makePoint , Toast.LENGTH_SHORT).show()
        setMapLongClick(googleMap)
    }
    fun editMarker(){
        Toast.makeText(context, R.string.editPoint, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun deleteMarker(){
        Toast.makeText(context, R.string.deletePoint, Toast.LENGTH_SHORT).show()
        googleMap.setOnMarkerClickListener { marker ->
            try{
                marker.remove()
            true}
            catch(e:Exception) {
                println("Can't delete marker $marker")
                false}
        }


    }
    fun showMarkers(){
        Toast.makeText(context, R.string.showAllPoints, Toast.LENGTH_SHORT).show()
        val builder = LatLngBounds.Builder()
        for (m in collection.markers) {
            builder.include(m.position)
        }
        val padding = 50
        val bounds = builder.build()
        val vision = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.setOnMapLoadedCallback({googleMap.moveCamera(vision)})
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener {latlng ->
        collection.addMarker(MarkerOptions().position(latlng))
        }
    }

    private fun setMapLongClick(map: GoogleMap){
        map.setOnMapLongClickListener {latLng ->
          collection.addMarker(MarkerOptions().position(latLng).title(latLng.toString()).draggable(true))
        }
    }
}