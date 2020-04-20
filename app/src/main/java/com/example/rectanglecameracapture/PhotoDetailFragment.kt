package com.example.rectanglecameracapture

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PhotoDetailFragment : Fragment(), OnMapReadyCallback {
    lateinit var mapView: MapView
    var googleMap: GoogleMap? = null
    var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_photo_detail, container, false)

        mapView = view.findViewById(R.id.mapView)

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle =
                savedInstanceState.getBundle(resources.getString(R.string.google_maps_key))
        }

        mapView.onCreate(mapViewBundle)

        mapView.getMapAsync(this)
        return view
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle =
            outState.getBundle(resources.getString(R.string.google_maps_key))
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(resources.getString(R.string.google_maps_key), mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        val curLatLng: LatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        googleMap?.addMarker(MarkerOptions().position(curLatLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 15.0F))
    }
}
