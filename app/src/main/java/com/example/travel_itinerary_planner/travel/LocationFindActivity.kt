package com.example.travel_itinerary_planner.travel

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.FindLocationBinding
import com.google.firebase.Timestamp
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Date

enum class PlaceCategory(val simpleName: String) {
    Shopping("Shopping"),
    Restaurant("Restaurant"),
    AttractionTourism("Tourism Attraction"),
    Unknown("Unknown")
}

class LocationFindActivity : LoggedInActivity() {
    private lateinit var binding: FindLocationBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var adapter: AddressAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FindLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val travelPlanID = intent.getStringExtra("travel_planID")
        val locationDateID = intent.getStringExtra("locationDateID")

        adapter = AddressAdapter(this, mutableListOf())
        binding.listLocation.adapter = adapter
        binding.textField.editText?.addTextChangedListener(object : TextWatcher {
            var searchJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = coroutineScope.launch {
                    s?.toString()?.let { query ->
                        if (query.isNotEmpty()) {
                            delay(100)
                            val predictions = withContext(Dispatchers.IO) { fetchAutocompletePredictions(query) }
                            withContext(Dispatchers.Main) {
                                adapter.clear()
                                adapter.addAll(predictions)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.imageButton13.setOnClickListener {
            finish()
        }

        binding.listLocation.setOnItemClickListener { _, _, position, _ ->
            val selectedAddress = adapter.getItem(position) ?: return@setOnItemClickListener
            Log.d("SelectedAddress", "Address: ${selectedAddress.description}")
            coroutineScope.launch {
                fetchPlaceDetails(selectedAddress.placeId, selectedAddress.description)?.let { placeDetails ->
                    Log.d("PlaceDetails", "Latitude: ${placeDetails.latitude}, Longitude: ${placeDetails.longitude}")
                    Log.d("PlaceDetails", "Category: ${placeDetails.category.simpleName}")
                    Log.d("PlaceDetails", "Name: ${placeDetails.name}")
                    Log.d("PlaceDetails", "Name(Tolower): ${placeDetails.nameToLower}")
                    Log.d("PlaceDetails", "clickRate: ${placeDetails.clickRate}")
                    Log.d("PlaceDetails", "Create Date: ${placeDetails.createDate}")
                    Log.d("PlaceDetails", "Address: ${placeDetails.address}")
                    Log.d("PlaceDetails", "Description: ${placeDetails.description ?: "No Description"}")
                    Log.d("PlaceDetails", "State: ${placeDetails.state ?: "No State"}")
                    var photoUrl =""
                    if (placeDetails.photoReference != null) {
                        photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=1080&photoreference=${placeDetails.photoReference}&key=AIzaSyDtnS0r7CFOE3KAK8Sz07ddFeNumRgP1tw"
                        Log.d("PlaceDetails", "Photo URL: $photoUrl")
                    } else {
                        Log.d("PlaceDetails", "Photo Reference: No Photo Available")
                    }
                    val placeDetailsMap = placeDetails.toMap(photoUrl)
                    checkAndAddTourismAttraction(placeDetailsMap)
                    val intent = Intent(this@LocationFindActivity, LocationAddActivity::class.java).apply {
                        putExtra("travel_planID", travelPlanID)
                        putExtra("locationDateID", locationDateID)
                        putExtra("photo", photoUrl)
                        putExtra("name",placeDetails.name)
                        putExtra("address",selectedAddress.description)
                        putExtra("latitude",placeDetails.latitude)
                        putExtra("longitude",placeDetails.longitude)
                        putExtra("state",placeDetails.state)
                    }
                    startActivity(intent)
                }
            }
        }
    }


    suspend fun fetchPlaceDetails(placeId: String, address:String): PlaceDetailsResult? = withContext(Dispatchers.IO) {
        val apiKey = "AIzaSyDtnS0r7CFOE3KAK8Sz07ddFeNumRgP1tw"
        val urlString = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=name,geometry,photos,formatted_address,address_components,types&key=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connect()
            val response = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            val jsonResponse = JSONObject(response)

            if (!jsonResponse.isNull("result")) {
                val result = jsonResponse.getJSONObject("result")
                val geometry = result.getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                val lat = location.getDouble("lat")
                val lng = location.getDouble("lng")
                val name = result.optString("name")
                val nameToLower = name.lowercase()
                val formattedAddress =  address
                val malaysiaZoneId: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
                val createDate: LocalDateTime = LocalDateTime.now(malaysiaZoneId)
                val typesJsonArray = result.optJSONArray("types")
                val typesList = mutableListOf<String>()
                for (i in 0 until typesJsonArray.length()) {
                    typesList.add(typesJsonArray.optString(i))
                }

                val category = determinePlaceCategory(typesList)
                val clickrate = 0
                val addressComponents = result.getJSONArray("address_components")
                val description = "the tourism attraction dont have any description"
                var state: String? = null
                for (i in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(i)
                    val typesArray = component.getJSONArray("types")
                    for (j in 0 until typesArray.length()) {
                        if (typesArray.getString(j) == "administrative_area_level_1") {
                            state = component.getString("long_name")
                            break
                        }
                    }
                    if (state != null) break
                }
                val photoReference = result.optJSONArray("photos")?.optJSONObject(0)?.optString("photo_reference", null)



                return@withContext PlaceDetailsResult(lat, lng, category, photoReference, name, nameToLower,formattedAddress, description, state,clickrate,createDate)
            } else {
                Log.e("LocationFindActivity", "No result found")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("LocationFindActivity", "Error fetching place details", e)
            return@withContext null
        } finally {
            connection.disconnect()
        }
    }

    fun PlaceDetailsResult.toMap(photo:String): Map<String, Any> {

        val zdt: ZonedDateTime = createDate.atZone(ZoneId.of("Asia/Kuala_Lumpur"))
        val date: Date = Date.from(zdt.toInstant())
        val timestamp = Timestamp(date)
        val geoPoint = GeoPoint(latitude, longitude)
        return mapOf(
            "point" to geoPoint,
            "TourismCategory" to category.simpleName,
            "TourismImage" to (photo),
            "TourismName" to name,
            "TourismNameLowercase" to nameToLower,
            "TourismAddress" to address,
            "TourismDesc" to (description ?: ""),
            "TourismState" to (state ?: ""),
            "clickRate" to clickRate,
            "TourismPostDate" to timestamp
        )
    }
    fun checkAndAddTourismAttraction(placeDetailsMap: Map<String, Any>) {
        val collection = FirebaseFirestore.getInstance().collection("Tourism Attractions")
        val name = placeDetailsMap["TourismName"] as String
        val address = placeDetailsMap["TourismAddress"] as String
        Log.d("check", "Name: $name")
        Log.d("check", "Name: $address")

        val point = placeDetailsMap["point"] as GeoPoint

        collection.whereEqualTo("point", point).get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                collection.whereEqualTo("TourismName", name).get().addOnSuccessListener { querySnapshotByName ->
                    if (querySnapshotByName.isEmpty) {
                        collection.whereEqualTo("TourismAddress", address).get().addOnSuccessListener { querySnapshotByAddress ->
                            if (querySnapshotByAddress.isEmpty) {
                                collection.add(placeDetailsMap).addOnSuccessListener {
                                    Log.d("FirebaseFirestore", "Record added successfully with ID: ${it.id}")
                                }.addOnFailureListener {
                                    Log.e("FirebaseFirestore", "Error adding document: $it")
                                }
                            } else {
                                Log.d("FirebaseFirestore", "Address found!")
                            }
                        }
                    } else {
                        Log.d("FirebaseFirestore", "Name found!")
                    }
                }
            } else {
                Log.d("FirebaseFirestore", "Point found!")
            }
        }
    }
    fun fetchAutocompletePredictions(query: String): List<Address> {
        val apiKey = "AIzaSyDtnS0r7CFOE3KAK8Sz07ddFeNumRgP1tw"
        val input = query.replace(" ", "+")
        val urlString = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$input&components=country:my&key=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connect()
            val response = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            val jsonResponse = JSONObject(response)
            val predictions = jsonResponse.getJSONArray("predictions")
            val result = mutableListOf<Address>()
            Log.d("API Response", response)
            for (i in 0 until predictions.length()) {
                val prediction = predictions.getJSONObject(i)
                val description = prediction.getString("description")
                val placeId = prediction.getString("place_id")
                result.add(Address(description, placeId))
            }
            return result
        } catch (e: Exception) {
            Log.e("LocationFindActivity", "Error fetching autocomplete predictions", e)
            return emptyList()
        } finally {
            connection.disconnect()
        }
    }

    fun determinePlaceCategory(types: List<String>): PlaceCategory {
        return when {
            types.any { it == "shopping_mall" || it == "store" } -> PlaceCategory.Shopping
            types.any { it == "restaurant" || it == "food" } -> PlaceCategory.Restaurant
            types.any { it == "tourist_attraction" || it == "museum" || it == "park" } -> PlaceCategory.AttractionTourism
            else -> PlaceCategory.Unknown
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}