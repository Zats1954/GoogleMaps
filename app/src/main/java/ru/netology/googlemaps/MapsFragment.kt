package ru.netology.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.awaitAnimateCamera
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.model.cameraPosition
import com.google.maps.android.ktx.utils.collection.addMarker
import ru.netology.googlemaps.adapter.MarkersAdapter
import ru.netology.googlemaps.databinding.FragmentMapsBinding


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var googleMap: GoogleMap
    lateinit var collection: MarkerManager.Collection
    var nomMarker: Int = 0
    var action: String = "info"
    private var _data: MutableLiveData<List<Marker>> = MutableLiveData()
    val data: LiveData<List<Marker>>
        get() = _data
    private lateinit var binding: FragmentMapsBinding

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
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val adapter = MarkersAdapter()
        binding.rwSpisok.adapter = adapter
        binding.rwSpisok.addItemDecoration(
            DividerItemDecoration(
                binding.rwSpisok.context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rwSpisok.isVisible = false
        data.observe(viewLifecycleOwner, {
            adapter.submitList(it.toList())
        })
        return binding.root
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

                    val fusedLocationProviderClient: FusedLocationProviderClient
                    fusedLocationProviderClient = LocationServices
                        .getFusedLocationProviderClient(this@MapsFragment.requireActivity())

                    fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        println("first location $it")
                    }

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
                )
            )

            val markerManager = MarkerManager(googleMap)
            collection = markerManager.newCollection().apply {
                addMarker {
                    position(target)
                    getDrawable(requireContext(), R.drawable.ic_netology_48dp)?.let { icon(it) }
                    title("marker${++nomMarker}")
                    draggable(false)
                }
            }
            _data.value = collection.markers.toList()
        }
    }

    private suspend fun moveToLocation(target: LatLng) {
        googleMap.awaitAnimateCamera(
            CameraUpdateFactory.newCameraPosition(
                cameraPosition {
                    target(target)
                    zoom(15F)
                }
            )
        )
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

    fun addMarker() {
        Toast.makeText(context, R.string.makePoint, Toast.LENGTH_SHORT).show()
        googleMap.setOnMapLongClickListener { latLng ->
            collection.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("marker${++nomMarker}")
                    .draggable(false)
            ).apply {
                this.showInfoWindow()
                _data.value = collection.markers.toList()
            }
        }
        _data.value = collection.markers.toList()
    }

    fun editMarker() {
        Toast.makeText(context, R.string.editPoint, Toast.LENGTH_SHORT).show()
        action = "edit"
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
        }
    }

    fun showMarker() {
        action = "show"
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    fun deleteMarker() {
        Toast.makeText(context, R.string.deletePoint, Toast.LENGTH_SHORT).show()
        action = "remove"
        googleMap.setOnMarkerClickListener { marker ->
            onMarkerClick(marker)
        }
    }

    fun showMarkers() {
        Toast.makeText(context, R.string.showAllPoints, Toast.LENGTH_SHORT).show()
        val builder = LatLngBounds.Builder()
        for (m in collection.markers) {
            builder.include(m.position)
        }
        val padding = 50
        val bounds = builder.build()
        val vision = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.setOnMapLoadedCallback({ googleMap.moveCamera(vision) })
        requireView().findViewById<RecyclerView>(R.id.rwSpisok).isVisible = true
    }


    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapClickListener { latlng ->
            collection.addMarker(MarkerOptions().position(latlng))
            _data.value = collection.markers.toList()
        }
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        when (action) {

            "edit" -> {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(HUE_BLUE))
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow()
                }
                lifecycleScope.launchWhenCreated { moveToLocation(marker.position) }
                view?.let { vw ->
                    val group = binding.editGroup
                    binding.etTitle.setText(marker.title)
                    binding.etTitle.doOnTextChanged { text, start, before, count ->
                        marker.title = text.toString()
                        collection.markers.find { it == marker }
                    }
                    group.isVisible = true
                    binding.tvOK.setOnClickListener { button ->
                        collection.remove(marker)
                        collection.addMarker(
                            MarkerOptions()
                                .position(marker.position)
                                .title(marker.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED))
                        )
                        _data.value = collection.markers.toList()
                        group.isVisible = false
                    }

                }
            }

            "show" -> {
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow()
                }
            }

            "remove" -> {
                try {
                    collection.remove(marker)
                    _data.value = collection.markers.toList()
                } catch (e: Exception) {
                    println("Can't delete marker $marker")
                    return false
                }
            }

            else -> {
                return false
            }
        }
        return true
    }

    fun clear() {
        binding.rwSpisok.isVisible = false
        binding.editGroup.isVisible = false
    }
}