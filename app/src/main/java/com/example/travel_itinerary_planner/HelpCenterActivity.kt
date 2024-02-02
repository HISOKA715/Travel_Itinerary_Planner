package com.example.travel_itinerary_planner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.chat_email.ChatActivity
import com.example.travel_itinerary_planner.chat_email.EmailSupportActivity
import com.example.travel_itinerary_planner.databinding.ActivityHelpCenterBinding

class HelpCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpCenterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)
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