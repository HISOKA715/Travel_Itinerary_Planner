package com.example.travel_itinerary_planner.search


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.SearchBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.tourism_attraction.RecommendAdapter
import com.example.travel_itinerary_planner.tourism_attraction.TravelPlanItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Search1Activity : LoggedInActivity() {
    private lateinit var binding: SearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val query = intent.getStringExtra("query") ?: return
        binding.editTextText.setText(query)
        setupLocationFilter()
        setupTourismCategoryFilter()
        performSearch()
        binding.imageButtonSearch.setOnClickListener {
            performSearch()
        }
        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                performSearch()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerTourismCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                performSearch()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }



    }

    private fun performSearch() {
        val query = binding.editTextText.text.toString().lowercase()
        if (query.isNotEmpty()) {
            setupSearch(query)
        }
    }
    private fun setupSearch(query: String) {
        val selectedLocation = binding.spinnerLocation.selectedItem.toString()
        val selectedCategory = binding.spinnerTourismCategory.selectedItem.toString()

        val db = FirebaseFirestore.getInstance()
        var queryRef: Query = db.collection("Tourism Attractions")

        val adjustedLocation = if (selectedLocation == "Kuala Lumpur") "Wilayah Persekutuan Kuala Lumpur" else selectedLocation

        if (adjustedLocation != "All") {
            queryRef = queryRef.whereEqualTo("TourismState", adjustedLocation)
        }
        if (selectedCategory != "All") {
            queryRef = queryRef.whereEqualTo("TourismCategory", selectedCategory)
        }

        queryRef.get().addOnSuccessListener { documents ->
            val searchResults = documents.mapNotNull { document ->
                val title = document.getString("TourismName") ?: ""
                val location = document.getString("TourismState") ?: ""
                val imageUrl = document.getString("TourismImage") ?: ""
                val category = document.getString("TourismCategory") ?: ""
                val id = document.id

                if (title.contains(query, ignoreCase = true) &&
                    (selectedLocation == "All" || location == adjustedLocation) &&
                    (selectedCategory == "All" || category == selectedCategory)) {
                    TravelPlanItem(id, title, location, imageUrl)
                } else {
                    null
                }
            }

            val adapter = RecommendAdapter(this, searchResults)
            binding.gridView.adapter = adapter
        }.addOnFailureListener { exception ->
            Log.d("Search1Activity", "Error getting documents: ", exception)
        }
    }
    private fun setupLocationFilter() {
        val locations = resources.getStringArray(R.array.search_location).toList()
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        binding.spinnerLocation.adapter = locationAdapter
    }

    private fun setupTourismCategoryFilter() {
        val categories = resources.getStringArray(R.array.tourism_categories).toList()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerTourismCategory.adapter = categoryAdapter
    }
}