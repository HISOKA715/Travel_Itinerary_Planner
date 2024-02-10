package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding
import com.example.travel_itinerary_planner.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarEditProfile.setOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
        }
    }
}