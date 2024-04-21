package com.example.travel_itinerary_planner.tourism_attraction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.RecommandBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecommandActivity : LoggedInActivity() {

    private lateinit var binding: RecommandBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RecommandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSpinner()
        binding.imageButton13.setOnClickListener {
            finish()
        }
        fetchTourismData()
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.travel_plan_options,
            R.layout.spinner_custom_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerTopTravelPlan.adapter = adapter
        }
        binding.spinnerTopTravelPlan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("SpinnerSelection", "Selected item: $selectedItem")

                when (selectedItem) {
                    "Top Travel Plan" -> {
                        Log.d("SpinnerAction", "Fetching top travel plans")
                        fetchTourismData()
                    }
                    "Recommendation" -> {
                        Log.d("SpinnerAction", "Fetching user preferences and recommendations")
                        fetchUserPreferencesAndRecommendations()
                    }
                    else -> Log.e("SpinnerAction", "Unknown selection: $selectedItem")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }
    private fun setupGridView(items: List<TravelPlanItem>) {
        val adapter = RecommendAdapter(this, items)
        binding.gridView.adapter = adapter
    }

    private fun fetchTourismData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions").orderBy("clickRate", Query.Direction.DESCENDING).get().addOnSuccessListener { documents ->
            val items = documents.map { document ->
                TravelPlanItem(
                    id = document.id,
                    title = document.getString("TourismName") ?: "",
                    location = document.getString("TourismState") ?: "",
                    imageUrl = document.getString("TourismImage") ?: ""
                )
            }
            setupGridView(items)
        }.addOnFailureListener { exception ->
            Log.d("Firestore", "Error getting documents: ", exception)
        }
    }

    fun fetchUserPreferencesAndRecommendations() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                val preferAttraction = document.getDouble("PreferAttraction") ?: 0.0
                val preferRestaurant = document.getDouble("PreferRestaurant") ?: 0.0
                val preferShopping = document.getDouble("PreferShopping") ?: 0.0
                val preferOther = document.getDouble("PreferOther") ?: 0.0
                val totalPreference = preferAttraction + preferRestaurant + preferShopping + preferOther

                val preferencesMap = mapOf(
                    "Tourism Attraction" to (preferAttraction / totalPreference * 100),
                    "Restaurant" to (preferRestaurant / totalPreference * 100),
                    "Shopping" to (preferShopping / totalPreference * 100),
                    "Unknown" to (preferOther / totalPreference * 100)
                )

                fetchTourismAttractionsBasedOnSortedPreferences(preferencesMap)
            }.addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting user preferences: ", exception)
            }
        } else {
            Log.d("FirebaseAuth", "No authenticated user found.")
        }
    }

    fun fetchTourismAttractionsBasedOnSortedPreferences(preferences: Map<String, Double>) {
        val db = FirebaseFirestore.getInstance()
        val fetchCountPerCategory = 20L
        val recommendations = mutableListOf<TravelPlanItem>()
        val categoriesProcessed = mutableSetOf<String>()

        preferences.keys.forEach { category ->
            db.collection("Tourism Attractions")
                .whereEqualTo("TourismCategory", category)
                .limit(fetchCountPerCategory)
                .get()
                .addOnSuccessListener { documents ->
                    val categoryRecommendations = documents.map { document ->
                        TravelPlanItem(
                            id = document.id,
                            title = document.getString("TourismName") ?: "",
                            location = document.getString("TourismState") ?: "",
                            imageUrl = document.getString("TourismImage") ?: ""
                        )
                    }.shuffled().take((preferences[category]!! / 100 * 10).toInt())

                    recommendations.addAll(categoryRecommendations)
                    categoriesProcessed.add(category)


                    if (categoriesProcessed.size == preferences.size) {
                        setupGridView(recommendations.shuffled().take(10))
                    }
                }.addOnFailureListener { exception ->
                    Log.d("Firestore", "Error fetching tourism attractions based on preferences: ", exception)
                }
        }
    }
}
