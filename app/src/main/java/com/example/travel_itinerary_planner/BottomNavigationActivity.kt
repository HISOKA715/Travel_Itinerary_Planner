package com.example.travel_itinerary_planner

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.travel_itinerary_planner.databinding.ActivityBottomNavigationBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigationActivity : LoggedInActivity(){

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        binding.navView.setupWithNavController(navController)

        val returnToDrawerFragment = intent.getBooleanExtra("returnToDrawerFragment", false)
        val navigateToDrawerFragment = intent.getBooleanExtra("navigateToDrawerFragment", false)
        val returnToProfileFragment = intent.getBooleanExtra("returnToProfileFragment", false)
        val navigateToProfileFragment = intent.getBooleanExtra("navigateToProfileFragment", false)
        val returnToSocialFragment = intent.getBooleanExtra("returnToSocialFragment", false)
        val navigateToSocialFragment = intent.getBooleanExtra("navigateToSocialFragment", false)
        val navigateToOthersPostDetailsFragment = intent.getBooleanExtra("navigateToOthersPostDetailsFragment", false)
        val returnToOthersProfileFragment = intent.getBooleanExtra("returnToOthersProfileFragment", false)
        val navigateToOthersProfileFragment = intent.getBooleanExtra("navigateToOthersProfileFragment", false)
        val returnToPostDetailsFragment = intent.getBooleanExtra("returnToPostDetailsFragment", false)
        val navigateToPostDetailsFragment = intent.getBooleanExtra("navigateToPostDetailsFragment", false)
        val navigateToSmartBudgetFragment = intent.getBooleanExtra("navigateToSmartBudgetFragment", false)
        val returnToHomeFragment = intent.getBooleanExtra("returnToHomeFragment", false)
        val returnToSmartBudgetFragment = intent.getBooleanExtra("returnToSmartBudgetFragment", false)
        if (returnToDrawerFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_drawer)


        } else if (navigateToDrawerFragment) {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_drawer)
        } else if (returnToProfileFragment) {
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_profile)

        } else if (navigateToProfileFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_profile)

        } else if (returnToSocialFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_social
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_social)


        } else if (navigateToSocialFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_social
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_social)


        } else if (returnToOthersProfileFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_social
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_others_profile)
        } else if (navigateToOthersProfileFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_social
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_others_profile)
        } else if (navigateToOthersPostDetailsFragment){
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_social
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_others_post_details)
        } else if (returnToPostDetailsFragment){
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_post_details)
        }
        else if (navigateToPostDetailsFragment){
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_post_details)
        }
        else if (navigateToSmartBudgetFragment){

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_home
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_smart_budget)

        }
        else if (returnToHomeFragment) {

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_home
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_home)
        }
        else if (returnToSmartBudgetFragment){

            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_home
            val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
            navController.navigate(R.id.navigation_smart_budget)

        }

    }
}
