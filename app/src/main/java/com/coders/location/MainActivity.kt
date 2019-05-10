package com.coders.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val mFusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    //
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = (10 * 1000).toLong() // 10 seconds
            fastestInterval = (5 * 1000).toLong() // 5 seconds
        }
    }
    private lateinit var locationCallback: LocationCallback
    private lateinit var stringBuilder: StringBuilder

    private val txtLocation by lazy { findViewById<TextView>(R.id.txtLocation) }
    private val txtContinueLocation by lazy { findViewById<TextView>(R.id.txtContinueLocation) }
    private val btnContinueLocation by lazy { findViewById<Button>(R.id.btnContinueLocation) }
    private val btnLocation by lazy { findViewById<Button>(R.id.btnLocation) }

    private var isContinue = false
    private var isGPS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GpsUtils(this).turnGPSOn { isGPSEnable ->
            // turn on GPS
            isGPS = isGPSEnable
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        if (!isContinue) {
                            txtLocation.text = String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                        } else {
                            stringBuilder.append(wayLatitude)
                            stringBuilder.append("-")
                            stringBuilder.append(wayLongitude)
                            stringBuilder.append("\n\n")
                            txtContinueLocation.text = stringBuilder.toString()
                        }
                        if (!isContinue) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback)
                        }
                    }
                }
            }
        }

        setupOfClicks()
    }

    private fun setupOfClicks() {
        btnLocation.setOnClickListener {

            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isContinue = false
            getLocation()
        }

        btnContinueLocation.setOnClickListener {
            if (!isGPS) {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isContinue = true
            stringBuilder = StringBuilder()
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    AppConstants.LOCATION_REQUEST)

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                mFusedLocationClient.lastLocation.addOnSuccessListener(this@MainActivity) { location ->
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        txtLocation.text = String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (isContinue) {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                } else {
                    mFusedLocationClient.lastLocation.addOnSuccessListener(this@MainActivity) { location ->
                        if (location != null) {
                            wayLatitude = location.latitude
                            wayLongitude = location.longitude
                            txtLocation.text = String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                        } else {
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true // flag maintain before get location
            }
        }
    }
}