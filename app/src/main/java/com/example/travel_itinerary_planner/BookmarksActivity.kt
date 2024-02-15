package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding
import com.example.travel_itinerary_planner.databinding.ActivityNotificationsBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity

class BookmarksActivity : LoggedInActivity() {
    private lateinit var binding: ActivityBookmarksBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarBookmarks.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
        }
    }
}