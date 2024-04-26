package com.example.travel_itinerary_planner.useractivity

import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.UserActivityBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.google.android.material.tabs.TabLayoutMediator

class UserListActivity : LoggedInActivity() {
    private lateinit var binding: UserActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        binding.viewPager.adapter = sectionsPagerAdapter
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Rate"
                1 -> "Like"
                2 -> "Feedback"
                else -> null
            }
        }.attach()

        // Handle intent to select specific tab
        val selectedTab = intent.getStringExtra("SELECTED_TAB")
        if (selectedTab != null) {
            val tabIndex = when (selectedTab) {
                "Rate" -> 0
                "Like" -> 1
                "Feedback" -> 2
                else -> 0
            }
            binding.viewPager.setCurrentItem(tabIndex, true)
        }

        binding.imageButton13.setOnClickListener {
            finish()
        }
    }
}