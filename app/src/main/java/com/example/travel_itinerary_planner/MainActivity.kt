package com.example.travel_itinerary_planner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.example.travel_itinerary_planner.databinding.ActivityMainBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity
import com.example.travel_itinerary_planner.login_register_reset.LoginActivity

class MainActivity : LoggedInActivity() {


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }


}