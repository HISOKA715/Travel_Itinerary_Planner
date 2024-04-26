package com.example.travel_itinerary_planner.notification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.BottomNavigationActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.TAG
import com.example.travel_itinerary_planner.databinding.NotificationBinding

class NotificationDetailActivity : AppCompatActivity() {
    private lateinit var binding: NotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.notification)
        binding.imageButton3.setOnClickListener {
            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToHomeFragment", true)
            startActivity(intent)
        }

    }



}

