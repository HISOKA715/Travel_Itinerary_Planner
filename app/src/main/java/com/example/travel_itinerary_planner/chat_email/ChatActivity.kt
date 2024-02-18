package com.example.travel_itinerary_planner.chat_email

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.travel_itinerary_planner.HelpCenterActivity
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.ActivityChatBinding
import com.example.travel_itinerary_planner.databinding.ActivityEmailSupportBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarChat.setNavigationOnClickListener {
            val intent = Intent(this, HelpCenterActivity::class.java)
            startActivity(intent)
        }
    }
}