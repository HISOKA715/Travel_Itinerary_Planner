package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityEditProfileBinding
import com.example.travel_itinerary_planner.databinding.ActivityNotificationsBinding
import com.example.travel_itinerary_planner.databinding.ActivityPersonalDetailsBinding

class PersonalDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarPersonalDetails.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
        }
    }
}