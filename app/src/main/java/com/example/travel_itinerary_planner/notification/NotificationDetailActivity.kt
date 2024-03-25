package com.example.travel_itinerary_planner.notification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.travel_itinerary_planner.R
import com.example.travel_itinerary_planner.databinding.NotificationBinding

class NotificationDetailActivity : AppCompatActivity() {
    private lateinit var binding: NotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.notification)

        val title = intent.getStringExtra("notification_title") ?: "No Title"
        val body = intent.getStringExtra("notification_body") ?: "No Body"
        val imageUrl = intent.getStringExtra("image_url")


        binding.title.text = title
        binding.body.text = body
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(binding.imageView2)
        }
        binding.imageButton3.setOnClickListener {
            finish()
        }
    }
}