package com.example.travel_itinerary_planner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.travel_itinerary_planner.databinding.ActivityBottomNavigationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        binding.navView.setupWithNavController(navController)

        val returnToDrawerFragment = intent.getBooleanExtra("returnToDrawerFragment", false)
        if (returnToDrawerFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.drawerFragment)


        }
        }
    }


