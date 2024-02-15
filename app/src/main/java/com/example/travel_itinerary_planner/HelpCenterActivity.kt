package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.chat_email.ChatActivity
import com.example.travel_itinerary_planner.chat_email.EmailSupportActivity
import com.example.travel_itinerary_planner.databinding.ActivityHelpCenterBinding
import com.example.travel_itinerary_planner.logged_in.LoggedInActivity


class HelpCenterActivity : LoggedInActivity() {
    private lateinit var binding: ActivityHelpCenterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarHelp.setNavigationOnClickListener {

            val intent = Intent(this, BottomNavigationActivity::class.java)
            intent.putExtra("returnToDrawerFragment", true)
            startActivity(intent)
            finish()
        }
        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(this, EmailSupportActivity::class.java)
            startActivity(intent)
        }
    }


}