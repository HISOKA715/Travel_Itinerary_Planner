package com.example.travel_itinerary_planner.travel

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.databinding.FindLocationBinding


class LocationFindActivity : LoggedInActivity() {
    private lateinit var binding: FindLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FindLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listView = findViewById<ListView>(R.id.list_location)
        val addresses = listOf(
            Address("123 Main Street"),
            Address("456 Elm Street")
        )
        val adapter = ListLocationAdapter(this, addresses)
        listView.adapter = adapter

        binding.imageButton13.setOnClickListener {
            finish()
        }
    }


}