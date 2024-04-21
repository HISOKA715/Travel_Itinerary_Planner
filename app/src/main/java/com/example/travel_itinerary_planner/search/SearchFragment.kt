package com.example.travel_itinerary_planner.search

import DataModel
import MyGridAdapter

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.FragmentSearchBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInFragment
import com.example.travel_itinerary_planner.tourism_attraction.TourismActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONArray
import org.json.JSONObject
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog



class SearchFragment : LoggedInFragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var isExpanded = false





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding.myGridView2.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.myGridView2.adapter.getItem(position) as DataModel1
            val intent = Intent(context, TourismActivity::class.java).apply {
                putExtra("documentId", selectedItem.id)
            }
            startActivity(intent)
        }
        loadSearchHistory()
        fetchTopTourismAttractions()


        binding.myGridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedItem = binding.myGridView.adapter.getItem(position) as DataModel
            saveSearchQuery(selectedItem.query)
            val intent = Intent(context, Search1Activity::class.java).apply {
                putExtra("query", selectedItem.query)
            }
            startActivity(intent)
        }

        binding.imageButtonSearch.setOnClickListener {
            val searchQuery = binding.editTextText.text.toString()
            if (searchQuery.isNotEmpty()) {
                saveSearchQuery(searchQuery)
            }
            val intent = Intent(context, Search1Activity::class.java).apply {
                putExtra("query",binding.editTextText.text.toString() )
            }
            startActivity(intent)
        }

        binding.imageButton7.setOnClickListener {
            showClearHistoryConfirmationDialog()
        }

        val expandClickListener = View.OnClickListener {
            isExpanded = !isExpanded
            binding.imageButton6.setImageResource(if (isExpanded) R.drawable.baseline_arrow_drop_up_24 else R.drawable.baseline_arrow_drop_down_24)
            if (isExpanded) {
                adjustGridViewHeight(binding.myGridView, true)
            } else {
                adjustGridViewHeight(binding.myGridView, false)
            }
            loadSearchHistory()
        }
        fetchUserPreferencesAndRecommendations()
        binding.textView1.setOnClickListener {
            fetchUserPreferencesAndRecommendations()
        }
        binding.imageButton1.setOnClickListener{
            fetchUserPreferencesAndRecommendations()
        }
        binding.textView7.setOnClickListener(expandClickListener)
        binding.imageButton6.setOnClickListener(expandClickListener)
    }


    private fun setupGridView(items: List<DataModel>, gridView: GridView) {
        val adapter = MyGridAdapter(requireContext(), R.layout.grid_item, items)
        gridView.adapter = adapter
    }

    private fun setupGridView1(items: List<DataModel1>, gridView: GridView) {
        val adapter1 = MyGridAdapter1(requireContext(), R.layout.grid_item, items)
        gridView.adapter = adapter1
    }

    private fun adjustGridViewHeight(gridView: GridView, isExpanded: Boolean) {
        val heightInPixels = if (isExpanded) dpToPx(250) else dpToPx(155)
        val params = gridView.layoutParams
        params.height = heightInPixels
        gridView.layoutParams = params
    }

    fun fetchTopTourismAttractions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Tourism Attractions")
            .orderBy("clickRate", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val attractions = documents.map { document ->
                    LocationData(
                        id = document.id,
                        name = document.getString("TourismName") ?: "",
                        clickRate = document.getLong("clickRate") ?: 0
                    )
                }
                updateTopAttractionsList(attractions)
            }
            .addOnFailureListener { exception ->
                Log.e("TourismActivity", "Error fetching top tourism attractions: ", exception)
            }
    }

    private fun showClearHistoryConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear the search history?")
            .setPositiveButton("Yes") { dialog, which ->
                clearSearchHistory()
                Toast.makeText(requireContext(), "Search history cleared.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun updateTopAttractionsList(attractions: List<LocationData>) {
        val adapter = TopAdapter(requireContext(), attractions)
        binding.listView1.adapter = adapter
        binding.listView1.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position) as LocationData
            val intent = Intent(context, TourismActivity::class.java).apply {
                putExtra("documentId", selectedItem.id)
            }
            startActivity(intent)
        }
    }



    private fun dpToPx(dp: Int): Int {

        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun saveSearchQuery(query: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        val historyJson = sharedPreferences.getString("history", "[]")
        val historyArray = JSONArray(historyJson)
        var queryExists = false

        for (i in 0 until historyArray.length()) {
            val item = historyArray.getJSONObject(i)
            if (item.getString("query") == query) {
                item.put("timestamp", System.currentTimeMillis())
                queryExists = true
                break
            }
        }

        if (!queryExists) {
            val newEntry = JSONObject().apply {
                put("query", query)
                put("timestamp", System.currentTimeMillis())
            }
            historyArray.put(newEntry)
        } else {

            val sortedJsonArray = JSONArray()
            val list = mutableListOf<JSONObject>()
            for (i in 0 until historyArray.length()) {
                list.add(historyArray.getJSONObject(i))
            }
            list.sortByDescending { it.getLong("timestamp") }
            list.forEach { sortedJsonArray.put(it) }

            sharedPreferences.edit().putString("history", sortedJsonArray.toString()).apply()
            loadSearchHistory()
            return
        }


        sharedPreferences.edit().putString("history", historyArray.toString()).apply()
        loadSearchHistory()
    }



    private fun loadSearchHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        val historyJson = sharedPreferences.getString("history", "[]")
        val historyArray = JSONArray(historyJson)
        val historyList = mutableListOf<DataModel>()

        for (i in 0 until historyArray.length()) {
            val item = historyArray.getJSONObject(i)
            val query = item.getString("query")
            val timestamp = item.getLong("timestamp")
            historyList.add(DataModel(query, timestamp))
        }

        // Sorting based on timestamp, showing the latest first
        val sortedHistory = if (isExpanded) historyList.sortedByDescending { it.timestamp }.take(10)
        else historyList.sortedByDescending { it.timestamp }.take(6)


        setupGridView(sortedHistory, binding.myGridView)
    }


    private fun clearSearchHistory() {
        val sharedPreferences = requireActivity().getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("history").apply()
        loadSearchHistory()
    }

    fun fetchTourismAttractionsBasedOnSortedPreferences(preferences: Map<String, Double>) {
        val db = FirebaseFirestore.getInstance()
        val fetchCountPerCategory = 20L
        val recommendations = mutableListOf<DataModel1>()
        val categoriesProcessed = mutableSetOf<String>()

        preferences.keys.forEach { category ->
            db.collection("Tourism Attractions")
                .whereEqualTo("TourismCategory", category)
                .limit(fetchCountPerCategory)
                .get()
                .addOnSuccessListener { documents ->
                    val categoryRecommendations = documents.map { document ->
                        DataModel1(
                            id = document.id,
                            primaryText = document.getString("TourismName") ?: "",
                        )
                    }.shuffled().take((preferences[category]!! / 100 * 10).toInt())

                    recommendations.addAll(categoryRecommendations)
                    categoriesProcessed.add(category)


                    if (categoriesProcessed.size == preferences.size) {
                        setupGridView1(recommendations.shuffled().take(6), binding.myGridView2)
                    }
                }.addOnFailureListener { exception ->
                    Log.d("Firestore", "Error fetching tourism attractions based on preferences: ", exception)
                }
        }
    }
}
