package ru.netology.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import com.google.maps.android.ktx.utils.collection.addMarker

class MapsFragment : Fragment() {
    private lateinit var googleMap: GoogleMap

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                googleMap.apply {
                    isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true
                }
            } else {
                // TODO: show sorry dialog
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

                    val fusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(requireActivity())
                    fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        println(it)
                    }
                }
                // 2. Должны показать обоснование необходимости прав
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO: show rationale dialog
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            val target = LatLng(48.9981, 21.2758)
            googleMap.awaitAnimateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition {
                        target(target)
                        zoom(15F)
                    }
                ))

            val markerManager = MarkerManager(googleMap)
            val collection: MarkerManager.Collection = markerManager.newCollection().apply {
                addMarker {
                    position(target)
//                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_netology_48dp))
                    icon(getDrawable(requireContext(), R.drawable.ic_netology_48dp)!!)
                    title("myPlace")
                }.apply {
                    tag = "Any data here"
                }
            }

            collection.setOnMarkerClickListener { marker ->
                Toast.makeText(requireContext(), "Marker is Clicked", Toast.LENGTH_SHORT).show()
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
}