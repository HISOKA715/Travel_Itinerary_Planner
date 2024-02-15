package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.databinding.ActivityBookmarksBinding
import com.example.travel_itinerary_planner.databinding.ActivityEditProfileBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity

class EditProfileActivity : LoggedInActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarEditProfile.setNavigationOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToProfileFragment", true)
            startActivity(intent)
        }
    }
}