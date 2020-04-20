package com.example.rectanglecameracapture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class MainActivity : AppCompatActivity(), PhotoFragment.PhotoFragmentAction,
    PhotoListFragment.PhotoListAction, ImageFragment.ImageFragmentListener {
    var PERMISSION_ALL = 1
    var flagPermissions = false
    private val UPDATE_INTERVAL: Long = 5000

    var PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    var location: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = UPDATE_INTERVAL

        locationCallback = object: LocationCallback() {
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
            }

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
            }
        }

        checkPermissions();

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.res_photo_layout, PhotoListFragment())
            .commit()
    }

    fun checkPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    PERMISSIONS,
                    PERMISSION_ALL
                )
            }
            flagPermissions = false
        }
        getLocation()
        flagPermissions = true
    }

    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun getLocation() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            this@MainActivity.mainLooper
        )
        fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(OnSuccessListener<Location> { location ->
                this.location = location
            })

        fusedLocationProviderClient.getLastLocation().addOnFailureListener(OnFailureListener { e ->
            Log.i(
                "MainActivity",
                "Exception while getting the location: " + e.message
            )
        })
    }

    private fun stopLocationRequests() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationRequests()
    }

    override fun onCaptureAction() {
        // check permissions
        if (!flagPermissions) {
            checkPermissions()
            Toast.makeText(this, "Grant permission", Toast.LENGTH_LONG).show()
            return
        }
        if (location == null) {
            getLocation()
            Toast.makeText(this, "Enable GPS", Toast.LENGTH_LONG).show()
            return
        }
        //start photo fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.res_photo_layout, PhotoFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onCropBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            val imageFragment = ImageFragment()
            imageFragment.imageSetupFragment(bitmap)
            imageFragment.currentLocation = location!!
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.res_photo_layout, imageFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onImageSave() {
        /*val fragment = PhotoDetailFragment()
        fragment.currentLocation = location
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.res_photo_layout, fragment)
            .addToBackStack(null)
            .commit()*/

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.res_photo_layout, PhotoListFragment())
            .commit()
    }

    override fun onRetake() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.res_photo_layout, PhotoFragment())
            .addToBackStack(null)
            .commit()
    }
}

