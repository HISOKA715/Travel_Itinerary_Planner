package com.example.travel_itinerary_planner.travel

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.MapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MapActivity :LoggedInActivity (), OnMapReadyCallback {
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private lateinit var binding:MapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var isFirstLocationUpdate = true
    private var lastPolylineLocation: LatLng? = null
    private var currentPolyline: Polyline? = null
    private var currentLocationMarker: Marker? = null
    private var arrivalNotificationShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.imageButtonSearch1.setOnClickListener {
            finish()
        }
        binding.imageButtonSearch2.setOnClickListener {
            val destination = LatLng(intent.getDoubleExtra("latitude", 2.0), intent.getDoubleExtra("longitude", 2.0))
            startGoogleMapsNavigation(destination)
        }
    }

    private fun updateDate(documentId: String, docId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        val newDate = calendar.time
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        }
        val newDateString = sdf.format(newDate)

        val locationDateRef = FirebaseFirestore.getInstance().collection("users/$userId/Travel_Plan/$docId/LocationDate")
        locationDateRef.whereEqualTo("LocationDateString", newDateString).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                Toast.makeText(this, "This date already exists. Please choose another date.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val updateMap = mapOf(
                "LocationDate" to newDate,
                "LocationDateString" to newDateString
            )
            locationDateRef.document(documentId).update(updateMap).addOnSuccessListener {
                updateLocationTimes(documentId, newDate, docId)
                Toast.makeText(this, "Date updated successfully.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error updating date: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateLocationTimes(documentId: String, newDate: Date, docId: String) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationsRef = FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$docId/LocationDate/$documentId/Location")

        locationsRef.get().addOnSuccessListener { snapshot ->
            val newCalendar = Calendar.getInstance().apply {
                time = newDate
                timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            }
            Log.d("UpdateLocationTime", "Updating to: ${newCalendar.time}")

            snapshot.documents.forEach { document ->
                document.getTimestamp("LocationTime")?.toDate()?.let { originalTime ->
                    val originalCalendar = Calendar.getInstance().apply {
                        time = originalTime
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                    }
                    val newCalendar = Calendar.getInstance().apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
                        time = newDate
                        set(Calendar.HOUR_OF_DAY, originalCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, originalCalendar.get(Calendar.MINUTE))
                    }
                    Log.d("FinalAdjustment", "Final: ${newCalendar.time}")
                    locationsRef.document(document.id).update("LocationTime", newCalendar.time)
                }
            }
        }
    }


    private fun startGoogleMapsNavigation(destination: LatLng) {
        val navigationUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=d")
        val navigationIntent = Intent(Intent.ACTION_VIEW, navigationUri)
        navigationIntent.setPackage("com.google.android.apps.maps")
        if (navigationIntent.resolveActivity(packageManager) != null) {
            startActivity(navigationIntent)
        } else {
            Toast.makeText(this, "Google Maps app is not installed", Toast.LENGTH_LONG).show()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val destination = LatLng(intent.getDoubleExtra("latitude", 2.0), intent.getDoubleExtra("longitude", 2.0))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    updateLocationUI(currentLocation)
                    fetchDirectionsAndDrawPolyline(currentLocation, destination)
                } else {
                    Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        googleMap.addMarker(MarkerOptions().position(destination).title("Destination"))
    }
    private fun shouldUpdatePolyline(currentLocation: LatLng): Boolean {
        val lastLocation = lastPolylineLocation ?: return true
        val distance = FloatArray(1)
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, currentLocation.latitude, currentLocation.longitude, distance)
        return distance[0] > 50
    }

    fun fetchDirectionsAndDrawPolyline(start: LatLng, end: LatLng) {
        val apiKey = "AIzaSyDtnS0r7CFOE3KAK8Sz07ddFeNumRgP1tw"
        val origin = "${start.latitude},${start.longitude}"
        val destination = "${end.latitude},${end.longitude}"
        val urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=$apiKey"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = URL(urlString).readText()
                val jsonObject = JSONObject(response)
                val routesArray = jsonObject.getJSONArray("routes")
                if (routesArray.length() > 0) {
                    val route = routesArray.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    val distance = leg.getJSONObject("distance").getString("text")
                    val duration = leg.getJSONObject("duration").getString("text")
                    val polyline = route.getJSONObject("overview_polyline").getString("points")
                    val decodedPath = PolyUtil.decode(polyline)

                    withContext(Dispatchers.Main) {
                        currentPolyline?.remove()
                        currentPolyline = googleMap.addPolyline(
                            PolylineOptions().addAll(decodedPath).width(10f).color(ContextCompat.getColor(this@MapActivity, android.R.color.holo_blue_dark))
                        )

                        binding.estimatedTime.text = duration
                        binding.estimatedDistance.text = distance
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Failed to get directions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    updateLocationUI(currentLocation)
                }
            }
        }
    }


    private fun updateLocationUI(currentLocation: LatLng) {
        if (currentLocationMarker == null) {
            currentLocationMarker = googleMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
        } else {
            currentLocationMarker?.position = currentLocation
        }


        val destination = LatLng(intent.getDoubleExtra("latitude", 2.0), intent.getDoubleExtra("longitude", 2.0))
        googleMap.addMarker(MarkerOptions().position(destination).title("Destination"))
        if (!arrivalNotificationShown && calculateDistanceBetweenPoints(currentLocation, destination) <= 500) {
            showArrivalDialog()
            arrivalNotificationShown = true
        }


        if (shouldUpdatePolyline(currentLocation)) {
            fetchDirectionsAndDrawPolyline(currentLocation, destination)
            lastPolylineLocation = currentLocation
        } else {
            currentPolyline?.isVisible = true
        }
        if (isFirstLocationUpdate) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f))
            isFirstLocationUpdate = false
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    updateLocationUI(currentLocation)
                }
            }
        }
    }
    private fun showArrivalDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Arrival Notification")
        builder.setMessage("You have arrived at your destination.")
        builder.setPositiveButton("OK") { dialog, which ->
            val travelplanid = intent.getStringExtra("docId") ?: return@setPositiveButton
            val locationDateid = intent.getStringExtra("locationDateId")  ?: return@setPositiveButton
            val locationid = intent.getStringExtra("locationId")  ?: return@setPositiveButton
            updateDate(locationDateid,travelplanid )
            markLocationAsExecuted(travelplanid, locationDateid, locationid)
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun markLocationAsExecuted(docId: String, locationDateId: String, locationId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val currentTime = com.google.firebase.Timestamp.now()

        val locationRef = FirebaseFirestore.getInstance()
            .collection("users/$userId/Travel_Plan/$docId/LocationDate/$locationDateId/Location")
            .document(locationId)

        locationRef.update(mapOf(
            "LocationStatus" to "Executed",
            "LocationTime" to currentTime
        )).addOnSuccessListener {
            Log.d("MapActivity", "Location marked as executed successfully.")
        }.addOnFailureListener { e ->
            Log.e("MapActivity", "Error marking location as executed: ", e)
        }
    }

    private fun calculateDistanceBetweenPoints(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0]
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback!!, Looper.getMainLooper())
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun centerMapOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13f))
            }
        }
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }


}